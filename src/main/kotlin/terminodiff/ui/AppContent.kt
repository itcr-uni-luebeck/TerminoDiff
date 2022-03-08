package terminodiff.terminodiff.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.uhn.fhir.context.FhirContext
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
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

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalMaterial3Api::class)
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
    val conceptMapState by produceState<ConceptMapState?>(null,
        diffDataContainer.loadState,
        diffDataContainer.codeSystemDiff) {
        if (diffDataContainer.codeSystemDiff != null) {
            value = ConceptMapState()
        }
    }
    val isReady by derivedStateOf {
        diffDataContainer.leftCodeSystem != null && diffDataContainer.rightCodeSystem != null && showDiff
    }
    Crossfade(useDarkTheme) { darkTheme ->
        TerminoDiffTheme(useDarkTheme = darkTheme) {
            Scaffold(topBar = {
                TerminoDiffTopAppBar(localizedStrings = localizedStrings,
                    diffDataContainer = diffDataContainer,
                    conceptMapState = conceptMapState,
                    fhirContext = fhirContext,
                    showGraphButtons = isReady,
                    useDarkTheme = useDarkTheme,
                    onLocaleChange = onLocaleChange,
                    onChangeDarkTheme = onChangeDarkTheme,
                    onReload = onReload,
                    onShowLoadScreen = {
                        setShowDiff.invoke(false)
                    })
            }, containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
                Crossfade(isReady) {
                    when (it) {
                        true -> DiffPaneContent(modifier = Modifier.padding(scaffoldPadding),
                            strings = localizedStrings,
                            useDarkTheme = darkTheme,
                            localizedStrings = localizedStrings,
                            diffDataContainer = diffDataContainer,
                            splitPaneState = splitPaneState)
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
    }
}

