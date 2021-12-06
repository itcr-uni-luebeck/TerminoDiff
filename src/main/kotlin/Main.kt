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
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ca.uhn.fhir.context.FhirContext
import com.google.gson.Gson
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Identifier
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.ui.metadataDiffPanel

const val baseFhirSystem = "https://fhir.example.org"
const val identifierSystem = "$baseFhirSystem/Identifier"

class TestContainer {
    companion object {
        val fhirContext = FhirContext.forR4()

        val cs1 = CodeSystem().apply {
            url = "$baseFhirSystem/CodeSystem/example"
            title = "Example CodeSystem for TerminoloDiff version 1.0.0"
            version = "1.0.0"
            name = "example-code-system-terminologodiff"
            id = "ex-cs-terminologodiff-v1"
            status = Enumerations.PublicationStatus.ACTIVE
            experimental = true
            identifier = listOf(Identifier().apply {
                url = "$identifierSystem/1"
                value = "ID1"
            })
        }

        val cs2 = cs1.copy().apply {
            title = "Example CodeSystem for TerminoloDiff version 2.0.0"
            version = "2.0.0"
            id = "ex-cs-terminologodiff-v2"
            experimental = false
            identifier = listOf(Identifier().apply {
                url = "$identifierSystem/2"
                value = "ID2"
            })
        }
    }
}

@Composable
fun AppWindow(applicationScope: ApplicationScope) {
    var locale by remember { mutableStateOf(SupportedLocale.getDefaultLocale()) }
    var strings by remember { mutableStateOf(getStrings(locale)) }
    Window(onCloseRequest = { applicationScope.exitApplication() }) {
        this.window.title = strings.terminoDiff
        LocalizedApp(strings) {
            locale = when (locale) {
                SupportedLocale.DE -> SupportedLocale.EN
                SupportedLocale.EN -> SupportedLocale.DE
            }
            strings = getStrings(locale)
        }
    }
}

@Composable
@Preview
fun AppWindowPreview() {
    application { AppWindow(this) }
}

@Composable
fun LocalizedApp(strings: LocalizedStrings, onLocaleChange: () -> Unit) {
    MaterialTheme {
        Column {
            Text(Gson().toJson(strings))
            Text(strings.language)
            Button(onClick = { onLocaleChange() }) {
                Text(strings.language)
            }
            metadataDiffPanel(
                TestContainer.fhirContext,
                TestContainer.cs1,
                TestContainer.cs2,
                strings
            )
        }

    }
}

fun main() = application {
    AppWindow(this)
}
