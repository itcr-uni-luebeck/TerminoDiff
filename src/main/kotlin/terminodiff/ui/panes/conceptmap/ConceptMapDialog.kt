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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ca.uhn.fhir.context.FhirContext
import libraries.accompanist.pager.ExperimentalPagerApi
import libraries.accompanist.pager.rememberPagerState
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.engine.graph.GraphSide
import terminodiff.terminodiff.ui.panes.conceptmap.mapping.ConceptMappingEditorContent
import terminodiff.terminodiff.ui.panes.conceptmap.meta.ConceptMapMetaEditorContent
import terminodiff.ui.TabItem
import terminodiff.ui.Tabs
import terminodiff.ui.TabsContent
import java.util.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun ConceptMapDialog(
    diffDataContainer: DiffDataContainer,
    conceptMapState: ConceptMapState,
    localizedStrings: LocalizedStrings,
    fhirContext: FhirContext,
    isDarkTheme: Boolean,
    onCloseRequest: () -> Unit,
) {
    val pagerState = rememberPagerState()
    val allConceptCodes by derivedStateOf {
        //diffDataContainer.codeSystemDiff!!.combinedGraph!!.graph.vertexSet().map(CombinedVertex::code)
        diffDataContainer.codeSystemDiff!!.combinedGraph!!.graph.vertexSet().filter { it.side == GraphSide.BOTH }
            .associate { it.code to "${it.code} (${it.getTooltip()})" }.toSortedMap()
    }
    Window(
        title = localizedStrings.conceptMap,
        onCloseRequest = onCloseRequest,
        state = rememberWindowState(position = WindowPosition(Alignment.TopCenter), size = DpSize(1280.dp, 960.dp))
    ) {
        Column(Modifier.fillMaxSize().background(colorScheme.background)) {
            Column(Modifier.padding(8.dp).clip(RoundedCornerShape(8.dp))) {
                val tabs = listOf(ConceptMapTabItem.conceptMapping(allConceptCodes, diffDataContainer),
                    ConceptMapTabItem.metadata())
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

class ConceptMapTabItem(
    icon: ImageVector,
    title: LocalizedStrings.() -> String,
    screen: @Composable (LocalizedStrings, FhirContext, ConceptMapScreenData) -> Unit,
) : TabItem<ConceptMapTabItem.ConceptMapScreenData>(TabItemSpec(icon, title, screen)) {

    companion object {
        fun metadata() = ConceptMapTabItem(
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

        fun conceptMapping(allConceptCodes: SortedMap<String, String>, diffDataContainer: DiffDataContainer) = ConceptMapTabItem(
            icon = Icons.Default.AccountTree,
            title = { conceptMap },
            screen = { strings, _, data ->
                ConceptMappingEditorContent(
                    localizedStrings = strings,
                    conceptMapState = data.conceptMapState,
                    diffDataContainer = diffDataContainer,
                    useDarkTheme = data.isDarkTheme,
                    allConceptCodes = allConceptCodes,
                )
            }
        )
    }

    class ConceptMapScreenData(
        val diffDataContainer: DiffDataContainer,
        val conceptMapState: ConceptMapState,
        val isDarkTheme: Boolean,
    ) : ScreenData
}