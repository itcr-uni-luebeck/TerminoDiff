package terminodiff.terminodiff.engine.resources

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.apache.http.HttpException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.io.path.bufferedWriter

private val logger: Logger = LoggerFactory.getLogger("InputResource")

data class InputResource(
    val kind: Kind,
    var localFile: File? = null,
    val resourceUrl: String? = null,
    val sourceFhirServerUrl: String? = null,
) {
    enum class Kind {
        FILE,
        FHIR_SERVER
    }

    suspend fun downloadRemoteFile(ktorClient: HttpClient): InputResource = when {
        kind == Kind.FILE -> this
        kind == Kind.FHIR_SERVER && resourceUrl != null -> {
            val tempFilePath = kotlin.io.path.createTempFile(prefix = "terminodiff", suffix = ".json")
            val rx = ktorClient.get(resourceUrl) {
                header("Accept", "application/json")
            }
            if (!rx.status.isSuccess()) throw HttpException("The resource $this could not be retrieved, error ${rx.status.value} ${rx.status.description}")
            tempFilePath.bufferedWriter().use {
                it.write(rx.bodyAsText())
            }
            this.copy(localFile = tempFilePath.toFile()).also {
                logger.info("Downloaded resource $it")
            }

        }
        else -> throw UnsupportedOperationException("The remote file can't be downloaded for input resource $this")
    }
}