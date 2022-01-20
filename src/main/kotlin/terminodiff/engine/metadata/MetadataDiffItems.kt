package terminodiff.terminodiff.engine.metadata

import org.hl7.fhir.r4.model.*
import terminodiff.engine.concepts.KeyedListDiff
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.i18n.LocalizedStrings

abstract class MetadataDiffItem(
    val label: LocalizedStrings.() -> String,
    val expectDifferences: Boolean,
    protected val localizedStrings: LocalizedStrings,
) {
    abstract fun compare(
        left: CodeSystem, right: CodeSystem,
    ): ResultPair

    abstract fun getRenderDisplay(codeSystem: CodeSystem): String?
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
    override fun getRenderDisplay(codeSystem: CodeSystem): String? = instanceGetter.invoke(codeSystem)
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

abstract class MetadataKeyedListDiffItem<Type, KeyType>(
    label: LocalizedStrings.() -> String,
    expectDifferences: Boolean,
    localizedStrings: LocalizedStrings,
    private val instanceGetter: (CodeSystem) -> List<Type>,
    private val displayLimit: Int = 3,
) : MetadataDiffItem(label, expectDifferences, localizedStrings) {

    abstract fun getKey(instance: Type): KeyType
    abstract fun getStringValue(instance: Type): String?


    override fun getRenderDisplay(codeSystem: CodeSystem): String? =
        instanceGetter.invoke(codeSystem).mapNotNull(::getLongDisplayValue)
            .joinToString("; ", limit = displayLimit)

    override fun compare(left: CodeSystem, right: CodeSystem): ResultPair {
        val leftValue = instanceGetter.invoke(left)
        val rightValue = instanceGetter.invoke(right)
        val keyedListDiff = KeyedListDiff(leftValue, rightValue, ::getKey, ::getStringValue).executeDiff()
        return when {
            keyedListDiff.any { it.result != KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL } -> MetadataComparisonResult.DIFFERENT to { differentValue }
            else -> MetadataComparisonResult.IDENTICAL to null
        }
    }

    abstract fun getLongDisplayValue(instance: Type): String?
}

class IdentifierListDiffItem(localizedStrings: LocalizedStrings) :
    MetadataKeyedListDiffItem<Identifier, Pair<Identifier.IdentifierUse, String>>(label = { identifiers },
        expectDifferences = false,
        localizedStrings = localizedStrings,
        instanceGetter = { it.identifier }) {

    override fun getKey(instance: Identifier): Pair<Identifier.IdentifierUse, String> = instance.use to instance.system

    override fun getStringValue(instance: Identifier): String? = instance.value

    override fun getLongDisplayValue(instance: Identifier): String = formatIdentifier(instance)

}

/*abstract class MetadataListDiffItem<Type, Key : Comparable<Key>, ComparisonValue : Comparable<ComparisonValue>>(
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

    override fun getRenderDisplay(codeSystem: CodeSystem): String? {
        val instance = instanceGetter.invoke(codeSystem)
        val count = localizedStrings.numberItems_.invoke(instance.size)
        val joinedItems = if (instance.isEmpty()) null else instance.joinToString(separator = "; ",
            limit = 2,
            transform = ::formatInstance)
        return joinedItems?.let { "$count: $it" } ?: count
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
}*/

class ContactComparisonItem(
    localizedStrings: LocalizedStrings,
) : MetadataKeyedListDiffItem<ContactDetail, String>(
    { contact },
    false,
    localizedStrings,
    { it.contact }
) {
    override fun getKey(instance: ContactDetail): String = instance.name

    override fun getStringValue(instance: ContactDetail): String? = formatDisplay(instance)

    override fun getLongDisplayValue(instance: ContactDetail): String = formatDisplay(instance, limit = 2)

    private fun formatDisplay(instance: ContactDetail, limit: Int? = null) =
        formatEntity {
            if (instance.hasName()) append(instance.name)
            if (instance.hasTelecom()) {
                val notNullInstances = instance.telecom.filterNotNull()
                val telecom =
                    notNullInstances.joinToString(separator = "; ",
                        limit = limit ?: notNullInstances.size,
                        transform = ::formatTelecom)
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

fun formatIdentifier(identifier: Identifier) = formatEntity {
    if (identifier.hasUse()) append("[${identifier.use.display}] ")
    if (identifier.hasSystem()) append("(${identifier.system}) ")
    if (identifier.hasValue()) append(identifier.value) else append("null")
}

fun formatCoding(coding: Coding) = formatEntity {
    if (coding.hasSystem()) append("(${coding.system}) ")
    if (coding.hasVersion()) append("(@${coding.version}) ")
    if (coding.hasCode()) append(coding.code)
    if (coding.hasDisplay()) append(": ${coding.display}")
}

private fun formatQuantity(quantity: Quantity) = formatEntity {
    if (quantity.hasComparator()) append("${quantity.comparator.name} ")
    if (quantity.hasValue()) append(quantity.value)
    if (quantity.hasUnit()) append(" ${quantity.unit}")
    if (quantity.hasCode()) { //FHIR R4: qty-3 states code.empty() or system.exists()
        append(" (${quantity.system} = ${quantity.code})")
    }
}

private fun formatRange(range: Range): String = formatEntity {
    if (range.hasLow()) append("${formatQuantity(range.low)} - ")
    if (range.hasHigh()) append(formatQuantity(range.high))
}

private fun formatReference(reference: Reference): String = formatEntity {
    if (reference.hasType()) append("${reference.type} ")
    if (reference.hasReference()) append(reference.reference)
    if (reference.hasIdentifier()) append(" (${formatIdentifier(reference.identifier)})")
}

class CodeableConceptComparisonItem(
    label: LocalizedStrings.() -> String,
    localizedStrings: LocalizedStrings,
    expectDifferences: Boolean = false,
    instanceGetter: (CodeSystem) -> List<CodeableConcept>,
) : MetadataKeyedListDiffItem<CodeableConcept, String>(label, expectDifferences, localizedStrings, instanceGetter) {

    override fun getKey(instance: CodeableConcept): String = instance.text ?: "null"

    override fun getStringValue(instance: CodeableConcept): String = formatCodingList(instance.coding)

    private fun formatCodingList(codings: List<Coding>, limit: Int = codings.size) =
        codings.joinToString(limit = limit, transform = ::formatCoding)

    override fun getLongDisplayValue(instance: CodeableConcept): String = formatCodingList(instance.coding, 2)

}

class UsageContextComparisonItem(
    localizedStrings: LocalizedStrings,
) : MetadataKeyedListDiffItem<UsageContext, String>({ useContext },
    expectDifferences = false,
    localizedStrings,
    { it.useContext }) {
    override fun getKey(instance: UsageContext): String = formatCoding(instance.code)

    override fun getStringValue(instance: UsageContext): String? = formatValue(instance)

    override fun getLongDisplayValue(instance: UsageContext): String? = formatValue(instance)

    private fun formatValue(usageContext: UsageContext) = when {
        usageContext.hasValueCodeableConcept() -> "${usageContext.valueCodeableConcept.text} - ${
            usageContext.valueCodeableConcept.coding.joinToString(limit = 2) { formatCoding(it) }
        }"
        usageContext.hasValueQuantity() -> formatQuantity(usageContext.valueQuantity)
        usageContext.hasValueRange() -> formatRange(usageContext.valueRange)
        usageContext.hasValueReference() -> formatReference(usageContext.valueReference)
        else -> null
    }
}