// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package terminodiff

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ca.uhn.fhir.context.FhirContext
import com.formdev.flatlaf.FlatDarkLaf
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.preferences.AppPreferences
import terminodiff.terminodiff.ui.TerminodiffAppContent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.UIManager

private val logger: Logger = LoggerFactory.getLogger(TerminoDiffApp::class.java)

/**
 * just for creating the log
 */
class TerminoDiffApp

val resourcesDir = System.getProperty("compose.application.resources.dir")?.let { path ->
    // this only works in the native distribution, (this includes when using `runDistributable` in Gradle/IntelliJ)
    // otherwise, resourcesDir will be `null`
    File(path)
}

fun main() = application {
    AppWindow(this)
}

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AppWindow(
    applicationScope: ApplicationScope
) {
    FlatDarkLaf.setup()

    Window(
        onCloseRequest = { applicationScope.exitApplication() },
        state = WindowState(size = DpSize(1366.dp, 768.dp), position = WindowPosition(Alignment.Center))
    ) {
        this.window.title = "TerminoDiff"
        resourcesDir?.let {
            this.window.iconImage = ImageIO.read(it.resolve("terminodiff@0.5x.png"))
        }
        UIManager.setLookAndFeel(FlatDarkLaf())
        LocalizedContent()
    }
}


@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun LocalizedContent() {
    var useDarkTheme by remember { mutableStateOf(AppPreferences.darkModeEnabled) }
    var locale by remember { mutableStateOf(SupportedLocale.valueOf(AppPreferences.language)) }
    val localizedStrings by derivedStateOf { getStrings(locale) }
    val fhirContext = remember { FhirContext.forR4() }
    val diffDataContainer = remember { DiffDataContainer(fhirContext, localizedStrings) }
    TerminodiffAppContent(
        localizedStrings = localizedStrings,
        diffDataContainer = diffDataContainer,
        fhirContext = fhirContext,
        useDarkTheme = useDarkTheme,
        onLocaleChange = {
            locale = when (locale) {
                SupportedLocale.DE -> SupportedLocale.EN
                SupportedLocale.EN -> SupportedLocale.DE
            }
            AppPreferences.language = locale.name
            logger.info("changed locale to ${locale.name}")
            diffDataContainer.localizedStrings = getStrings(locale)
        },
        onChangeDarkTheme = {
            useDarkTheme = !useDarkTheme
            AppPreferences.darkModeEnabled = useDarkTheme
        },
    )
}


