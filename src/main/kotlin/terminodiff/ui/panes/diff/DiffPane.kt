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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.VerticalSplitPane
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.cursorForHorizontalResize
import terminodiff.ui.panes.conceptdiff.ConceptDiffPanel
import terminodiff.ui.panes.graph.ShowGraphsPanel
import terminodiff.ui.panes.metadatadiff.MetadataDiffPanel

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun DiffPaneContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    strings: LocalizedStrings,
    useDarkTheme: Boolean,
    diffDataContainer: DiffDataContainer,
    splitPaneState: SplitPaneState,
) {
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
                    useDarkTheme = useDarkTheme
                )
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