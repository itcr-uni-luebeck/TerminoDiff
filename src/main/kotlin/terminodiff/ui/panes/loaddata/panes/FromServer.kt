package terminodiff.terminodiff.ui.panes.loaddata.panes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.DataFormatException
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.i18n.LocalizedStrings
import terminodiff.preferences.AppPreferences

private val logger: Logger = LoggerFactory.getLogger("FromServerScreen")

@Composable
fun FromServerScreenWrapper(
    localizedStrings: LocalizedStrings,
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
    fhirContext: FhirContext,
) {
    var baseServerUrl: String by remember { mutableStateOf(AppPreferences.terminologyServerUrl) }

    val ktorClient = remember {
        HttpClient(CIO) {
            expectSuccess = false
            followRedirects = true
        }
    }

    /**
     * https://developer.android.com/jetpack/compose/side-effects#producestate
     */
    /*val (resourceListPending: Boolean, resourceList: List<DownloadableCodeSystem>?) by
    produceState<Pair<Boolean,List<DownloadableCodeSystem>?>(null,
        baseServerUrl) {
        val list = listCodeSystems(baseServerUrl, ktorClient, fhirContext)
        value = list
    }*/
    val resourceListPair by produceState<Pair<Boolean, List<DownloadableCodeSystem>?>>(true to null, baseServerUrl) {
        value = true to null
        val list = listCodeSystems(baseServerUrl, ktorClient, fhirContext)
        value = false to list
    }
    val (isResourceListPending, resourceList) = resourceListPair
    FromServerScreen(localizedStrings = localizedStrings,
        baseServerUrl = baseServerUrl,
        onChangeBaseServerUrl = { newUrl ->
            baseServerUrl = newUrl
            AppPreferences.terminologyServerUrl = newUrl
        },
        isResourceListPending = isResourceListPending,
        resourceList = resourceList,
        onLoadLeftFile = onLoadLeft,
        onLoadRightFile = onLoadRight)
}

private suspend fun listCodeSystems(
    urlString: String,
    ktorClient: HttpClient,
    fhirContext: FhirContext,
): List<DownloadableCodeSystem>? = try {
    val codeSystemUrl = URLBuilder(urlString.trimEnd('/')).apply {
        appendPathSegments("CodeSystem")
        parameters.append("_elements", "url,id,version,name,title,link,content")
    }.build()
    logger.debug("Requesting resource bundle from $codeSystemUrl")
    val list = retrieveBundleOfDownloadableResources(ktorClient, codeSystemUrl, fhirContext)
    list?.sortedBy { it.canonicalUrl }?.sortedBy { it.version }
} catch (e: Exception) {
    logger.info("Error reqesting from FHIR Base $urlString: ${e.message}")
    null
}

private suspend fun retrieveBundleOfDownloadableResources(
    ktorClient: HttpClient,
    initialUrl: Url,
    fhirContext: FhirContext,
): List<DownloadableCodeSystem>? {
    var nextUrl: Url? = initialUrl
    val resources = mutableListOf<DownloadableCodeSystem>()
    while (nextUrl != null) {
        val thisUrl = nextUrl
        val bundleRx = ktorClient.get {
            url(thisUrl)
            headers {
                append("Accept", "application/json")
                append("Cache-Control", "max-age=30")
            }
        }
        if (!bundleRx.status.isSuccess()) {
            logger.debug("GET rx to $thisUrl not successful: ${bundleRx.status}")
            return null
        } else {
            try {
                val bundle = fhirContext.newJsonParser().parseResource(Bundle::class.java, bundleRx.bodyAsText())
                val entries: List<DownloadableCodeSystem> = bundle.entry.mapNotNull { entry ->
                    val resource = entry?.resource
                    when (resource?.resourceType?.name) {
                        "CodeSystem" -> {
                            val cs = resource as CodeSystem
                            DownloadableCodeSystem(
                                physicalUrl = entry.fullUrl,
                                canonicalUrl = cs.url,
                                id = cs.id,
                                version = cs.version,
                                metaVersion = cs.meta.versionId,
                                name = cs.name,
                                title = cs.title,
                                content = cs.content
                            )
                        }
                        else -> null
                    }
                }
                resources.addAll(entries)
                nextUrl = bundle.getLink("next")?.url?.let { Url(it) }
            } catch (e: DataFormatException) {
                return null
            }
        }
    }
    logger.info("Retrieved bundle with ${resources.count()} from $initialUrl")
    return resources.sortedBy { it.canonicalUrl }.sortedBy { it.version }
}

@Composable
fun FromServerScreen(
    localizedStrings: LocalizedStrings,
    baseServerUrl: String,
    onChangeBaseServerUrl: (String) -> Unit,
    isResourceListPending: Boolean,
    resourceList: List<DownloadableCodeSystem>?,
    onLoadLeftFile: LoadListener,
    onLoadRightFile: LoadListener,
) = Column(modifier = Modifier.fillMaxSize()) {
    resourceList?.let {
        logger.debug("resource list (${it.size}): ${it.joinToString(limit = 3)}")
    } ?: logger.debug("null resource list")
    val trailingIconPair : Pair<ImageVector, String> by derivedStateOf {
        when {
            isResourceListPending -> Icons.Default.Pending to localizedStrings.pending
            resourceList == null -> Icons.Default.Cancel to localizedStrings.invalid
            else -> Icons.Default.CheckCircle to localizedStrings.valid
        }
    }
    val (trailingIcon, trailingIconDescription) = trailingIconPair
    LabeledTextField(
        value = baseServerUrl,
        onValueChange = onChangeBaseServerUrl,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        labelText = localizedStrings.fhirTerminologyServer,
        trailingIconVector = trailingIcon,
        trailingIconDescription = trailingIconDescription
    )
}

data class DownloadableCodeSystem(
    val physicalUrl: String,
    val canonicalUrl: String,
    val id: String,
    val version: String?,
    val metaVersion: String?,
    val name: String?,
    val title: String?,
    val content: CodeSystem.CodeSystemContentMode,
)