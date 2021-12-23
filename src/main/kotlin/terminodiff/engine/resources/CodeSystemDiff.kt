package terminodiff.engine.resources

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.metadata.MetadataDiff
import terminodiff.engine.metadata.MetadataDiffBuilder

/*
data class CodeSystemDiff(
    val metadata: MetadataDiff,
    val concepts: ConceptDiff,
)

class CodeSystemDiffBuilder(
    private val fhirContext: FhirContext,
    private val cs1: CodeSystem,
    private val cs2: CodeSystem
) {
    fun build(): CodeSystemDiff {
        val metadataDiff = MetadataDiffBuilder(fhirContext, cs1, cs2).build()
        val conceptDiff = ConceptDiff.compareConcept(fhirContext, cs1, cs2)
        return CodeSystemDiff(metadata = metadataDiff, concepts = conceptDiff)
    }
}*/
