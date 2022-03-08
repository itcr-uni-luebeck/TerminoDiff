package terminodiff.terminodiff.ui.panes.loaddata.panes.fromserver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.terminodiff.ui.util.TerminodiffDialog
import terminodiff.ui.panes.loaddata.panes.fromserver.DownloadableCodeSystem
import terminodiff.ui.panes.loaddata.panes.fromserver.retrieveBundleOfDownloadableResources
import terminodiff.ui.panes.loaddata.panes.fromserver.urlBuilderWithProtocol
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable
import terminodiff.ui.util.SelectableText

private val logger: Logger = LoggerFactory.getLogger("VReadDialog")

@Composable
fun VReadDialog(
    resource: InputResource,
    ktorClient: HttpClient,
    coroutineScope: CoroutineScope,
    fhirContext: FhirContext,
    localizedStrings: LocalizedStrings,
    onCloseReject: () -> Unit,
    onSelectLeft: (InputResource) -> Unit,
    onSelectRight: (InputResource) -> Unit,
) {

    val vReadVersions: List<DownloadableCodeSystem>? by produceState<List<DownloadableCodeSystem>?>(null, resource) {
        withContext(Dispatchers.IO) {
            Thread.sleep(1000)
        }
        val historyUrl = buildHistoryUrl(resource)
        val bundle = retrieveBundleOfDownloadableResources(ktorClient, historyUrl, fhirContext)
        bundle?.let {
            logger.info("Retrieved bundle with ${bundle.size} versions from $historyUrl")
        } ?: logger.info("Error retrieving bundle from $historyUrl")
        value = bundle?.sortedByDescending { it.metaVersion }
    }

    val lazyListState = rememberLazyListState()
    var leftSelection: DownloadableCodeSystem? by remember { mutableStateOf(null) }
    var rightSelection: DownloadableCodeSystem? by remember { mutableStateOf(null) }
    val onCloseAccept: () -> Unit = {
        leftSelection?.let { invokeLoadListener(onSelectLeft, it, resource, coroutineScope, ktorClient) }
        rightSelection?.let { invokeLoadListener(onSelectRight, it, resource, coroutineScope, ktorClient) }
        onCloseReject()
    }
    TerminodiffDialog(title = localizedStrings.vReadFor_(resource), onCloseRequest = onCloseReject) {
        when {
            vReadVersions == null -> {
                Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.fillMaxSize(0.75f), colorScheme.primary)
                }
            }
            vReadVersions!!.isEmpty() -> Text(text = localizedStrings.anUnknownErrorOccurred,
                style = typography.titleMedium)
            else -> {
                VReadTable(modifier = Modifier.weight(0.9f),
                    vReadVersions = vReadVersions!!,
                    lazyListState = lazyListState,
                    localizedStrings = localizedStrings,
                    leftSelection = leftSelection,
                    rightSelection = rightSelection,
                    onSelectLeft = { leftSelection = it },
                    onSelectRight = { rightSelection = it })
                Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    OutlinedButton(modifier = Modifier.wrapContentSize(),
                        onClick = onCloseReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.onSurfaceVariant)) {
                        Text(localizedStrings.closeReject)
                    }
                    Button(modifier = Modifier.wrapContentSize(),
                        onClick = onCloseAccept,
                        colors = ButtonDefaults.buttonColors(colorScheme.secondary, colorScheme.onSecondary),
                        enabled = listOf(leftSelection, rightSelection).any { it != null }) {
                        Text(localizedStrings.closeAccept, color = colorScheme.onSecondary)
                    }
                }

            }
        }
    }
}

fun invokeLoadListener(
    onSelect: (InputResource) -> Unit,
    downloadableCodeSystem: DownloadableCodeSystem,
    resource: InputResource,
    coroutineScope: CoroutineScope,
    ktorClient: HttpClient,
) {
    val physicalUrl = URLBuilder(buildHistoryUrl(resource)).apply {
        appendPathSegments(downloadableCodeSystem.metaVersion!!) // ok if this crashes due to metaVersion == null, because that should never happen ;)
    }.build()
    val inputResource = InputResource(
        kind = InputResource.Kind.VREAD,
        resourceUrl = physicalUrl.toString(),
        downloadableCodeSystem = downloadableCodeSystem,
        sourceFhirServerUrl = resource.sourceFhirServerUrl,
    )
    coroutineScope.launch {
        val downloaded = inputResource.downloadRemoteFile(ktorClient)
        onSelect.invoke(downloaded)
    }
}

@Composable
fun VReadTable(
    modifier: Modifier,
    vReadVersions: List<DownloadableCodeSystem>,
    lazyListState: LazyListState,
    localizedStrings: LocalizedStrings,
    leftSelection: DownloadableCodeSystem?,
    rightSelection: DownloadableCodeSystem?,
    onSelectLeft: (DownloadableCodeSystem) -> Unit,
    onSelectRight: (DownloadableCodeSystem) -> Unit,
) {
    LazyTable(
        modifier = modifier.padding(8.dp),
        columnSpecs = columnSpecs(localizedStrings, leftSelection, rightSelection, onSelectLeft, onSelectRight),
        backgroundColor = colorScheme.surfaceVariant,
        lazyListState = lazyListState,
        zebraStripingColor = colorScheme.secondaryContainer,
        tableData = vReadVersions,
        localizedStrings = localizedStrings,
        keyFun = DownloadableCodeSystem::metaVersion,
    )
}

private fun buildHistoryUrl(resource: InputResource): Url = when {
    resource.downloadableCodeSystem == null -> throw UnsupportedOperationException("Can not retrieve VRead for a resource without reference to CS")
    resource.sourceFhirServerUrl == null -> throw UnsupportedOperationException("Can not retrieve VRead for a resource without FHIR base")
    else -> urlBuilderWithProtocol(resource.sourceFhirServerUrl).apply {
        appendPathSegments("CodeSystem", resource.downloadableCodeSystem.id, "_history")
    }.build()
}

private fun columnSpecs(
    localizedStrings: LocalizedStrings,
    leftSelection: DownloadableCodeSystem?,
    rightSelection: DownloadableCodeSystem?,
    onSelectLeft: (DownloadableCodeSystem) -> Unit,
    onSelectRight: (DownloadableCodeSystem) -> Unit,
) = listOf(metaVersionColumn(localizedStrings),
    lastUpdateVersionColumn(localizedStrings),
    selectColumn(localizedStrings.loadLeft, leftSelection, onSelectLeft),
    selectColumn(localizedStrings.loadRight, rightSelection, onSelectRight))

private fun metaVersionColumn(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.metaVersion, weight = 0.1f) {
        SelectableText(it.metaVersion)
    }

private fun lastUpdateVersionColumn(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.metaVersion, weight = 0.25f) {
        SelectableText(it.lastChange.toString())
    }

private fun selectColumn(
    title: String,
    selection: DownloadableCodeSystem?,
    onSelect: (DownloadableCodeSystem) -> Unit,
) = ColumnSpec<DownloadableCodeSystem>(title = title, weight = 0.1f) {
    RadioButton(selected = selection == it, onClick = { onSelect.invoke(it) })
}