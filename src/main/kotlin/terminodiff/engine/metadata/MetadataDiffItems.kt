package terminodiff.terminodiff.engine.metadata

import org.hl7.fhir.r4.model.*
import terminodiff.i18n.LocalizedStrings

abstract class MetadataDiffItem(
    val label: LocalizedStrings.() -> String,
    val expectDifferences: Boolean,
    protected val localizedStrings: LocalizedStrings,
) {
    abstract fun compare(
        left: CodeSystem, right: CodeSystem,
    ): ResultPair

    abstract val renderDisplay: (CodeSystem) -> String?
}

enum class MetadataComparisonResult {
    IDENTICAL, DIFFERENT
}

open class StringComparisonItem(
    label: LocalizedStrings.() -> String,
    expectDifferences: Boolean,
    localizedStrings: LocalizedStrings,
    val drawItalic: Boolean = false,
    private val instanceGetter: (CodeSystem) -> String?,
) : MetadataDiffItem(label, expectDifferences, localizedStrings) {
    override val renderDisplay: (CodeSystem) -> String?
        get() = instanceGetter

    override fun compare(
        left: CodeSystem,
        right: CodeSystem,
    ): ResultPair {
        val leftValue = instanceGetter.invoke(left)
        val rightValue = instanceGetter.invoke(right)
        return when {
            leftValue == null && rightValue == null -> MetadataComparisonResult.IDENTICAL to { bothValuesAreNull }
            null in listOf(leftValue, rightValue) -> MetadataComparisonResult.DIFFERENT to { oneValueIsNull }
            leftValue != rightValue -> MetadataComparisonResult.DIFFERENT to { differentValue }
            else -> MetadataComparisonResult.IDENTICAL to null
        }
    }
}

class BooleanComparisonItem(
    label: LocalizedStrings.() -> String,
    expectDifferences: Boolean,
    localizedStrings: LocalizedStrings,
    private val booleanGetter: (CodeSystem) -> Boolean?,
) : StringComparisonItem(label,
    expectDifferences,
    localizedStrings,
    drawItalic = true,
    instanceGetter = { localizedStrings.boolean_(booleanGetter(it)) })

class NumericComparisonItem(
    label: LocalizedStrings.() -> String,
    expectDifferences: Boolean,
    localizedStrings: LocalizedStrings,
    private val numericGetter: (CodeSystem) -> Number?,
) : StringComparisonItem(label, expectDifferences, localizedStrings, drawItalic = true, instanceGetter = {
    numericGetter(it).toString()
})

abstract class MetadataListDiffItem<Type, Key : Comparable<Key>, ComparisonValue : Comparable<ComparisonValue>>(
    label: LocalizedStrings.() -> String,
    expectDifferences: Boolean,
    localizedStrings: LocalizedStrings,
    private val instanceGetter: (CodeSystem) -> List<Type>,
) : MetadataDiffItem(label, expectDifferences, localizedStrings) {

    protected open fun compareItem(
        key: Key, l: ComparisonValue, r: ComparisonValue,
    ): Pair<MetadataComparisonResult, String?> = when (l == r) {
        true -> MetadataComparisonResult.IDENTICAL to null
        else -> MetadataComparisonResult.DIFFERENT to localizedStrings.keyIsDifferent_.invoke(key.toString())
    }

    open fun deepCompare(leftValue: List<Type>, rightValue: List<Type>): Map<Key, StringResultPair> {
        val left = leftValue.associate { getComparisonKey(it) to getComparisonValue(it) }
        val right = rightValue.associate { getComparisonKey(it) to getComparisonValue(it) }
        val allKeysMatch = left.keys.toSortedSet() == right.keys.toSortedSet()
        val commonKeys = left.keys.intersect(right.keys)
        val differentKeys = left.keys.plus(right.keys).minus(commonKeys)
        val keyComparisons = commonKeys.associateWith { compareItem(it, left[it]!!, right[it]!!) }
        return when (allKeysMatch) {
            true -> keyComparisons
            else -> keyComparisons.plus(differentKeys.associateWith { MetadataComparisonResult.DIFFERENT to localizedStrings.oneValueIsNull })
        }
    }

    abstract fun getComparisonKey(value: Type): Key
    abstract fun getComparisonValue(value: Type): ComparisonValue?
    abstract fun formatInstance(data: Type): String

    override val renderDisplay: (CodeSystem) -> String?
        get() = { codeSystem ->
            val instance = instanceGetter.invoke(codeSystem)
            val count = localizedStrings.numberItems_.invoke(instance.size)
            val joinedItems = if (instance.isEmpty()) null else instance.joinToString(separator = "; ",
                limit = 2,
                transform = ::formatInstance)
            joinedItems?.let { "$count: $it" } ?: count
        }

    override fun compare(
        left: CodeSystem,
        right: CodeSystem,
    ): ResultPair {
        val leftValue = instanceGetter.invoke(left)
        val rightValue = instanceGetter.invoke(right)
        return when {
            leftValue.isEmpty() && rightValue.isEmpty() -> MetadataComparisonResult.IDENTICAL to { bothListsAreEmpty }
            else -> {
                val deep = deepCompare(leftValue, rightValue)
                val differentValues =
                    deep.values.filter { it.first == MetadataComparisonResult.DIFFERENT }.mapNotNull { it.second }
                when (val countDifferent = deep.values.count { it.first == MetadataComparisonResult.DIFFERENT }) {
                    0 -> MetadataComparisonResult.IDENTICAL to null
                    else -> MetadataComparisonResult.DIFFERENT to {
                        numberDifferentReason_.invoke(countDifferent, differentValues)
                    }
                }
            }
        }
    }
}

