package terminodiff.terminodiff.ui.panes.diff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.engine.graph.CodeSystemDiffBuilder
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.java.ui.NeighborhoodJFrame
import terminodiff.terminodiff.engine.metadata.MetadataComparisonResult
import terminodiff.ui.cursorForHorizontalResize
import terminodiff.ui.panes.conceptdiff.ConceptDiffPanel
import terminodiff.ui.panes.metadatadiff.MetadataDiffPanel
import javax.swing.JOptionPane

private val logger = LoggerFactory.getLogger("DiffPane")

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun DiffPaneContent(
    modifier: Modifier = Modifier,
    strings: LocalizedStrings,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
    diffDataContainer: DiffDataContainer,
    splitPaneState: SplitPaneState,
) {
    var neighborhoodDisplay: NeighborhoodDisplay? by remember { mutableStateOf(null) }

    var showIdenticalDialog: Boolean? by remember {
        mutableStateOf(diffDataContainer.codeSystemDiff?.let { diff ->
            when {
                diff.metadataDifferences.comparisons.filter { comparison ->
                    comparison.diffItem.label.invoke(localizedStrings) != localizedStrings.id // id differences are ok
                }.all { comparison -> comparison.result == MetadataComparisonResult.IDENTICAL } -> {
                    val listsEmpty = diff.onlyInRightConcepts.isEmpty() && diff.onlyInLeftConcepts.isEmpty()
                    val comparisonsEmpty = diff.conceptDifferences.values.all { cdiff ->
                        cdiff.propertyComparison.all { pdiff -> pdiff.result == KeyedListDiffResultKind.IDENTICAL } &&
                                cdiff.conceptComparison.all { cc -> cc.result == ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL } &&
                                cdiff.designationComparison.all { dc -> dc.result == KeyedListDiffResultKind.IDENTICAL }
                    }
                    listsEmpty && comparisonsEmpty
                }
                else -> false
            }
        })
    }

    if (showIdenticalDialog == true) {
        JOptionPane.showConfirmDialog(/* parentComponent = */ null,
            /* message = */ localizedStrings.resourcesIdenticalMessage,
            /* title = */ localizedStrings.resourcesIdentical,
            /* optionType = */ JOptionPane.DEFAULT_OPTION,
            /* messageType = */ JOptionPane.INFORMATION_MESSAGE)
        @Suppress("UNUSED_VALUE")
        showIdenticalDialog = false
    }

    if (neighborhoodDisplay != null) {
        showNeighborhoodJFrame(neighborhoodDisplay!!, useDarkTheme, localizedStrings)
        neighborhoodDisplay = null
    }

    Column(
        modifier.fillMaxSize(),
    ) {
        VerticalSplitPane(splitPaneState = splitPaneState) {
            first(100.dp) {
                ConceptDiffPanel(diffDataContainer = diffDataContainer,
                    localizedStrings = strings,
                    useDarkTheme = useDarkTheme) { focusCode ->
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
                    Box(Modifier.height(3.dp).fillMaxWidth().background(MaterialTheme.colorScheme.primary))
                }
                handle {
                    Box(Modifier.markAsHandle().cursorForHorizontalResize()
                        .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)).height(9.dp)
                        .fillMaxWidth())
                }
            }
        }
    }
}

fun showNeighborhoodJFrame(
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
    private var layers by mutableStateOf(1)

    fun getNeighborhoodGraph() = codeSystemDiff.combinedGraph!!.getSubgraph(focusCode, layers).also {
        logger.info("neighborhood of $focusCode and $layers layers: ${it.vertexSet().size} vertices and ${it.edgeSet().size} edges")
    }

    fun changeLayers(delta: Int): Int {
        layers = (layers + delta).coerceAtLeast(1)
        return layers
    }
}