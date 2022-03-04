package terminodiff.ui.panes.loaddata.panes.fromserver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.i18n.LocalizedStrings
import terminodiff.preferences.AppPreferences
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.terminodiff.ui.panes.loaddata.panes.fromserver.VReadDialog
import terminodiff.terminodiff.ui.panes.loaddata.panes.fromserver.fromServerPaneColumnSpecs
import terminodiff.terminodiff.ui.util.LabeledTextField
import terminodiff.ui.AppIconResource
import terminodiff.ui.ImageRelativePath
import terminodiff.ui.LoadListener
import terminodiff.ui.MouseOverPopup
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable
import java.util.*

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

    val coroutineScope = rememberCoroutineScope()

    /**
     * https://developer.android.com/jetpack/compose/side-effects#producestate
     */
    val resourceListPair by produceState<Pair<Boolean, List<DownloadableCodeSystem>?>>(true to null, baseServerUrl) {
        value = true to null
        val list = listCodeSystems(baseServerUrl, ktorClient, fhirContext)
        value = false to list
    }
    val (isResourceListPending, resourceList) = resourceListPair
    FromServerScreen(
        localizedStrings = localizedStrings,
        baseServerUrl = baseServerUrl,
        onChangeBaseServerUrl = { newUrl ->
            baseServerUrl = newUrl
            AppPreferences.terminologyServerUrl = newUrl
        },
        ktorClient = ktorClient,
        coroutineScope = coroutineScope,
        isResourceListPending = isResourceListPending,
        resourceList = resourceList,
        fhirContext = fhirContext,
        onLoadLeftFile = onLoadLeft,
        onLoadRightFile = onLoadRight,
    )
}

fun urlBuilderWithProtocol(urlString: String) = urlString.trimEnd('/').let { trimUrl ->
    URLBuilder(when {
        trimUrl.startsWith("http") -> trimUrl
        else -> "https://$trimUrl" // add HTTP prefix so that URLs of the fashion http://localhost/termserver.example.local/fhir are not constructed...
    })
}

private suspend fun listCodeSystems(
    urlString: String,
    ktorClient: HttpClient,
    fhirContext: FhirContext,
): List<DownloadableCodeSystem>? = try {
    val codeSystemUrl = urlBuilderWithProtocol(urlString).apply {
        appendPathSegments("CodeSystem")
        parameters.append("_elements", "url,id,version,name,title,link,content")
    }.build()
    logger.debug("Requesting resource bundle from $codeSystemUrl")
    val list = retrieveBundleOfDownloadableResources(ktorClient, codeSystemUrl, fhirContext)
    list?.sortedBy { it.canonicalUrl }?.sortedBy { it.version }
} catch (e: Exception) {
    logger.info("Error requesting from FHIR Base $urlString: ${e.message}")
    null
}

suspend fun retrieveBundleOfDownloadableResources(
    ktorClient: HttpClient,
    initialUrl: Url,
    fhirContext: FhirContext,
): List<DownloadableCodeSystem>? {
    var nextUrl: Url? = URLBuilder(initialUrl).apply {
        parameters.append("_count", "256")
    }.build()
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
                            val id = when (entry.id) {
                                null -> cs.idElement.idPart
                                else -> entry.id
                            }
                            DownloadableCodeSystem(physicalUrl = entry.fullUrl,
                                canonicalUrl = cs.url,
                                id = id, //cs.idElement.idPart,
                                version = cs.version,
                                metaVersion = cs.meta.versionId,
                                lastChange = cs.meta.lastUpdated,
                                name = cs.name,
                                title = cs.title,
                                content = cs.content)
                        }
                        else -> null
                    }
                }
                resources.addAll(entries)
                logger.debug("Read a page of ${entries.size}, now read ${resources.size}")
                nextUrl = bundle.getLink("next")?.url?.let { Url(it) }
            } catch (e: DataFormatException) {
                return null
            }
        }
    }
    logger.info("Retrieved bundle with ${resources.count()} resources from $initialUrl")
    return resources.sortedBy { it.canonicalUrl }.sortedBy { it.version }
}