class IdentifierDiffItem(
    localizedStrings: LocalizedStrings,
) : MetadataListDiffItem<Identifier, String, String>({ identifiers }, false, localizedStrings, { it.identifier }) {

    override fun getComparisonKey(value: Identifier): String = value.system ?: "null"
    override fun getComparisonValue(value: Identifier): String = formatInstance(value)

    override fun formatInstance(data: Identifier): String = formatEntity {
        if (data.hasUse()) append("[${data.use.display}] ")
        if (data.hasSystem()) append("(${data.system}) ")
        if (data.hasValue()) append(data.value) else append("null")
    }
}

class ContactComparisonItem(
    localizedStrings: LocalizedStrings,
) : MetadataListDiffItem<ContactDetail, String, String>({ contact }, false, localizedStrings, { it.contact }) {

    override fun getComparisonKey(value: ContactDetail): String = value.name ?: "null"

    override fun getComparisonValue(value: ContactDetail): String = formatInstance(value)

    override fun formatInstance(data: ContactDetail): String = formatEntity {
        if (data.hasName()) append(data.name)
        if (data.hasTelecom()) {
            val telecom =
                data.telecom.filterNotNull().joinToString(separator = "; ", limit = 2, transform = ::formatTelecom)
            append(": $telecom")
        }
    }

    private fun formatTelecom(contact: ContactPoint): String = formatEntity {
        if (contact.hasUse()) append("[${contact.use.display}] ")
        if (contact.hasSystem()) append("(${contact.system.display}) ")
        if (contact.hasValue()) append(contact.value)
        if (contact.hasRank()) append(" @${contact.rank}")
    }
}

private fun formatEntity(init: String? = null, builder: StringBuilder.() -> Unit) = when (init) {
    null -> StringBuilder()
    else -> StringBuilder(init)
}.apply(builder).trim().toString()

private fun formatCoding(coding: Coding) = formatEntity {
    if (coding.hasSystem()) append("(${coding.system}) ")
    if (coding.hasVersion()) append("(@${coding.version}) ")
    if (coding.hasCode()) append(coding.code)
}

private fun formatQuantity(quantity: Quantity) = formatEntity {
    if (quantity.hasComparator()) append("${quantity.comparator.name} ")
    if (quantity.hasValue()) append(quantity.value)
    if (quantity.hasUnit()) append(" ${quantity.unit}")
    if (quantity.hasCode()) { //FHIR R4: qty-3 states code.empty() or system.exists()
        append(" (${quantity.system} = ${quantity.code})")
    }
}

class CodeableConceptComparisonItem(
    label: LocalizedStrings.() -> String,
    localizedStrings: LocalizedStrings,
    expectDifferences: Boolean = false,
    private val instanceGetter: (CodeSystem) -> CodeableConcept,
) : MetadataDiffItem(label,
    expectDifferences,
    localizedStrings) {

    private val codingDiffItem =
        CodingComparisonItem(label, localizedStrings, expectDifferences) { instanceGetter.invoke(it).coding }

    override fun compare(left: CodeSystem, right: CodeSystem): ResultPair {
        val leftValue = instanceGetter.invoke(left)
        val rightValue = instanceGetter.invoke(right)
        val codingComparison = codingDiffItem.compare(left, right)
        val textNotEqual = leftValue.text != rightValue.text
        return when {
            leftValue.hasText() && rightValue.hasText() -> when {
                textNotEqual -> when (codingComparison.first) {
                    MetadataComparisonResult.DIFFERENT -> MetadataComparisonResult.DIFFERENT to {
                        textDifferentAndAnotherReason_.invoke(codingComparison.second!!.invoke(localizedStrings))
                    }
                    else -> MetadataComparisonResult.DIFFERENT to { textDifferent }
                }
                else -> codingComparison
            }
            else -> codingComparison
        }
    }

    override val renderDisplay: (CodeSystem) -> String?
        get() = { codeSystem ->
            val instance = instanceGetter.invoke(codeSystem)
            formatEntity {
                if (instance.hasText()) append(instance.text)
                if (instance.hasCoding()) append(": ", instance.coding.joinToString(limit = 2) { formatCoding(it) })
                trimStart(':')
            }
        }
}

class CodingComparisonItem(
    label: LocalizedStrings.() -> String,
    localizedStrings: LocalizedStrings,
    expectDifferences: Boolean = false,
    instanceGetter: (CodeSystem) -> List<Coding>,
) : MetadataListDiffItem<Coding, String, String>(label, expectDifferences, localizedStrings, instanceGetter) {
    override fun getComparisonKey(value: Coding): String = value.system ?: "null"

    override fun getComparisonValue(value: Coding): String = formatCoding(coding = value)

    override fun formatInstance(data: Coding): String = formatCoding(coding = data)
}


class UsageContextComparisonItem(
    localizedStrings: LocalizedStrings,
) : MetadataListDiffItem<UsageContext, String, String>({ useContext }, false, localizedStrings, { it.useContext }) {
    override fun getComparisonKey(value: UsageContext): String = formatCoding(value.code)

    override fun getComparisonValue(value: UsageContext): String? = formatValue(value)

    override fun formatInstance(data: UsageContext): String = when (val formatValue = formatValue(data)) {
        null -> getComparisonKey(data)
        else -> "${getComparisonKey(data)} - $formatValue}"
    }

    private fun formatValue(usageContext: UsageContext) = when {
        usageContext.hasValueCodeableConcept() -> "${usageContext.valueCodeableConcept.text} - ${
            usageContext.valueCodeableConcept.coding.joinToString(limit = 2) { formatCoding(it) }
        }"
        usageContext.hasValueQuantity() -> formatQuantity(usageContext.valueQuantity)
        else -> null
    }
}