package terminodiff.ui.panes.graph

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hl7.fhir.r4.model.CodeSystem
import org.jgrapht.Graph
import terminodiff.engine.graph.DiffEdge
import terminodiff.engine.graph.DiffNode
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.graphs.CodeSystemGraphLayoutFrame
import terminodiff.ui.graphs.DiffGraphLayoutFrame

@Composable
fun ShowGraphsPanel(
    leftCs: CodeSystem,
    rightCs: CodeSystem,
    diffGraph: Graph<DiffNode, DiffEdge>,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
) {
    Card(
        Modifier.padding(8.dp).fillMaxWidth(),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        val buttonColors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                colors = buttonColors,
                onClick = {
                    showGraphSwingWindow(
                        codeSystem = leftCs,
                        frameTitle = localizedStrings.showLeftGraphButton,
                        useDarkTheme = useDarkTheme
                    )
                }) {
                Text(localizedStrings.showLeftGraphButton, color = MaterialTheme.colorScheme.onPrimary)
            }
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.tertiary),
                onClick = {
                    showDiffGraphSwingWindow(
                        diffGraph = diffGraph,
                        frameTitle = localizedStrings.diffGraph,
                        useDarkTheme = useDarkTheme,
                        localizedStrings = localizedStrings
                    )
                }) {
                Text(localizedStrings.diffGraph, color = MaterialTheme.colorScheme.onTertiary)
            }

            Button(
                colors = buttonColors,
                onClick = {
                    showGraphSwingWindow(
                        codeSystem = rightCs,
                        frameTitle = localizedStrings.showRightGraphButton,
                        useDarkTheme = useDarkTheme
                    )
                }) {
                Text(localizedStrings.showRightGraphButton, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

fun showDiffGraphSwingWindow(
    diffGraph: Graph<DiffNode, DiffEdge>,
    frameTitle: String,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings
) =
    DiffGraphLayoutFrame(
        diffGraph = diffGraph,
        title = frameTitle,
        useDarkTheme = useDarkTheme,
        localizedStrings = localizedStrings
    )

fun showGraphSwingWindow(
    codeSystem: CodeSystem,
    frameTitle: String,
    useDarkTheme: Boolean
) = CodeSystemGraphLayoutFrame(codeSystem, frameTitle, useDarkTheme)