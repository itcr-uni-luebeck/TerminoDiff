package terminodiff.engine.resources

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.metadata.MetadataDiff
import terminodiff.engine.metadata.MetadataDiffBuilder

class CodeSystemDiff(
    val metadata: MetadataDiff,
    val concepts: ConceptDiff,
) {

}

class CodeSystemDiffBuilder(
    val fhirContext: FhirContext,
    val cs1: CodeSystem,
    val cs2: CodeSystem
) {
    fun build(): CodeSystemDiff {
        val metadataDiff = MetadataDiffBuilder(fhirContext, cs1, cs2).build()
        val conceptDiff = ConceptDiff.compareCodeSystemConcepts(fhirContext, cs1, cs2)
        return CodeSystemDiff(metadata = metadataDiff, concepts = conceptDiff)
    }
}