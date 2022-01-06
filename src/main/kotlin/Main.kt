// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.engine.resources.loadFile
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.preferences.AppPreferences
import terminodiff.ui.*
import terminodiff.ui.panes.conceptdiff.ConceptDiffPanel
import terminodiff.ui.panes.graph.ShowGraphsPanel
import terminodiff.ui.panes.metadatadiff.MetadataDiffPanel
import terminodiff.ui.theme.TerminoDiffTheme
import java.awt.Dimension
import java.io.File

private val logger: Logger = LoggerFactory.getLogger(TerminoDiffApp::class.java)

/**
 * just for creating the log
 */
class TerminoDiffApp


fun main() = application {
    AppWindow(this)
}

@Composable
fun AppWindow(applicationScope: ApplicationScope) {
    var locale by remember { mutableStateOf(SupportedLocale.valueOf(AppPreferences.language)) }
    var strings by remember { mutableStateOf(getStrings(locale)) }
    val scrollState = rememberScrollState()
    var useDarkTheme by remember { mutableStateOf(AppPreferences.darkModeEnabled) }
    var hasResizedWindow by remember { mutableStateOf(false) }
    Window(
        onCloseRequest = { applicationScope.exitApplication() },
    ) {
        this.window.title = strings.terminoDiff
        if (!hasResizedWindow) {
            // app crashes if we use state for the window, when the locale is changed, with the error
            // that the window is already on screen.
            // this is because everything is recomposed when the locale changes, and that breaks AWT.
            // using the mutable state, we change the window size exactly once, during the first (re-) composition,
            // so that the user can then change the res as they require.
            // A resolution of 1280x960 is 4:3.
            this.window.size = Dimension(1280, 960)
            hasResizedWindow = true
        }

        LocalizedAppWindow(
            localizedStrings = strings,
            scrollState = scrollState,
            useDarkTheme = useDarkTheme,
            frameWindow = this,
            onLocaleChange = {
                locale = when (locale) {
                    SupportedLocale.DE -> SupportedLocale.EN
                    SupportedLocale.EN -> SupportedLocale.DE
                }
                strings = getStrings(locale)
                AppPreferences.language = locale.name
                logger.info("changed locale to ${locale.name}")
            },
            onChangeDarkTheme = {
                useDarkTheme = !useDarkTheme
                AppPreferences.darkModeEnabled = useDarkTheme
            })
    }
}

@Composable
fun LocalizedAppWindow(
    localizedStrings: LocalizedStrings,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    frameWindow: FrameWindowScope
) {
    //val leftCs = TestContainer.oncotreeLeft
    //val rightCs = TestContainer.oncotreeRight
    val scope = rememberCoroutineScope()
    var leftCsFilenameResource: Pair<File, CodeSystem>? by remember { mutableStateOf(null) }
    var rightCsFilenameResource: Pair<File, CodeSystem>? by remember { mutableStateOf(null) }
    val fhirContext = remember { FhirContext.forR4() }
    var dataContainer by remember {
        mutableStateOf(
            DiffDataContainer(
                leftCsFilenameResource?.second,
                rightCsFilenameResource?.second
            )
        )
    }

    fun updateDataContainer() {
        dataContainer = DiffDataContainer(
            leftCsFilenameResource?.second,
            rightCsFilenameResource?.second
        )
        dataContainer.computeDiff(localizedStrings)
    }

    val onLoadLeftFile: () -> Unit = {
        loadFile(localizedStrings.loadLeftFile, fhirContext = fhirContext, frameWindow)?.let {
            leftCsFilenameResource = it
            updateDataContainer()
        }
    }
    val onLoadRightFile: () -> Unit = {
        loadFile(localizedStrings.loadRightFile, fhirContext = fhirContext, frameWindow)?.let {
            rightCsFilenameResource = it
            updateDataContainer()
        }
    }

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
            when (dataContainer.isInitialized) {
                true -> ContainerInitializedContent(
                    modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    dataContainer = dataContainer,
                    strings = localizedStrings,
                    useDarkTheme = useDarkTheme,
                    fhirContext = fhirContext
                )
                false -> ContainerUninitializedContent(
                    modifier = Modifier.padding(scaffoldPadding),
                    scrollState = scrollState,
                    localizedStrings = localizedStrings,
                    leftFile = leftCsFilenameResource?.first,
                    rightFile = rightCsFilenameResource?.first,
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
    dataContainer: DiffDataContainer,
    strings: LocalizedStrings,
    useDarkTheme: Boolean,
    fhirContext: FhirContext
) {
    Column(
        modifier = modifier.scrollable(scrollState, Orientation.Vertical),
    ) {
        ShowGraphsPanel(
            leftCs = dataContainer.leftCodeSystem!!,
            rightCs = dataContainer.rightCodeSystem!!,
            localizedStrings = strings,
            useDarkTheme = useDarkTheme,
        )
        ConceptDiffPanel(
            verticalWeight = 0.45f,
            diffDataContainer = dataContainer,
            localizedStrings = strings,
            useDarkTheme = useDarkTheme
        )
        MetadataDiffPanel(
            leftCs = dataContainer.leftCodeSystem!!,
            rightCs = dataContainer.rightCodeSystem!!,
            localizedStrings = strings,
            useDarkTheme = useDarkTheme,
            fhirContext = fhirContext
        )
    }
}

