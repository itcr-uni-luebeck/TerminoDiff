package terminodiff.terminodiff.engine.metadata


import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Identifier
import terminodiff.i18n.LocalizedStrings

typealias ResultPair = Pair<MetadataDiff.MetadataComparisonResult, (LocalizedStrings.() -> String)?>
typealias StringResultPair = Pair<MetadataDiff.MetadataComparisonResult, String?>

class MetadataDiff(left: CodeSystem, right: CodeSystem, localizedStrings: LocalizedStrings) {

    val comparisons = runComparisons(left, right, localizedStrings)

    private fun runComparisons(
        left: CodeSystem,
        right: CodeSystem,
        localizedStrings: LocalizedStrings
    ): List<MetadataComparison> =
        generateComparisonDefinitions(localizedStrings).map {
            val (result, explanation) = it.compare(left, right)
            MetadataComparison(it, result, explanation, localizedStrings)
        }

    private fun generateComparisonDefinitions(localizedStrings: LocalizedStrings) = listOf(
        StringComparisonItem({ id }, true, localizedStrings) { it.id },
        StringComparisonItem({ canonicalUrl }, false, localizedStrings) { it.url },
        IdentifierDiffItem({ identifiers }, localizedStrings),
        StringComparisonItem({ version }, true, localizedStrings) { it.version },
        StringComparisonItem({ name }, false, localizedStrings) { it.name },
        StringComparisonItem({ title }, false, localizedStrings) { it.title },
        StringComparisonItem({ status }, false, localizedStrings) { it.status.toString() },
        StringComparisonItem({ experimental }, false, localizedStrings) { it.experimental.toString() },
        StringComparisonItem({ date }, true, localizedStrings) { it.date?.toString() },
        StringComparisonItem({ publisher }, false, localizedStrings) { it.publisher?.toString() },
        StringComparisonItem({ description }, false, localizedStrings) { it.description },
    )

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
            left: CodeSystem,
            right: CodeSystem
        ): ResultPair

        abstract val renderDisplay: (CodeSystem) -> String?
    }

    enum class MetadataComparisonResult {
        IDENTICAL,
        DIFFERENT
    }

    class StringComparisonItem(
        label: LocalizedStrings.() -> String,
        expectDifferences: Boolean,
        localizedStrings: LocalizedStrings,
        private val instanceGetter: (CodeSystem) -> String?
    ) :
        MetadataDiffItem(label, expectDifferences, localizedStrings) {
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
            key: Key,
            l: ComparisonValue,
            r: ComparisonValue
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
                                countDifferent,
                                differentValues
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
        override fun getComparisonValue(value: Identifier): String = format(value)

        override val renderDisplay: (CodeSystem) -> String?
            get() = { codeSystem ->
                val count = localizedStrings.numberItems_.invoke(codeSystem.identifier.size)
                val joinedItems = if (codeSystem.identifier.isEmpty()) null else codeSystem.identifier.joinToString(
                    separator =
                    "\n", limit = 2, transform = ::format
                )
                joinedItems?.let { "$count: $it" } ?: count
            }

        private fun format(identifier: Identifier): String = StringBuilder("• ").apply {
            if (identifier.hasUse()) append("[${identifier.use.display}] ")
            if (identifier.hasSystem()) append("(${identifier.system}) ")
            if (identifier.hasValue()) append(identifier.value) else append("null")
        }.trim().toString()
    }


}