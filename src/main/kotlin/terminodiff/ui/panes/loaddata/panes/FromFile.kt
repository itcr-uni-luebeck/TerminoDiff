package terminodiff.terminodiff.ui.panes.loaddata.panes

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Plagiarism
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import li.flor.nativejfilechooser.NativeJFileChooser
import org.apache.commons.lang3.SystemUtils
import terminodiff.i18n.LocalizedStrings
import terminodiff.preferences.AppPreferences
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.terminodiff.ui.util.LabeledTextField
import terminodiff.ui.AppIconResource
import terminodiff.ui.AppImageIcon
import terminodiff.ui.LoadListener
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.invariantSeparatorsPathString


@Composable
fun FromFileScreenWrapper(
    localizedStrings: LocalizedStrings,
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
) {
    var selectedPath: String by remember { mutableStateOf("") }
    val selectedFile: File by derivedStateOf { File(selectedPath) }
    FromFileScreen(localizedStrings = localizedStrings,
        selectedFile = selectedFile,
        selectedPath = selectedPath,
        onChangeFilePath = {
            selectedPath = it ?: ""
        },
        onLoadLeftFile = onLoadLeft,
        onLoadRightFile = onLoadRight)
}

@Composable
private fun FromFileScreen(
    localizedStrings: LocalizedStrings,
    selectedFile: File?,
    onChangeFilePath: (String?) -> Unit,
    onLoadLeftFile: (InputResource) -> Unit,
    onLoadRightFile: (InputResource) -> Unit,
    selectedPath: String,
) = Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
    val buttonColors =
        ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary)
    val isValidPath by derivedStateOf {
        when {
            selectedFile == null -> false
            selectedFile.exists() -> true
            else -> false
        }
    }

    LabeledTextField(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        value = selectedPath,
        onValueChange = onChangeFilePath,
        labelText = localizedStrings.fileSystem,
        trailingIconVector = Icons.Default.Plagiarism,
        trailingIconDescription = localizedStrings.fileSystem
    ) {
        val newFile = showLoadFileDialog(localizedStrings.loadFromFile)
        newFile?.let {
            onChangeFilePath.invoke(it.absolutePath)
            AppPreferences.fileBrowserDirectory = it.toPath().parent.invariantSeparatorsPathString
        }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(modifier = Modifier.padding(4.dp),
            colors = buttonColors,
            enabled = isValidPath,
            onClick = { onLoadLeftFile(InputResource(InputResource.Kind.FILE, selectedFile)) },
            elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)) {
            AppImageIcon(relativePath = AppIconResource.icLoadLeftFile,
                label = localizedStrings.loadLeft,
                tint = buttonColors.contentColor(enabled = isValidPath).value)
            Text(localizedStrings.loadLeft, color = buttonColors.contentColor(enabled = isValidPath).value)
        }
        Button(modifier = Modifier.padding(4.dp),
            colors = buttonColors,
            enabled = isValidPath,
            onClick = { onLoadRightFile(InputResource(InputResource.Kind.FILE, selectedFile)) },
            elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)) {
            AppImageIcon(relativePath = AppIconResource.icLoadRightFile,
                label = localizedStrings.loadRight,
                tint = buttonColors.contentColor(enabled = isValidPath).value)
            Text(localizedStrings.loadRight, color = buttonColors.contentColor(enabled = isValidPath).value)
        }
    }
}

private fun getFileChooser(title: String): JFileChooser {
    return when (SystemUtils.IS_OS_MAC) {
        // NativeJFileChooser hangs on Azul Zulu 17 + JavaFX on macOS 12.1 aarch64.
        // With Azul Zulu w/o JFX, currently the file browser does not work at all on a M1 MBA.
        // The behaviour of NativeJFileChooser is different on Intel Macs, where it appears to work.
        // Hence, the non-native file chooser from Swing is used instead, which is not *nearly* as nice
        // as the native dialog on Windows, but it seems to be much more stable.
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