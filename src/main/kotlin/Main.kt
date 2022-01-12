// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package terminodiff

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.preferences.AppPreferences
import terminodiff.terminodiff.ui.TerminodiffAppContent
import java.awt.Dimension
import java.io.File
import javax.imageio.ImageIO
import javax.swing.UIManager

private val logger: Logger = LoggerFactory.getLogger(TerminoDiffApp::class.java)

/**
 * just for creating the log
 */
class TerminoDiffApp

val resourcesDir = System.getProperty("compose.application.resources.dir")?.let {
    // this only works in the native distribution, or when running via `runDistributable` in Gradle/IntelliJ
    File(it)
}

fun main() = application {
    ThemedAppWindow(this)
}

@Composable
fun ThemedAppWindow(applicationScope: ApplicationScope) {
    var useDarkTheme by remember { mutableStateOf(AppPreferences.darkModeEnabled) }
    AppWindow(applicationScope = applicationScope, useDarkTheme = useDarkTheme, onChangeDarkTheme = {
        useDarkTheme = !useDarkTheme
        AppPreferences.darkModeEnabled = useDarkTheme
    })
}

@Composable
fun AppWindow(applicationScope: ApplicationScope, useDarkTheme: Boolean, onChangeDarkTheme: () -> Unit) {
    when (useDarkTheme && SystemUtils.IS_OS_WINDOWS) {
        //setting this does not make sense if not on windows
        true -> FlatDarkLaf.setup()
        else -> FlatLightLaf.setup()
    }
    var locale by remember { mutableStateOf(SupportedLocale.valueOf(AppPreferences.language)) }
    var strings by remember { mutableStateOf(getStrings(locale)) }
    val scrollState = rememberScrollState()
    var hasResizedWindow by remember { mutableStateOf(false) }
    Window(
        onCloseRequest = { applicationScope.exitApplication() },
    ) {
        this.window.title = strings.terminoDiff
        resourcesDir?.let {
            this.window.iconImage = ImageIO.read(it.resolve("terminodiff@0.5x.png"))
        }
        when (useDarkTheme && SystemUtils.IS_OS_WINDOWS) {
            false -> UIManager.setLookAndFeel(FlatLightLaf())
            else -> UIManager.setLookAndFeel(FlatDarkLaf())
        }

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

        TerminodiffAppContent(
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
            onChangeDarkTheme = onChangeDarkTheme
        )
    }
}



