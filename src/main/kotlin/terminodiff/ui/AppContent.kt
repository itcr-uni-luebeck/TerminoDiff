package terminodiff.terminodiff.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.terminodiff.engine.resources.InputResource.*
import terminodiff.terminodiff.ui.panes.diff.DiffPaneContent
import terminodiff.terminodiff.ui.panes.loaddata.LoadDataPaneContent
import terminodiff.terminodiff.ui.panes.loaddata.showLoadFileDialog
import terminodiff.ui.TerminoDiffTopAppBar
import terminodiff.ui.theme.TerminoDiffTheme
import java.io.File
import java.net.InetAddress
import java.util.*

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
    var goButtonClicked by remember { mutableStateOf(false) }
    val onLoadLeftFile: (InputResource) -> Unit = {
        diffDataContainer.leftResource = it
    }
    val onLoadRightFile: (InputResource) -> Unit = {
        diffDataContainer.rightResource = it
    }

    val coroutineScope = rememberCoroutineScope()
    /*@Suppress("ControlFlowWithEmptyBody") when (InetAddress.getLocalHost().hostName.lowercase(Locale.getDefault())) {
        // TODO: 04/02/22 remove prior to release!
        "joshua-athena-windows" -> coroutineScope.launch {
            diffDataContainer.leftResource = InputResource(Kind.FILE,
                File("C:\\Users\\jpwie\\repos\\TerminoDiff\\src\\main\\resources\\testresources\\oncotree_2017_06_21.json"))
            //diffDataContainer.rightFilename = File("C:\\Users\\jpwie\\repos\\TerminoDiff\\src\\main\\resources\\testresources\\oncotree_2021_11_02.json")
        }
    }
*/
    TerminodiffContentWindow(localizedStrings = localizedStrings,
        scrollState = scrollState,
        useDarkTheme = useDarkTheme,
        onLocaleChange = onLocaleChange,
        onChangeDarkTheme = onChangeDarkTheme,
        onLoadLeft = onLoadLeftFile,
        onLoadRight = onLoadRightFile,
        onReload = { diffDataContainer.reload() },
        diffDataContainer = diffDataContainer,
        splitPaneState = splitPaneState,
        goButtonClicked = goButtonClicked) { goButtonClicked = true }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun TerminodiffContentWindow(
    localizedStrings: LocalizedStrings,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    onLoadLeft: (InputResource) -> Unit,
    onLoadRight: (InputResource) -> Unit,
    onReload: () -> Unit,
    diffDataContainer: DiffDataContainer,
    splitPaneState: SplitPaneState,
    goButtonClicked: Boolean,
    onGoButtonClick: () -> Unit,
) {
    TerminoDiffTheme(useDarkTheme = useDarkTheme) {
        Scaffold(topBar = {
            TerminoDiffTopAppBar(
                localizedStrings = localizedStrings,
                onLocaleChange = onLocaleChange,
                onChangeDarkTheme = onChangeDarkTheme,
                onReload = onReload,
            )
        }, backgroundColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
            when (diffDataContainer.leftCodeSystem != null && diffDataContainer.rightCodeSystem != null && goButtonClicked) {
                true -> DiffPaneContent(modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    strings = localizedStrings,
                    useDarkTheme = useDarkTheme,
                    diffDataContainer = diffDataContainer,
                    splitPaneState = splitPaneState)
                false -> LoadDataPaneContent(modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    localizedStrings = localizedStrings,
                    leftResource = diffDataContainer.leftResource,
                    rightResource = diffDataContainer.rightResource,
                    onLoadLeft = onLoadLeft,
                    onLoadRight = onLoadRight,
                    onGoButtonClick = onGoButtonClick)
            }
        }
    }
}

