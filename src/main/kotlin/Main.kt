// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.gson.Gson
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings

@Composable
@Preview
fun App() {

    var locale by remember { mutableStateOf(SupportedLocale.getDefaultLocale()) }
    var strings by remember { mutableStateOf(getStrings(locale)) }

    LocalizedApp(strings) {
        locale = when (locale) {
            SupportedLocale.DE -> SupportedLocale.EN
            SupportedLocale.EN -> SupportedLocale.DE
        }
        strings = getStrings(locale)
    }

}

@Composable
fun LocalizedApp(strings: LocalizedStrings, onLocaleChange: () -> Unit) {
    MaterialTheme {
        Column {
            Text(Gson().toJson(strings))
            Text(strings.language)
            Button(onClick = { onLocaleChange() }) {
                Text(strings.terminoDiff)
            }
        }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
