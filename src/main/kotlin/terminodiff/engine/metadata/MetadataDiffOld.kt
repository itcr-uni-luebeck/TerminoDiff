package terminodiff.engine.metadata

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.CodeableConcept
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.defaultStrings

data class MetadataDiffOld(
    val diffResults: List<MetadataComparisonResult>
) {

    data class MetadataComparisonResult(
        val diffItem: MetadataDiffItemOld<*>,
        val result: MetadataDiffItemResultOld
    ) {
        val expected get() = diffItem.expectDifferences
    }

    abstract class MetadataDiffItemOld<T>(
        val label: LocalizedStrings.() -> String,
        val instanceGetter: CodeSystem.() -> T?,
        val expectDifferences: Boolean = false
    ) {
        abstract fun compareNonNull(left: T, right: T): MetadataDiffItemResultOld

        fun compare(cs1: CodeSystem, cs2: CodeSystem): MetadataComparisonResult {
            val left: T? = instanceGetter.invoke(cs1)
            val right: T? = instanceGetter.invoke(cs2)
            @Suppress("KotlinConstantConditions") val result = when {
                left == null && right == null -> MetadataDiffItemResultOld.BOTH_NULL
                (left != null && right == null) || (left == null && right != null) -> MetadataDiffItemResultOld.DIFFERENT
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

    class MetadataStringDiffItemOld(
        label: LocalizedStrings.() -> String,
        instanceGetter: CodeSystem.() -> String?,
        expectDifferences: Boolean = false
    ) : MetadataDiffItemOld<String>(label, instanceGetter, expectDifferences) {
        override fun compareNonNull(left: String, right: String): MetadataDiffItemResultOld = when (left) {
            right -> MetadataDiffItemResultOld.IDENTICAL
            else -> MetadataDiffItemResultOld.DIFFERENT
        }
    }

    class MetadataListDiffItemOld(
        label: LocalizedStrings.() -> String,
        instanceGetter: CodeSystem.() -> List<*>?,
        expectDifferences: Boolean = false
    ) : MetadataDiffItemOld<List<*>>(label, instanceGetter, expectDifferences) {
        override fun compareNonNull(left: List<*>, right: List<*>): MetadataDiffItemResultOld = when {
            left.isEmpty() && right.isEmpty() -> MetadataDiffItemResultOld.BOTH_EMPTY
            left.size != right.size -> MetadataDiffItemResultOld.DIFFERENT_COUNT
            left.zip(right).map { (v1, v2) ->
                if (v1 == null && v2 == null) true else v1 == v2
            }
                .any { !it } -> MetadataDiffItemResultOld.DIFFERENT
            else -> MetadataDiffItemResultOld.IDENTICAL
        }
    }

    class MetadataCodeableConceptDiffItemOld(
        label: LocalizedStrings.() -> String,
        instanceGetter: CodeSystem.() -> List<CodeableConcept>?,
        expectDifferences: Boolean = false
    ) : MetadataDiffItemOld<List<CodeableConcept>>(label, instanceGetter, expectDifferences) {
        override fun compareNonNull(
            left: List<CodeableConcept>,
            right: List<CodeableConcept>
        ): MetadataDiffItemResultOld {
            //TODO()
            return MetadataDiffItemResultOld.DIFFERENT
        }

    }

    enum class MetadataDiffItemResultOld {
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
            MetadataStringDiffItemOld({ id }, { id }, true),
            MetadataStringDiffItemOld({ canonicalUrl }, { url }),
            MetadataListDiffItemOld({ identifiers }, { identifier }),
            MetadataStringDiffItemOld({ version }, { version }, expectDifferences = true),
            MetadataStringDiffItemOld({ name }, { name }),
            MetadataStringDiffItemOld({ title }, { title }),
            MetadataStringDiffItemOld({ status }, { status.toString() }),
            MetadataStringDiffItemOld({ experimental }, { experimental.toString() }),
            MetadataStringDiffItemOld({ date }, { date?.toString() }),
            MetadataStringDiffItemOld({ publisher }, { publisher }),
            MetadataListDiffItemOld({ contact }, { contact }),
            MetadataStringDiffItemOld({ description }, { description }, true),
            // TODO: 06/12/2021 useContext
            MetadataCodeableConceptDiffItemOld({ jurisdiction }, { jurisdiction })
        )
    }
}

class MetadataDiffBuilderOld(private val fhirContext: FhirContext, val left: CodeSystem, val right: CodeSystem) {
    fun build(): MetadataDiffOld {
        val results = MetadataDiffOld.generateDiffItems(fhirContext).map { item ->
            item.compare(left, right)
        }
        return MetadataDiffOld(results)
    }
}