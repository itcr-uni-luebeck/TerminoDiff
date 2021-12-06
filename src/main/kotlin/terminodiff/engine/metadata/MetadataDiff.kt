package terminodiff.engine.metadata

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem

class MetadataDiff {
}

class MetadataDiffBuilder(val fhirContext: FhirContext, val cs1: CodeSystem, val cs2: CodeSystem) {
    fun build() : MetadataDiff{
        return MetadataDiff()
    }
}