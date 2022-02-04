package terminodiff.terminodiff.engine.resources

import java.io.File
import java.net.URL

data class InputResource(
    val kind: Kind,
    val localFile: File? = null,
    val resourceUrl: URL? = null,
    val sourceFhirServerUrl: URL? = null
) {
    enum class Kind {
        FILE,
        FHIR_SERVER
    }
}