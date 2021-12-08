// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.*
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Identifier
import org.slf4j.LoggerFactory
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.ui.TerminoDiffTopAppBar
import terminodiff.ui.MetadataDiffPanel
import terminodiff.ui.ShowGraphsPanel
import java.io.File

const val baseFhirSystem = "https://fhir.example.org"
const val identifierSystem = "$baseFhirSystem/Identifier"
val root = LoggerFactory.getILoggerFactory().getLogger("terminodiff")

class TestContainer {
    companion object {
        val fhirContext: FhirContext = FhirContext.forR4()

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

        val oncotree: CodeSystem = fhirContext.newJsonParser().parseResource(
            CodeSystem::class.java,
            File("src/main/resources/oncotree.json").readText(),
        )

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
    val scrollState = rememberScrollState()
    Window(onCloseRequest = { applicationScope.exitApplication() }) {
        this.window.title = strings.terminoDiff
        LocalizedAppWindow(strings, scrollState) {
            locale = when (locale) {
                SupportedLocale.DE -> SupportedLocale.EN
                SupportedLocale.EN -> SupportedLocale.DE
            }
            strings = getStrings(locale)
            root.info("changed locale to ${locale.name}")
        }
    }
}

@Composable
fun LocalizedAppWindow(
    strings: LocalizedStrings,
    scrollState: ScrollState,
    onLocaleChange: () -> Unit,
) {
    MaterialTheme {
        Scaffold(
            topBar = {
                TerminoDiffTopAppBar(
                    localizedStrings = strings,
                    onLocaleChange = onLocaleChange,
                    onLoadLeftFile = {},
                    onLoadRightFile = {})
            }
        ) {
            Column(
                modifier = Modifier.scrollable(scrollState, Orientation.Vertical)
            ) {
                ShowGraphsPanel(TestContainer.fhirContext, TestContainer.oncotree, TestContainer.oncotree, strings)
                MetadataDiffPanel(
                    TestContainer.fhirContext,
                    TestContainer.cs1,
                    TestContainer.oncotree,
                    strings
                )
            }
        }
    }
}

fun main() = application {
    AppWindow(this)
}
