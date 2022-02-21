package terminodiff.terminodiff.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.uhn.fhir.context.FhirContext
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.terminodiff.ui.panes.diff.DiffPaneContent
import terminodiff.terminodiff.ui.panes.loaddata.LoadDataPaneContent
import terminodiff.ui.TerminoDiffTopAppBar
import terminodiff.ui.theme.TerminoDiffTheme

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun TerminodiffAppContent(
    localizedStrings: LocalizedStrings,
    diffDataContainer: DiffDataContainer,
    fhirContext: FhirContext,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    splitPaneState: SplitPaneState,
) {
    var showDiff by remember { mutableStateOf(false) }
    val onLoadLeftFile: (InputResource) -> Unit = {
        diffDataContainer.leftResource = it
    }
    val onLoadRightFile: (InputResource) -> Unit = {
        diffDataContainer.rightResource = it
    }

    TerminodiffContentWindow(localizedStrings = localizedStrings,
        scrollState = scrollState,
        useDarkTheme = useDarkTheme,
        onLocaleChange = onLocaleChange,
        onChangeDarkTheme = onChangeDarkTheme,
        fhirContext = fhirContext,
        onLoadLeft = onLoadLeftFile,
        onLoadRight = onLoadRightFile,
        onReload = { diffDataContainer.reload() },
        diffDataContainer = diffDataContainer,
        splitPaneState = splitPaneState,
        showDiff = showDiff) { newValue -> showDiff = newValue }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun TerminodiffContentWindow(
    localizedStrings: LocalizedStrings,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    fhirContext: FhirContext,
    onLoadLeft: (InputResource) -> Unit,
    onLoadRight: (InputResource) -> Unit,
    onReload: () -> Unit,
    diffDataContainer: DiffDataContainer,
    splitPaneState: SplitPaneState,
    showDiff: Boolean,
    setShowDiff: (Boolean) -> Unit,
) {
    TerminoDiffTheme(useDarkTheme = useDarkTheme) {
        Scaffold(topBar = {
            TerminoDiffTopAppBar(
                localizedStrings = localizedStrings,
                onLocaleChange = onLocaleChange,
                onChangeDarkTheme = onChangeDarkTheme,
                onReload = onReload,
                onShowLoadScreen = {
                    setShowDiff.invoke(false)
                }
            )
        }, backgroundColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
            when (diffDataContainer.leftCodeSystem != null && diffDataContainer.rightCodeSystem != null && showDiff) {
                true -> DiffPaneContent(
                    modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    strings = localizedStrings,
                    useDarkTheme = useDarkTheme,
                    localizedStrings = localizedStrings,
                    diffDataContainer = diffDataContainer,
                    splitPaneState = splitPaneState
                )
                false -> LoadDataPaneContent(
                    modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    localizedStrings = localizedStrings,
                    leftResource = diffDataContainer.leftResource,
                    rightResource = diffDataContainer.rightResource,
                    onLoadLeft = onLoadLeft,
                    onLoadRight = onLoadRight,
                    fhirContext = fhirContext,
                    onGoButtonClick = { setShowDiff.invoke(true) },
                )
            }
        }
    }
}

