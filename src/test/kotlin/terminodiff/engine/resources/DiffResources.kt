package terminodiff.engine.resources

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Identifier

const val baseFhirSystem = "https://fhir.example.org/"
const val identifierSystem = "$baseFhirSystem/Identifier"

class DiffResources {

    companion object {
        val fhirContext = FhirContext.forR4()

        val cs1 = CodeSystem().apply {
            url = "$baseFhirSystem/CodeSystem/example"
            title = "Example CodeSystem for TerminoloDiff version 1.0.0"
            version = "1.0.0"
            name = "example-code-system-terminologodiff"
            id = "ex-cs-terminologodiff-v1"
            status = Enumerations.PublicationStatus.ACTIVE
            experimental = true
            identifier = listOf(Identifier().apply {
                url = "$identifierSystem/1"
                value = "ID1"
            })
        }

        val cs2 = cs1.copy().apply {
            title = "Example CodeSystem for TerminoloDiff version 2.0.0"
            version = "2.0.0"
            id = "ex-cs-terminologodiff-v2"
            experimental = false
            identifier = listOf(Identifier().apply {
                url = "$identifierSystem/2"
                value = "ID2"
            })
        }
    }

}