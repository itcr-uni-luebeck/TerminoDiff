package terminodiff.terminodiff.ui.panes.diff

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.slf4j.LoggerFactory
import terminodiff.engine.graph.CodeSystemDiffBuilder
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.java.ui.NeighborhoodJFrame
import terminodiff.ui.cursorForHorizontalResize
import terminodiff.ui.panes.conceptdiff.ConceptDiffPanel
import terminodiff.ui.panes.graph.ShowGraphsPanel
import terminodiff.ui.panes.metadatadiff.MetadataDiffPanel

private val logger = LoggerFactory.getLogger("DiffPane")

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun DiffPaneContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    strings: LocalizedStrings,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
    diffDataContainer: DiffDataContainer,
    fhirContext: FhirContext,
    splitPaneState: SplitPaneState,
) {
    var neighborhoodDisplay: NeighborhoodDisplay? by remember { mutableStateOf(null) }

    if (neighborhoodDisplay != null) {
        showNeighboorhoodJFrame(neighborhoodDisplay!!, useDarkTheme, localizedStrings)
        neighborhoodDisplay = null
    }

    Column(
        modifier = modifier.scrollable(scrollState, Orientation.Vertical),
    ) {
        ShowGraphsPanel(
            leftCs = diffDataContainer.leftCodeSystem!!,
            rightCs = diffDataContainer.rightCodeSystem!!,
            diffGraph = diffDataContainer.codeSystemDiff!!.differenceGraph,
            localizedStrings = strings,
            useDarkTheme = useDarkTheme,
        )
        VerticalSplitPane(splitPaneState = splitPaneState) {
            first(100.dp) {
                ConceptDiffPanel(
                    diffDataContainer = diffDataContainer,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme,
                    fhirContext = fhirContext
                ) { focusCode ->
                    diffDataContainer.codeSystemDiff?.let { diff ->
                        if (neighborhoodDisplay?.focusCode == focusCode) {
                            neighborhoodDisplay!!.changeLayers(1)
                        } else {
                            neighborhoodDisplay = NeighborhoodDisplay(focusCode, diff)
                        }
                    }
                }
            }
            second(100.dp) {
                MetadataDiffPanel(
                    diffDataContainer = diffDataContainer,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme,
                )
            }
            splitter {
                visiblePart {
                    Box(Modifier.height(3.dp).fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary))
                }
                handle {
                    Box(
                        Modifier
                            .markAsHandle()
                            .cursorForHorizontalResize()
                            .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            .height(9.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

fun showNeighboorhoodJFrame(
    neighborhoodDisplay: NeighborhoodDisplay,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
) {
    NeighborhoodJFrame(
        /* graph = */ neighborhoodDisplay.getNeighborhoodGraph(),
        /* focusCode = */ neighborhoodDisplay.focusCode,
        /* isDarkTheme = */ useDarkTheme,
        /* localizedStrings = */ localizedStrings,
        /* frameTitle = */ localizedStrings.graph).apply {
        addClickListener { delta ->
            val newValue = neighborhoodDisplay.changeLayers(delta)
            this.setGraph(neighborhoodDisplay.getNeighborhoodGraph())
            newValue
        }
    }
}

data class NeighborhoodDisplay(
    val focusCode: String,
    val codeSystemDiff: CodeSystemDiffBuilder,
) {
    var layers by mutableStateOf(1)

    fun getNeighborhoodGraph() = codeSystemDiff.combinedGraph?.getSubgraph(focusCode, layers)?.also {
        logger.info("neighborhood of $focusCode and $layers layers: ${it.vertexSet().size} vertices and ${it.edgeSet().size} edges")
    }

    fun changeLayers(delta: Int): Int {
        layers = (layers + delta).coerceAtLeast(1)
        return layers
    }
}