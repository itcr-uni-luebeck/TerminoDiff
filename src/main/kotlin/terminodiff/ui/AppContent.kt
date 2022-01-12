package terminodiff.terminodiff.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import li.flor.nativejfilechooser.NativeJFileChooser
import org.apache.commons.lang3.SystemUtils
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
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun TerminodiffAppContent(
    localizedStrings: LocalizedStrings,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit
) {
    val fhirContext = remember { FhirContext.forR4() }
    val diffDataContainer = remember { DiffDataContainer(fhirContext, localizedStrings) }
    val onLoadLeftFile: () -> Unit = {
        diffDataContainer.leftFilename = showLoadFileDialog(localizedStrings.loadLeftFile)
    }
    val onLoadRightFile: () -> Unit = {
        diffDataContainer.rightFilename = showLoadFileDialog(localizedStrings.loadRightFile)
    }

    TerminodiffContentWindow(
        localizedStrings = localizedStrings,
        scrollState = scrollState,
        useDarkTheme = useDarkTheme,
        onLocaleChange = onLocaleChange,
        onChangeDarkTheme = onChangeDarkTheme,
        fhirContext = fhirContext,
        onLoadLeftFile = onLoadLeftFile,
        onLoadRightFile = onLoadRightFile,
        diffDataContainer = diffDataContainer
    )
}

@Composable
fun TerminodiffContentWindow(
    localizedStrings: LocalizedStrings,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    fhirContext: FhirContext,
    onLoadLeftFile: () -> Unit,
    onLoadRightFile: () -> Unit,
    diffDataContainer: DiffDataContainer
) {

    TerminoDiffTheme(useDarkTheme = useDarkTheme) {
        Scaffold(
            topBar = {
                TerminoDiffTopAppBar(
                    localizedStrings = localizedStrings,
                    onLocaleChange = onLocaleChange,
                    onLoadLeftFile = onLoadLeftFile,
                    onLoadRightFile = onLoadRightFile,
                    onChangeDarkTheme = onChangeDarkTheme
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
                    fhirContext = fhirContext,
                    diffDataContainer = diffDataContainer
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

@Composable
private fun ContainerInitializedContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    strings: LocalizedStrings,
    useDarkTheme: Boolean,
    fhirContext: FhirContext,
    diffDataContainer: DiffDataContainer
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
        ConceptDiffPanel(
            verticalWeight = 0.45f,
            diffDataContainer = diffDataContainer,
            localizedStrings = strings,
            useDarkTheme = useDarkTheme
        )
        MetadataDiffPanel(
            diffDataContainer = diffDataContainer,
            localizedStrings = strings,
            useDarkTheme = useDarkTheme,
        )
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