package terminodiff.engine.concepts

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem

class ConceptDiff {

    companion object {
        fun compareCodeSystemConcepts(fhirContext: FhirContext, cs1: CodeSystem, cs2: CodeSystem) : ConceptDiff {
            return ConceptDiff()
        }
    }
}