@file:OptIn(ExperimentalPagerApi::class)

package terminodiff.terminodiff.ui.panes.conceptmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import ca.uhn.fhir.context.FhirContext
import libraries.accompanist.pager.ExperimentalPagerApi
import libraries.accompanist.pager.rememberPagerState
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.ui.panes.conceptmap.mapping.ConceptMappingEditorContent
import terminodiff.terminodiff.ui.panes.conceptmap.meta.ConceptMapMetaEditorContent
import terminodiff.ui.TabItem
import terminodiff.ui.Tabs
import terminodiff.ui.TabsContent

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun ConceptMapDialog(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    fhirContext: FhirContext,
    isDarkTheme: Boolean,
    onCloseRequest: () -> Unit,
) {
    val conceptMapState by remember { mutableStateOf(ConceptMapState(diffDataContainer)) }
    val pagerState = rememberPagerState()
    Window(
        title = localizedStrings.conceptMap,
        onCloseRequest = onCloseRequest,
    ) {
        Column(Modifier.fillMaxSize().background(colorScheme.background)) {
            val tabs = listOf(ConceptMapTabItem.Metadata, ConceptMapTabItem.ConceptMapping)
            Column(Modifier.padding(8.dp).clip(RoundedCornerShape(8.dp))) {
                Tabs(tabs = tabs, pagerState = pagerState, localizedStrings = localizedStrings)
                TabsContent(tabs = tabs,
                    pagerState = pagerState,
                    localizedStrings = localizedStrings,
                    fhirContext = fhirContext) {
                    ConceptMapTabItem.ConceptMapScreenData(diffDataContainer, conceptMapState, isDarkTheme)
                }
            }
        }
    }
}

sealed class ConceptMapTabItem(
    icon: ImageVector,
    title: LocalizedStrings.() -> String,
    screen: @Composable (LocalizedStrings, FhirContext, ConceptMapScreenData) -> Unit,
) : TabItem<ConceptMapTabItem.ConceptMapScreenData>(icon, title, screen) {

    object Metadata : ConceptMapTabItem(
        icon = Icons.Default.Description,
        title = { metadata },
        screen = { strings, fhirContext, data ->
            ConceptMapMetaEditorContent(conceptMapState = data.conceptMapState,
                localizedStrings = strings,
                isDarkTheme = data.isDarkTheme,
                fhirContext = fhirContext
            )
        }
    )

    object ConceptMapping : ConceptMapTabItem(
        icon = Icons.Default.AccountTree,
        title = { conceptMap },
        screen = { strings, _, data ->
            ConceptMappingEditorContent(localizedStrings = strings,
                conceptMapState = data.conceptMapState,
                useDarkTheme = data.isDarkTheme,
                codeSystemDiff = data.diffDataContainer.codeSystemDiff!!)
        }
    )

    class ConceptMapScreenData(
        val diffDataContainer: DiffDataContainer,
        val conceptMapState: ConceptMapState,
        val isDarkTheme: Boolean,
    ) : ScreenData
}