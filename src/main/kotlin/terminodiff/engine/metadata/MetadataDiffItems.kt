package terminodiff.terminodiff.engine.metadata

import org.hl7.fhir.r4.model.*
import terminodiff.engine.concepts.KeyedListDiff
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.chipForDiffResult
import terminodiff.ui.util.textForValue

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
    DIFFERENT, IDENTICAL
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

abstract class MetadataKeyedListDiffItem<ItemType, KeyType>(
    label: LocalizedStrings.() -> String,
    expectDifferences: Boolean,
    localizedStrings: LocalizedStrings,
    private val instanceGetter: (CodeSystem) -> List<ItemType>,
    private val displayLimit: Int = 3,
) : MetadataDiffItem(label, expectDifferences, localizedStrings) {

    abstract fun getKey(instance: ItemType): KeyType
    abstract fun getStringValue(instance: ItemType): String?

    abstract fun getKeyColumns(
        localizedStrings: LocalizedStrings,
    ): List<ColumnSpec<KeyedListDiffResult<KeyType, String>>>

    fun getColumns(
        localizedStrings: LocalizedStrings,
        diffColors: DiffColors,
    ): List<ColumnSpec<KeyedListDiffResult<KeyType, String>>> =
        getKeyColumns(localizedStrings).plus(getCommonColumns(localizedStrings, diffColors))

    private fun getCommonColumns(
        localizedStrings: LocalizedStrings,
        diffColors: DiffColors,
    ): List<ColumnSpec<KeyedListDiffResult<KeyType, String>>> = listOf(ColumnSpec(localizedStrings.comparison, 0.1f) {
        chipForDiffResult(localizedStrings, diffColors, it.result)
    }, ColumnSpec(localizedStrings.leftValue, 0.2f) {
        textForValue(it.leftValue?.joinToString())
    }, ColumnSpec(localizedStrings.rightValue, 0.2f) {
        textForValue(it.rightValue?.joinToString())
    })

    private fun detailedCompare(
        left: CodeSystem,
        right: CodeSystem,
    ): MutableList<KeyedListDiffResult<KeyType, String>> {
        val leftValue = instanceGetter.invoke(left)
        val rightValue = instanceGetter.invoke(right)
        return KeyedListDiff(leftValue, rightValue, ::getKey, ::getStringValue).executeDiff()
    }

    abstract fun mapComparisonResult(
        result: MetadataComparisonResult,
        explanation: (LocalizedStrings.() -> String)?,
        detailedCompare: MutableList<KeyedListDiffResult<KeyType, String>>,
    ): MetadataListComparison<ItemType, KeyType>

    fun getComparisonResult(left: CodeSystem, right: CodeSystem) = compare(left, right).let { (result, explanation) ->
        mapComparisonResult(result, explanation, detailedCompare(left, right))
    }


    override fun getRenderDisplay(codeSystem: CodeSystem): String =
        instanceGetter.invoke(codeSystem).mapNotNull(::getLongDisplayValue).joinToString("; ", limit = displayLimit)

    override fun compare(left: CodeSystem, right: CodeSystem): ResultPair {
        val detailedResult = detailedCompare(left, right)
        return when {
            detailedResult.any { it.result != KeyedListDiffResultKind.IDENTICAL } -> MetadataComparisonResult.DIFFERENT to null
            else -> MetadataComparisonResult.IDENTICAL to null
        }
    }

    abstract fun getLongDisplayValue(instance: ItemType): String?
}

typealias IdentifierKeyType = Pair<Identifier.IdentifierUse, String>

class IdentifierListDiffItem(localizedStrings: LocalizedStrings) :
    MetadataKeyedListDiffItem<Identifier, IdentifierKeyType>(label = { identifiers },
        expectDifferences = false,
        localizedStrings = localizedStrings,
        instanceGetter = { it.identifier }) {

    override fun getKey(instance: Identifier): IdentifierKeyType = instance.use to instance.system

    override fun getStringValue(instance: Identifier): String? = instance.value

    override fun getLongDisplayValue(instance: Identifier): String = formatIdentifier(instance)

    override fun mapComparisonResult(
        result: MetadataComparisonResult,
        explanation: (LocalizedStrings.() -> String)?,
        detailedCompare: MutableList<KeyedListDiffResult<IdentifierKeyType, String>>,
    ): MetadataListComparison<Identifier, IdentifierKeyType> = IdentifierListComparison(listDiffItem = this,
        result = result,
        explanation = explanation,
        detailedResult = detailedCompare)

    override fun getKeyColumns(
        localizedStrings: LocalizedStrings,
    ): List<ColumnSpec<KeyedListDiffResult<IdentifierKeyType, String>>> {
        return listOf(ColumnSpec(localizedStrings.use, 0.1f) { textForValue(it.key.first) },
            ColumnSpec(localizedStrings.system, 0.1f) { textForValue(it.key.second) })
    }
}

