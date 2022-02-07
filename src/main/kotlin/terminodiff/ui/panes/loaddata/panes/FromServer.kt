package terminodiff.terminodiff.ui.panes.loaddata.panes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.DataFormatException
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
    val cleanServerUrl: Url by derivedStateOf { URLBuilder(baseServerUrl.trimEnd('/')).build() }
    val coroutineScope = rememberCoroutineScope()
    val ktorClient = remember {
        HttpClient(CIO) {
            expectSuccess = false
            followRedirects = true
        }
    }
    val availableResources : List<DownloadableCodeSystem>? by derivedStateOf { listResources() }
    val remoteResourceList: MutableList<DownloadableCodeSystem> = remember { mutableStateListOf() }
    FromServerScreen(localizedStrings = localizedStrings,
        baseServerUrl = baseServerUrl,
        onChangeBaseServerUrl = { newUrl ->
            baseServerUrl = newUrl
            AppPreferences.terminologyServerUrl = newUrl
            listResources(
                newUrl = newUrl, coroutineScope = coroutineScope, ktorClient = ktorClient,
                fhirContext = fhirContext)
            { foundResources ->
                remoteResourceList.clear()
                remoteResourceList.addAll(foundResources)
            }
        },
        onLoadLeftFile = onLoadLeft,
        onLoadRightFile = onLoadRight)
}

/*private fun listResources(
    newUrl: String,
    coroutineScope: CoroutineScope,
    ktorClient: HttpClient,
    fhirContext: FhirContext,
    onList: (List<DownloadableCodeSystem>) -> Unit,
) {
    val codeSystemUrl = URLBuilder(newUrl.trimEnd('/')).apply {
        appendPathSegments("CodeSystem")
        parameters.append("_elements", "url,id,version,name,title,link,content")
    }.build()
    logger.debug("Requesting resource bundle from $codeSystemUrl")
    coroutineScope.launch {
        val list = retrieveBundleOfDownloadableResources(ktorClient, codeSystemUrl, fhirContext)
        onList.invoke(list.sortedBy { it.canonicalUrl }.sortedBy { it.version })
    }
}*/

private fun listResources(
    newUrl: String,
    coroutineScope: CoroutineScope,
    ktorClient: HttpClient,
    fhirContext: FhirContext,
    onList: (List<DownloadableCodeSystem>) -> Unit,
)  : List<DownloadableCodeSystem>? {
    val codeSystemUrl = URLBuilder(newUrl.trimEnd('/')).apply {
        appendPathSegments("CodeSystem")
        parameters.append("_elements", "url,id,version,name,title,link,content")
    }.build()
    logger.debug("Requesting resource bundle from $codeSystemUrl")
    val job = coroutineScope.launch {
        val list = retrieveBundleOfDownloadableResources(ktorClient, codeSystemUrl, fhirContext)
        onList.invoke(list.sortedBy { it.canonicalUrl }.sortedBy { it.version })
        return@launch list // TODO: 07/02/22 this does not work
    }
}

private suspend fun retrieveBundleOfDownloadableResources(
    ktorClient: HttpClient,
    initialUrl: Url,
    fhirContext: FhirContext,
): List<DownloadableCodeSystem> {
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
            logger.info("GET rx to $thisUrl not successful: ${bundleRx.status}")
            return emptyList()
        } else {
            logger.info(bundleRx.status.toString())
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
                return emptyList()
            }
        }
    }
    logger.info("Retrieved bundle from $initialUrl; ${resources.count()} resources")
    return resources
}

@Composable
fun FromServerScreen(
    localizedStrings: LocalizedStrings,
    baseServerUrl: String,
    onChangeBaseServerUrl: (String) -> Unit,
    onLoadLeftFile: LoadListener,
    onLoadRightFile: LoadListener,
) = Column(modifier = Modifier.fillMaxSize()) {
    /*TextField(
        value = baseServerUrl,
        onValueChange = onChangeBaseServerUrl,
        label = {
            Text(localizedStrings.fhirTerminologyServer, color = colorScheme.onSecondaryContainer.copy(0.75f))
        }
    )*/
    LabeledTextField(
        value = baseServerUrl,
        onValueChange = onChangeBaseServerUrl,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        labelText = localizedStrings.fhirTerminologyServer,
    )
}

data class DownloadableCodeSystem(
    val physicalUrl: String,
    val canonicalUrl: String,
    val id: String,
    val version: String?,
    val name: String?,
    val title: String?,
    val content: CodeSystem.CodeSystemContentMode,
)