@Composable
fun FromServerScreen(
    localizedStrings: LocalizedStrings,
    baseServerUrl: String,
    onChangeBaseServerUrl: (String) -> Unit,
    coroutineScope: CoroutineScope,
    fhirContext: FhirContext,
    ktorClient: HttpClient,
    isResourceListPending: Boolean,
    resourceList: List<DownloadableCodeSystem>?,
    onLoadLeftFile: LoadListener,
    onLoadRightFile: LoadListener,
) = Column(modifier = Modifier.fillMaxSize()) {
    val trailingIconPair: Pair<ImageVector, String> by derivedStateOf {
        when {
            isResourceListPending -> Icons.Default.Pending to localizedStrings.pending
            resourceList == null -> Icons.Default.Cancel to localizedStrings.invalid
            else -> Icons.Default.CheckCircle to localizedStrings.valid
        }
    }
    val (trailingIcon, trailingIconDescription) = trailingIconPair
    val lazyListState = rememberLazyListState()
    var vReadResource: InputResource? by remember { mutableStateOf(null) }
    vReadResource?.let {
        VReadDialog(resource = it,
            ktorClient = ktorClient,
            fhirContext = fhirContext,
            coroutineScope = coroutineScope,
            localizedStrings = localizedStrings,
            onCloseReject = { vReadResource = null },
            onSelectLeft = onLoadLeftFile,
            onSelectRight = onLoadRightFile)
    }
    LabeledTextField(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        value = baseServerUrl,
        onValueChange = onChangeBaseServerUrl,
        labelText = localizedStrings.fhirTerminologyServer,
        trailingIconVector = trailingIcon,
        trailingIconDescription = trailingIconDescription)

    when {
        isResourceListPending -> Row(Modifier.fillMaxWidth().weight(0.5f), horizontalArrangement = Arrangement.Center) {
            CircularProgressIndicator(Modifier.fillMaxHeight(0.75f).padding(16.dp), colorScheme.onPrimaryContainer)
        }
        resourceList != null -> {
            logger.debug("resource list (${resourceList.size}): ${resourceList.joinToString(limit = 3)}")
            ListOfResources(resourceList = resourceList,
                lazyListState = lazyListState,
                localizedStrings = localizedStrings,
                baseServerUrl = baseServerUrl,
                coroutineScope = coroutineScope,
                ktorClient = ktorClient,
                onLoadLeftFile = onLoadLeftFile,
                onLoadRightFile = onLoadRightFile,
                onShowVReadDialog = { vReadResource = it })
        }
    }
}

@Composable
fun ListOfResources(
    resourceList: List<DownloadableCodeSystem>,
    lazyListState: LazyListState,
    localizedStrings: LocalizedStrings,
    baseServerUrl: String,
    coroutineScope: CoroutineScope,
    ktorClient: HttpClient,
    onLoadLeftFile: LoadListener,
    onLoadRightFile: LoadListener,
    onShowVReadDialog: (InputResource) -> Unit,
) {
    var selectedItem: DownloadableCodeSystem? by remember { mutableStateOf(null) }
    var isDownloadingCurrently by remember { mutableStateOf(false) }
    val columnSpecs: List<ColumnSpec<DownloadableCodeSystem>> by derivedStateOf {
        fromServerPaneColumnSpecs(localizedStrings, selectedItem, onCheckedChange = {
            selectedItem = it
        })
    }

    @Composable
    fun leftRightButton(text: String, iconPath: ImageRelativePath, onLoadFile: (InputResource) -> Unit) = LoadButton(
        text = text,
        selectedItem = selectedItem,
        baseServerUrl = baseServerUrl,
        enabled = selectedItem != null && (!isDownloadingCurrently),
        iconImageVector = AppIconResource.loadXmlImageVector(iconPath)) {
        isDownloadingCurrently = true
        coroutineScope.launch {
            val downloaded = it.downloadRemoteFile(ktorClient)
            onLoadFile.invoke(downloaded)
            isDownloadingCurrently = false
        }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        leftRightButton(text = localizedStrings.loadLeft,
            iconPath = AppIconResource.icLoadLeftFile,
            onLoadFile = onLoadLeftFile)

        val vReadDisabled = (selectedItem?.metaVersion?.equals("1")) ?: true
        LoadButton(text = localizedStrings.vread,
            selectedItem = selectedItem,
            baseServerUrl = baseServerUrl,
            iconImageVector = Icons.Default.Compare,
            enabled = !vReadDisabled,
            tooltip = localizedStrings.vreadExplanationEnabled_.invoke(selectedItem != null && !vReadDisabled),
            onClick = onShowVReadDialog)

        leftRightButton(text = localizedStrings.loadRight,
            iconPath = AppIconResource.icLoadRightFile,
            onLoadFile = onLoadRightFile)
    }
    LazyTable(
        //modifier = Modifier.padding(4.dp),
        columnSpecs = columnSpecs,
        backgroundColor = colorScheme.tertiaryContainer,
        foregroundColor = colorScheme.onTertiaryContainer,
        lazyListState = lazyListState,
        zebraStripingColor = colorScheme.primaryContainer,
        tableData = resourceList,
        localizedStrings = localizedStrings,
        keyFun = DownloadableCodeSystem::id,
    )
}

@Composable
private fun LoadButton(
    text: String,
    selectedItem: DownloadableCodeSystem?,
    baseServerUrl: String,
    enabled: Boolean,
    iconImageVector: ImageVector,
    tooltip: String? = null,
    onClick: (InputResource) -> Unit,
) {
    val buttonColors =
        ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
    MouseOverPopup(text = tooltip ?: text) {
        Button(colors = buttonColors, onClick = {
            selectedItem?.let { item ->
                val resource = InputResource(kind = InputResource.Kind.FHIR_SERVER,
                    resourceUrl = item.physicalUrl,
                    sourceFhirServerUrl = baseServerUrl,
                    downloadableCodeSystem = item)
                onClick.invoke(resource)
            }
        }, enabled = enabled) {
            Icon(imageVector = iconImageVector,
                contentDescription = text,
                tint = buttonColors.contentColor(enabled).value)
            Text(text, color = buttonColors.contentColor(enabled).value)
        }
    }
}

data class DownloadableCodeSystem(
    val id: String,
    val physicalUrl: String,
    val canonicalUrl: String?,
    val version: String?,
    val metaVersion: String?,
    val name: String?,
    val title: String?,
    val content: CodeSystem.CodeSystemContentMode?,
    val lastChange: Date?,
)