// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.graph.CodeSystemDiffBuilder
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.graph.CodeSystemRole
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.ui.ConceptDiffPanel
import terminodiff.ui.MetadataDiffPanel
import terminodiff.ui.ShowGraphsPanel
import terminodiff.ui.TerminoDiffTopAppBar
import terminodiff.ui.theme.TerminoDiffTheme
import java.io.File

val logger: Logger = LoggerFactory.getILoggerFactory().getLogger("terminodiff")

class TestContainer {
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
}


@Composable
fun AppWindow(applicationScope: ApplicationScope) {
    var locale by remember { mutableStateOf(SupportedLocale.getDefaultLocale()) }
    var strings by remember { mutableStateOf(getStrings(locale)) }
    val scrollState = rememberScrollState()
    var useDarkTheme by remember { mutableStateOf(false) }
    Window(onCloseRequest = { applicationScope.exitApplication() }) {
        this.window.title = strings.terminoDiff

        LocalizedAppWindow(strings = strings, scrollState = scrollState, useDarkTheme = useDarkTheme, onLocaleChange = {
            locale = when (locale) {
                SupportedLocale.DE -> SupportedLocale.EN
                SupportedLocale.EN -> SupportedLocale.DE
            }
            strings = getStrings(locale)
            logger.info("changed locale to ${locale.name}")
        }, onChangeDarkTheme = {
            useDarkTheme = !useDarkTheme
        })
    }
}

@Composable
fun LocalizedAppWindow(
    strings: LocalizedStrings,
    scrollState: ScrollState,
    useDarkTheme: Boolean,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit
) {
    val leftCs = TestContainer.oncotreeLeft
    val rightCs = TestContainer.oncotreeRight
    val leftGraphBuilder by remember { mutableStateOf(CodeSystemGraphBuilder(leftCs, CodeSystemRole.LEFT)) }
    val rightGraphBuilder by remember { mutableStateOf(CodeSystemGraphBuilder(rightCs, CodeSystemRole.RIGHT)) }
    val diff by remember { mutableStateOf(buildDiff(leftGraphBuilder, rightGraphBuilder, strings)) }
    TerminoDiffTheme(useDarkTheme = useDarkTheme) {
        Scaffold(
            topBar = {
                TerminoDiffTopAppBar(
                    localizedStrings = strings,
                    onLocaleChange = onLocaleChange,
                    onLoadLeftFile = {},
                    onLoadRightFile = {},
                    onChangeDarkTheme = onChangeDarkTheme
                )
            },
            backgroundColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.scrollable(scrollState, Orientation.Vertical),
            ) {
                ShowGraphsPanel(
                    fhirContext = TestContainer.fhirContext,
                    leftCs = leftCs,
                    rightCs = rightCs,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme
                )
                ConceptDiffPanel(
                    leftCs = leftCs,
                    rightCs = rightCs,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme
                )
                MetadataDiffPanel(
                    fhirContext = TestContainer.fhirContext,
                    leftCs = leftCs,
                    rightCs = rightCs,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme
                )

            }
        }
    }
}

private fun buildDiff(
    leftGraphBuilder: CodeSystemGraphBuilder,
    rightGraphBuilder: CodeSystemGraphBuilder,
    strings: LocalizedStrings
): CodeSystemDiffBuilder {
    return CodeSystemDiffBuilder(leftGraphBuilder, rightGraphBuilder).build().also {
        logger.info("${it.onlyInLeftConcepts.size} code(-s) only in left: ${it.onlyInLeftConcepts.joinToString(", ")}")
        logger.info("${it.onlyInRightConcepts.size} code(-s) only in right: ${it.onlyInRightConcepts.joinToString(", ")}")
        val differentConcepts =
            it.conceptDifferences.filterValues { d -> d.conceptComparison.any { c -> c.result != ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL } || d.propertyComparison.size != 0 }
        logger.info(
            "${differentConcepts.size} concept-level difference(-s): ${
                differentConcepts.entries.joinToString(separator = "\n - ") { (key, diff) ->
                    "$key -> ${
                        diff.toString(
                            strings
                        )
                    }"
                }
            }"
        )
    }
}

fun main() = application {
    AppWindow(this)
}
