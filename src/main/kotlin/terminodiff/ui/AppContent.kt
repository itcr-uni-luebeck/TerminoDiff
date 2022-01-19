package terminodiff.terminodiff.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import li.flor.nativejfilechooser.NativeJFileChooser
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.preferences.AppPreferences
import terminodiff.ui.AppIconResource
import terminodiff.ui.AppImageIcon
import terminodiff.ui.TerminoDiffTopAppBar
import terminodiff.ui.panes.conceptdiff.ConceptDiffPanel
import terminodiff.ui.panes.graph.ShowGraphsPanel
import terminodiff.ui.panes.metadatadiff.MetadataDiffPanel
import terminodiff.ui.theme.TerminoDiffTheme
import java.awt.Cursor
import java.io.File
import java.net.InetAddress
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private val logger: Logger = LoggerFactory.getLogger("TerminodiffAppContent")

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun TerminodiffAppContent(
    localizedStrings: LocalizedStrings,
    diffDataContainer: DiffDataContainer,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    splitPaneState: SplitPaneState,
) {
    val onLoadLeftFile: () -> Unit = {
        showLoadFileDialog(localizedStrings.loadLeftFile)?.let {
            diffDataContainer.leftFilename = it
        }
    }
    val onLoadRightFile: () -> Unit = {
        showLoadFileDialog(localizedStrings.loadRightFile)?.let {
            diffDataContainer.rightFilename = it
        }
    }

    val coroutineScope = rememberCoroutineScope()
    when (val hostname = InetAddress.getLocalHost().hostName.lowercase(Locale.getDefault())) {
        "joshua-athena-windows" ->
            coroutineScope.launch {
                diffDataContainer.leftFilename = File("C:\\Users\\jpwie\\repos\\TerminoDiff\\src\\main\\resources\\testresources\\oncotree_2020_10_01.json")
                diffDataContainer.rightFilename = File("C:\\Users\\jpwie\\repos\\TerminoDiff\\src\\main\\resources\\testresources\\oncotree_2021_11_02.json")
            }
        else -> logger.info("hostname: $hostname")
    }


    TerminodiffContentWindow(
        localizedStrings = localizedStrings,
        scrollState = scrollState,
        useDarkTheme = useDarkTheme,
        onLocaleChange = onLocaleChange,
        onChangeDarkTheme = onChangeDarkTheme,
        onLoadLeftFile = onLoadLeftFile,
        onLoadRightFile = onLoadRightFile,
        onReload = { diffDataContainer.reload() },
        diffDataContainer = diffDataContainer,
        splitPaneState = splitPaneState
    )
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun TerminodiffContentWindow(
    localizedStrings: LocalizedStrings,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    onLoadLeftFile: () -> Unit,
    onLoadRightFile: () -> Unit,
    onReload: () -> Unit,
    diffDataContainer: DiffDataContainer,
    splitPaneState: SplitPaneState
) {
    TerminoDiffTheme(useDarkTheme = useDarkTheme) {
        Scaffold(
            topBar = {
                TerminoDiffTopAppBar(
                    localizedStrings = localizedStrings,
                    onLocaleChange = onLocaleChange,
                    onLoadLeftFile = onLoadLeftFile,
                    onLoadRightFile = onLoadRightFile,
                    onChangeDarkTheme = onChangeDarkTheme,
                    onReload = onReload
                )
            },
            backgroundColor = MaterialTheme.colorScheme.background
        ) { scaffoldPadding ->
            when (diffDataContainer.leftCodeSystem != null && diffDataContainer.rightCodeSystem != null) {
                true -> ContainerInitializedContent(
                    modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    strings = localizedStrings,
                    useDarkTheme = useDarkTheme,
                    diffDataContainer = diffDataContainer,
                    splitPaneState = splitPaneState
                )
                false -> ContainerUninitializedContent(
                    modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    localizedStrings = localizedStrings,
                    leftFile = diffDataContainer.leftFilename,
                    rightFile = diffDataContainer.rightFilename,
                    onLoadLeftFile = onLoadLeftFile,
                    onLoadRightFile = onLoadRightFile,
                )
            }
        }
    }
}

@Composable
private fun ContainerUninitializedContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    localizedStrings: LocalizedStrings,
    onLoadLeftFile: () -> Unit,
    onLoadRightFile: () -> Unit,
    leftFile: File?,
    rightFile: File?,
) {
    Column(modifier.scrollable(scrollState, Orientation.Vertical)) {
        Card(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            elevation = 8.dp,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            val buttonColors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            Column(Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    localizedStrings.noDataLoadedTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row {
                    Button(
                        modifier = Modifier.padding(4.dp),
                        colors = buttonColors,
                        onClick = onLoadLeftFile,
                        elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        AppImageIcon(
                            relativePath = AppIconResource.icLoadLeftFile,
                            label = localizedStrings.loadLeftFile,
                            tint = buttonColors.contentColor(true).value
                        )
                        Text(localizedStrings.loadLeftFile)
                    }
                    Button(
                        modifier = Modifier.padding(4.dp),
                        onClick = onLoadRightFile,
                        colors = buttonColors,
                    ) {
                        AppImageIcon(
                            relativePath = AppIconResource.icLoadRightFile,
                            label = localizedStrings.loadRightFile,
                            tint = buttonColors.contentColor(true).value
                        )
                        Text(localizedStrings.loadRightFile)
                    }
                }
                when {
                    leftFile != null -> localizedStrings.leftFileOpenFilename_.invoke(leftFile)
                    rightFile != null -> localizedStrings.rightFileOpenFilename_.invoke(rightFile)
                    else -> null
                }?.let { openFilenameText ->
                    Text(
                        text = openFilenameText,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.cursorForHorizontalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
private fun ContainerInitializedContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    strings: LocalizedStrings,
    useDarkTheme: Boolean,
    diffDataContainer: DiffDataContainer,
    splitPaneState: SplitPaneState
) {
    Column(
        modifier = modifier.scrollable(scrollState, Orientation.Vertical),
    ) {
        ShowGraphsPanel(
            leftCs = diffDataContainer.leftCodeSystem!!,
            rightCs = diffDataContainer.rightCodeSystem!!,
            diffGraph = diffDataContainer.codeSystemDiff!!.differenceGraph,
            localizedStrings = strings,
            useDarkTheme = useDarkTheme,
        )
        VerticalSplitPane(splitPaneState = splitPaneState) {
            first(100.dp) {
                ConceptDiffPanel(
                    diffDataContainer = diffDataContainer,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme
                )
            }
            second(100.dp) {
                MetadataDiffPanel(
                    diffDataContainer = diffDataContainer,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme,
                )
            }
            splitter {
                visiblePart {
                    Box(Modifier.height(3.dp).fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary))
                }
                handle {
                    Box(
                        Modifier
                            .markAsHandle()
                            .cursorForHorizontalResize()
                            .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            .height(9.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }


    }
}

fun getFileChooser(title: String): JFileChooser {
    return when (SystemUtils.IS_OS_MAC) {
        // NativeJFileChooser hangs on Azul Zulu 11 + JavaFX on macOS 12.1 aarch64.
        // with Azul Zulu w/o JFX, currently the file browser does not work at all on a M1 MBA.
        // hence, the non-native file chooser is used instead, which is not *nearly* as nice,
        // but it seems to be much more stabl
        true -> JFileChooser(AppPreferences.fileBrowserDirectory)
        else -> NativeJFileChooser(AppPreferences.fileBrowserDirectory)
    }.apply {
        dialogTitle = title
        isAcceptAllFileFilterUsed = false
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+JSON (*.json)", "json", "JSON"))
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+XML (*.xml)", "xml", "XML"))
    }
}

fun showLoadFileDialog(title: String): File? = getFileChooser(title).let { chooser ->
    when (chooser.showOpenDialog(null)) {
        JFileChooser.CANCEL_OPTION -> null
        JFileChooser.APPROVE_OPTION -> {
            return@let chooser.selectedFile?.absoluteFile ?: return null
        }
        else -> null
    }
}