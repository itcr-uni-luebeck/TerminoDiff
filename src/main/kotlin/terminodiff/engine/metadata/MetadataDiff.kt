package terminodiff.terminodiff.engine.metadata


import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.ContactDetail
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.Identifier
import terminodiff.i18n.LocalizedStrings

typealias ResultPair = Pair<MetadataDiff.MetadataComparisonResult, (LocalizedStrings.() -> String)?>
typealias StringResultPair = Pair<MetadataDiff.MetadataComparisonResult, String?>

class MetadataDiff(left: CodeSystem, right: CodeSystem, localizedStrings: LocalizedStrings) {

    private val comparisonDefinitions by derivedStateOf { generateComparisonDefinitions(localizedStrings) }

    val comparisons by derivedStateOf { runComparisons(left, right, localizedStrings, comparisonDefinitions) }

    private fun runComparisons(
        left: CodeSystem,
        right: CodeSystem,
        localizedStrings: LocalizedStrings,
        comparisonDefinitions: List<MetadataDiffItem>
    ): List<MetadataComparison> = comparisonDefinitions.map {
        val (result, explanation) = it.compare(left, right)
        MetadataComparison(it, result, explanation, localizedStrings)
    }

    private fun generateComparisonDefinitions(localizedStrings: LocalizedStrings) =
        listOf(StringComparisonItem({ id }, true, localizedStrings) { it.id },
            StringComparisonItem({ canonicalUrl }, false, localizedStrings) { it.url },
            IdentifierDiffItem({ identifiers }, localizedStrings),
            StringComparisonItem({ version }, true, localizedStrings) { it.version },
            StringComparisonItem({ name }, false, localizedStrings) { it.name },
            StringComparisonItem({ title }, false, localizedStrings) { it.title },
            StringComparisonItem({ status }, false, localizedStrings) { it.status.toString() },
            StringComparisonItem(
                { experimental },
                false,
                localizedStrings
            ) { localizedStrings.boolean_(it.experimental) },
            StringComparisonItem({ date }, true, localizedStrings) { it.date?.toString() },
            StringComparisonItem({ publisher }, false, localizedStrings) { it.publisher?.toString() },
            ContactComparisonItem({ contact }, localizedStrings),
            StringComparisonItem({ description }, false, localizedStrings) { it.description },
            StringComparisonItem({ purpose }, false, localizedStrings) { it.purpose },
            StringComparisonItem({ copyright }, false, localizedStrings) { it.copyright },
            StringComparisonItem(
                { caseSensitive }, false, localizedStrings
            ) { localizedStrings.boolean_(it.caseSensitive) },
            StringComparisonItem({ valueSet }, false, localizedStrings) { it.valueSet },
            StringComparisonItem({ hierarchyMeaning }, false, localizedStrings) { it.hierarchyMeaning.display },
            StringComparisonItem(
                { compositional },
                false,
                localizedStrings
            ) { localizedStrings.boolean_(it.compositional) })

    data class MetadataComparison(
        val diffItem: MetadataDiffItem,
        val result: MetadataComparisonResult,
        val explanation: (LocalizedStrings.() -> String)? = null,
        private val localizedStrings: LocalizedStrings
    )

    abstract class MetadataDiffItem(
        val label: LocalizedStrings.() -> String,
        val expectDifferences: Boolean,
        protected val localizedStrings: LocalizedStrings
    ) {
        abstract fun compare(
            left: CodeSystem, right: CodeSystem
        ): ResultPair

        abstract val renderDisplay: (CodeSystem) -> String?
    }

    enum class MetadataComparisonResult {
        IDENTICAL, DIFFERENT
    }

    class StringComparisonItem(
        label: LocalizedStrings.() -> String,
        expectDifferences: Boolean,
        localizedStrings: LocalizedStrings,
        private val instanceGetter: (CodeSystem) -> String?
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

    abstract class MetadataListDiffItem<Type, Key : Comparable<Key>, ComparisonValue : Comparable<ComparisonValue>>(
        label: LocalizedStrings.() -> String,
        expectDifferences: Boolean,
        localizedStrings: LocalizedStrings,
        private val instanceGetter: (CodeSystem) -> List<Type>,
    ) : MetadataDiffItem(label, expectDifferences, localizedStrings) {

        protected open fun compareItem(
            key: Key, l: ComparisonValue, r: ComparisonValue
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
                val joinedItems = if (instance.isEmpty()) null else instance.joinToString(
                    separator = "\n", limit = 2, transform = ::formatInstance
                )
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
                            numberDifferentReason_.invoke(
                                countDifferent, differentValues
                            )
                        }
                    }
                }
            }
        }
    }

    class IdentifierDiffItem(
        label: LocalizedStrings.() -> String,
        localizedStrings: LocalizedStrings,
    ) : MetadataListDiffItem<Identifier, String, String>(label, false, localizedStrings, { it.identifier }) {

        override fun getComparisonKey(value: Identifier): String = value.system ?: "null"
        override fun getComparisonValue(value: Identifier): String = formatInstance(value)

        override fun formatInstance(data: Identifier): String = StringBuilder("• ").apply {
            if (data.hasUse()) append("[${data.use.display}] ")
            if (data.hasSystem()) append("(${data.system}) ")
            if (data.hasValue()) append(data.value) else append("null")
        }.trim().toString()
    }

    class ContactComparisonItem(
        label: LocalizedStrings.() -> String, localizedStrings: LocalizedStrings
    ) : MetadataListDiffItem<ContactDetail, String, String>(label, false, localizedStrings, { it.contact }) {

        override fun getComparisonKey(value: ContactDetail): String = value.name ?: "null"

        override fun getComparisonValue(value: ContactDetail): String = formatInstance(value)

        override fun formatInstance(data: ContactDetail): String = StringBuilder("• ").apply {
            if (data.hasName()) append(data.name)
            if (data.hasTelecom()) {
                val telecom =
                    data.telecom.filterNotNull().joinToString(separator = "; ", limit = 2, transform = ::formatTelecom)
                append(": $telecom")
            }
        }.trim().toString()

        private fun formatTelecom(contact: ContactPoint): String = StringBuilder().apply {
            if (contact.hasUse()) append("[${contact.use.display}] ")
            if (contact.hasSystem()) append("(${contact.system.display}) ")
            if (contact.hasValue()) append(contact.value)
            if (contact.hasRank()) append(" @${contact.rank}")
        }.trim().toString()

    }

}