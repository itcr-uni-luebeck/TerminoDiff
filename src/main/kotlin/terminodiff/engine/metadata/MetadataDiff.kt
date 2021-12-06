package terminodiff.engine.metadata

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.CodeableConcept
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.defaultStrings

data class MetadataDiff(
    val diffResults: List<MetadataComparisonResult>
) {

    data class MetadataComparisonResult(
        val diffItem: MetadataDiffItem<*>,
        val result: MetadataDiffItemResult
    ) {
        val expected get() = diffItem.expectDifferences
    }

    abstract class MetadataDiffItem<T>(
        val label: LocalizedStrings.() -> String,
        val instanceGetter: CodeSystem.() -> T?,
        val expectDifferences: Boolean = false
    ) {
        abstract fun compareNonNull(left: T, right: T): MetadataDiffItemResult

        fun compare(cs1: CodeSystem, cs2: CodeSystem): MetadataComparisonResult {
            val left: T? = instanceGetter.invoke(cs1)
            val right: T? = instanceGetter.invoke(cs2)
            @Suppress("KotlinConstantConditions") val result = when {
                left == null && right == null -> MetadataDiffItemResult.BOTH_NULL
                (left != null && right == null) || (left == null && right != null) -> MetadataDiffItemResult.DIFFERENT
                else -> compareNonNull(left!!, right!!)
            }
            return MetadataComparisonResult(
                diffItem = this, result = result
            )
        }

        override fun toString(): String {
            return "MetadataDiffItem(label='${defaultStrings.label()}', expectDifferences=$expectDifferences)"
        }
    }

    class MetadataStringDiffItem(
        label: LocalizedStrings.() -> String,
        instanceGetter: CodeSystem.() -> String?,
        expectDifferences: Boolean = false
    ) : MetadataDiffItem<String>(label, instanceGetter, expectDifferences) {
        override fun compareNonNull(left: String, right: String): MetadataDiffItemResult = when (left) {
            right -> MetadataDiffItemResult.IDENTICAL
            else -> MetadataDiffItemResult.DIFFERENT
        }
    }

    class MetadataListDiffItem(
        label: LocalizedStrings.() -> String,
        instanceGetter: CodeSystem.() -> List<*>?,
        expectDifferences: Boolean = false
    ) : MetadataDiffItem<List<*>>(label, instanceGetter, expectDifferences) {
        override fun compareNonNull(left: List<*>, right: List<*>): MetadataDiffItemResult = when {
            left.isEmpty() && right.isEmpty() -> MetadataDiffItemResult.BOTH_EMPTY
            left.size != right.size -> MetadataDiffItemResult.DIFFERENT_COUNT
            left.zip(right).map { (v1, v2) ->
                if (v1 == null && v2 == null) true else v1 == v2
            }
                .any { !it } -> MetadataDiffItemResult.DIFFERENT
            else -> MetadataDiffItemResult.IDENTICAL
        }
    }

    class MetadataCodeableConceptDiffItem(
        label: LocalizedStrings.() -> String,
        instanceGetter: CodeSystem.() -> List<CodeableConcept>?,
        expectDifferences: Boolean = false
    ) : MetadataDiffItem<List<CodeableConcept>>(label, instanceGetter, expectDifferences) {
        override fun compareNonNull(
            left: List<CodeableConcept>,
            right: List<CodeableConcept>
        ): MetadataDiffItemResult {
            //TODO()
            return MetadataDiffItemResult.DIFFERENT
        }

    }

    enum class MetadataDiffItemResult {
        // TODO: 06/12/2021 this should be reduced to identical, different, with an explanation text
        // (which comes from LocalizedStrings, e.g.: `DIFFERENT("the number of elements is different")`
        // or `DIFFERENT("element foo is not in left CodeSystem")
        IDENTICAL,
        DIFFERENT,
        BOTH_NULL,
        BOTH_EMPTY,
        DIFFERENT_COUNT,
        DIFFERENT_TEXT
    }

    companion object {
        fun generateDiffItems(@Suppress("UNUSED_PARAMETER") fhirContext: FhirContext) = listOf(
            MetadataStringDiffItem({ id }, { id }, true),
            MetadataStringDiffItem({ canonicalUrl }, { url }),
            MetadataListDiffItem({ identifiers }, { identifier }),
            MetadataStringDiffItem({ version }, { version }, expectDifferences = true),
            MetadataStringDiffItem({ name }, { name }),
            MetadataStringDiffItem({ title }, { title }),
            MetadataStringDiffItem({ status }, { status.toString() }),
            MetadataStringDiffItem({ experimental }, { experimental.toString() }),
            MetadataStringDiffItem({ date }, { date?.toString() }),
            MetadataStringDiffItem({ publisher }, { publisher }),
            MetadataListDiffItem({ contact }, { contact }),
            MetadataStringDiffItem({ description }, { description }, true),
            // TODO: 06/12/2021 useContext
            MetadataCodeableConceptDiffItem({ jurisdiction }, { jurisdiction })
        )
    }

}

class MetadataDiffBuilder(val fhirContext: FhirContext, val left: CodeSystem, val right: CodeSystem) {
    fun build(): MetadataDiff {
        val results = MetadataDiff.generateDiffItems(fhirContext).map { item ->
            item.compare(left, right)
        }
        return MetadataDiff(results)
    }
}