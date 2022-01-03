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
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
import terminodiff.ui.theme.TerminoDiffTheme
import java.io.File

val logger: Logger = LoggerFactory.getILoggerFactory().getLogger("terminodiff")

class TerminoDiffApp

/*class TestContainer {
    companion object {
        val fhirContext: FhirContext = FhirContext.forR4()

        private fun loadCsByName(filename: String): CodeSystem = fhirContext.newJsonParser().parseResource(
            CodeSystem::class.java,
            File("src/main/resources/testresources/$filename").readText(),
        )

        val cs1 = loadCsByName("simple-left.json")
        val cs2 = loadCsByName("simple-right.json")

        val oncotreeLeft = loadCsByName("oncotree_2020_10_01.json")
        val oncotreeRight = loadCsByName("oncotree_2021_11_02.json")

    }
}*/


@Composable
fun AppWindow(applicationScope: ApplicationScope) {
    var locale by remember { mutableStateOf(SupportedLocale.valueOf(AppPreferences.language)) }
    var strings by remember { mutableStateOf(getStrings(locale)) }
    val scrollState = rememberScrollState()
    var useDarkTheme by remember { mutableStateOf(AppPreferences.darkModeEnabled) }
    Window(onCloseRequest = { applicationScope.exitApplication() }) {
        this.window.title = strings.terminoDiff

        LocalizedAppWindow(
            localizedStrings = strings,
            scrollState = scrollState,
            useDarkTheme = useDarkTheme,
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
    onChangeDarkTheme: () -> Unit
) {
    //val leftCs = TestContainer.oncotreeLeft
    //val rightCs = TestContainer.oncotreeRight
    var leftCsFilenameResource: Pair<File, CodeSystem>? by remember { mutableStateOf(null) }
    var rightCsFilenameResource: Pair<File, CodeSystem>? by remember { mutableStateOf(null) }
    val fhirContext = remember { FhirContext.forR4() }
    val dataContainer by remember {
        derivedStateOf {
            DiffDataContainer(
                leftCsFilenameResource?.second,
                rightCsFilenameResource?.second
            )
        }
    }
    val onLoadLeftFile: () -> Unit = {
        loadFile(localizedStrings.loadLeftFile, fhirContext = fhirContext)?.let {
            leftCsFilenameResource = it
        }
    }
    val onLoadRightFile: () -> Unit = {
        loadFile(localizedStrings.loadRightFile, fhirContext = fhirContext)?.let {
            rightCsFilenameResource = it
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
                    leftFilename = leftCsFilenameResource?.first,
                    rightFilename = rightCsFilenameResource?.first,
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
    leftFilename: File?,
    rightFilename: File?,
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
                    leftFilename != null -> localizedStrings.`leftFileOpenFilename$`.format(leftFilename.absolutePath)
                    rightFilename != null -> localizedStrings.`rightFileOpenFilename$`.format(rightFilename.absolutePath)
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
            useDarkTheme = useDarkTheme
        )
        ConceptDiffPanel(
            leftCs = dataContainer.leftCodeSystem!!,
            rightCs = dataContainer.rightCodeSystem!!,
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


fun main() = application {
    AppWindow(this)
}