class ContactComparisonItem(
    localizedStrings: LocalizedStrings,
) : MetadataKeyedListDiffItem<ContactDetail, String>({ contact }, false, localizedStrings, { it.contact }) {
    override fun getKey(instance: ContactDetail): String = instance.name ?: ""

    override fun getStringValue(instance: ContactDetail): String = formatDisplay(instance)

    override fun getLongDisplayValue(instance: ContactDetail): String = formatDisplay(instance, limit = 2)

    private fun formatDisplay(instance: ContactDetail, limit: Int? = null) = formatEntity {
        if (instance.hasName()) append(instance.name)
        if (instance.hasTelecom()) {
            val notNullInstances = instance.telecom.filterNotNull()
            val telecom = notNullInstances.joinToString(separator = "; ",
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

    override fun getKeyColumns(
        localizedStrings: LocalizedStrings,
    ): List<ColumnSpec<KeyedListDiffResult<String, String>>> = listOf(ColumnSpec(localizedStrings.name, 0.15f) {
        textForValue(it.key)
    })

    override fun mapComparisonResult(
        result: MetadataComparisonResult,
        explanation: (LocalizedStrings.() -> String)?,
        detailedCompare: MutableList<KeyedListDiffResult<String, String>>,
    ): MetadataListComparison<ContactDetail, String> = ContactListComparison(listDiffItem = this,
        result = result,
        explanation = explanation,
        detailedResult = detailedCompare)
}

private fun formatEntity(
    init: String? = null,
    postProcessing: (String.() -> String)? = null,
    builder: StringBuilder.() -> Unit,
) = when (init) {
    null -> StringBuilder()
    else -> StringBuilder(init)
}.apply(builder).toString().let { s ->
    postProcessing?.invoke(s) ?: s
}.trim()

fun formatIdentifier(identifier: Identifier) = formatEntity {
    if (identifier.hasUse()) append("[${identifier.use.display}] ")
    if (identifier.hasSystem()) append("(${identifier.system}) ")
    if (identifier.hasValue()) append(identifier.value) else append("null")
}

fun formatCoding(coding: Coding) = formatEntity(postProcessing = { trimStart(':') }) {
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

    override fun getKeyColumns(
        localizedStrings: LocalizedStrings,
    ): List<ColumnSpec<KeyedListDiffResult<String, String>>> = listOf(ColumnSpec(localizedStrings.text, 0.3f) {
        textForValue(it.key)
    })

    override fun mapComparisonResult(
        result: MetadataComparisonResult,
        explanation: (LocalizedStrings.() -> String)?,
        detailedCompare: MutableList<KeyedListDiffResult<String, String>>,
    ): MetadataListComparison<CodeableConcept, String> = CodeableConceptComparison(listDiffItem = this,
        result = result,
        explanation = explanation,
        detailedResult = detailedCompare)

}

class UsageContextComparisonItem(
    localizedStrings: LocalizedStrings,
) : MetadataKeyedListDiffItem<UsageContext, String>({ useContext },
    expectDifferences = false,
    localizedStrings,
    { it.useContext }) {
    override fun getKey(instance: UsageContext): String {
        return formatCoding(instance.code)
    }

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

    override fun getKeyColumns(
        localizedStrings: LocalizedStrings,
    ): List<ColumnSpec<KeyedListDiffResult<String, String>>> = listOf(ColumnSpec(localizedStrings.code, 0.3f) {
        textForValue(it.key)
    })

    override fun mapComparisonResult(
        result: MetadataComparisonResult,
        explanation: (LocalizedStrings.() -> String)?,
        detailedCompare: MutableList<KeyedListDiffResult<String, String>>,
    ): MetadataListComparison<UsageContext, String> = UsageContextComparison(listDiffItem = this,
        result = result,
        explanation = explanation,
        detailedResult = detailedCompare)
}