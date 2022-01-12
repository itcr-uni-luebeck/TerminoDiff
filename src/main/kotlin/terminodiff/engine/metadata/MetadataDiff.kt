package terminodiff.terminodiff.engine.metadata


import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Identifier
import terminodiff.i18n.LocalizedStrings

typealias ResultPair = Pair<MetadataDiff.MetadataComparisonResult, (LocalizedStrings.() -> String)?>

class MetadataDiff(left: CodeSystem, right: CodeSystem, private val localizedStrings: LocalizedStrings) {

    val comparisons = runComparisons(left, right)

    private fun runComparisons(left: CodeSystem, right: CodeSystem): List<MetadataComparison> =
        generateComparisonDefinitions().map {
            val (result, explanation) = it.compare(left, right)
            MetadataComparison(it, result, explanation)
        }

    private fun generateComparisonDefinitions() = listOf<MetadataDiffItem>(
        StringComparisonItem({ id }, true) { it.id },
        StringComparisonItem({ canonicalUrl }, false) { it.url },
        IdentifierDiffItem({ identifiers }, localizedStrings = localizedStrings),
        StringComparisonItem({ version }, true) { it.version },
        StringComparisonItem({ name }, false) { it.name },
        StringComparisonItem({ title }, false) { it.title },
        StringComparisonItem({ status }, false) { it.status.toString() },
        StringComparisonItem({ experimental }, false) { it.experimental.toString() },
        StringComparisonItem({ date }, true) { it.date?.toString() },
        StringComparisonItem({ publisher }, false) { it.publisher?.toString() },
        StringComparisonItem({ description }, false) { it.description },
    )

    data class MetadataComparison(
        val diffItem: MetadataDiffItem,
        val result: MetadataComparisonResult,
        val explanation: (LocalizedStrings.() -> String)? = null
    )

    abstract class MetadataDiffItem(
        val label: LocalizedStrings.() -> String,
        val expectDifferences: Boolean
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
        private val instanceGetter: (CodeSystem) -> String?
    ) :
        MetadataDiffItem(label, expectDifferences) {
        override val renderDisplay: (CodeSystem) -> String?
            get() = instanceGetter

        override fun compare(
            left: CodeSystem,
            right: CodeSystem
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

    abstract class MetadataListDiffItem<T>(
        label: LocalizedStrings.() -> String,
        expectDifferences: Boolean,
        private val instanceGetter: (CodeSystem) -> List<T>,
    ) : MetadataDiffItem(label, expectDifferences) {

        open fun deepCompare(leftValue: List<T>, rightValue: List<T>): ResultPair {
            val left = leftValue.associate { getComparisonKey(it) to getComparisonValue(it)}
            val right = rightValue.associate { getComparisonKey(it) to getComparisonValue(it)}
            return when {
                // TODO: 12/01/22  
                left.keys.toSet().containsAll(right.keys) -> {
                    MetadataComparisonResult.IDENTICAL to null
                }
                else -> MetadataComparisonResult.DIFFERENT to { differentValue }
            }
        }

        abstract fun getComparisonKey(value: T): String?
        abstract fun getComparisonValue(value: T): Comparable<*>

        override fun compare(
            left: CodeSystem,
            right: CodeSystem
        ): ResultPair {
            val leftValue = instanceGetter.invoke(left)
            val rightValue = instanceGetter.invoke(right)
            return when {
                leftValue.isEmpty() && rightValue.isEmpty() -> MetadataComparisonResult.IDENTICAL to { bothListsAreEmpty }
                else -> deepCompare(leftValue, rightValue)
            }
        }
    }

    class IdentifierDiffItem(
        label: LocalizedStrings.() -> String,
        private val localizedStrings: LocalizedStrings,
    ) : MetadataListDiffItem<Identifier>(label, false, { it.identifier }) {

        override fun getComparisonKey(value: Identifier): String? = value.system
        override fun getComparisonValue(value: Identifier): Comparable<*> = value.value

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