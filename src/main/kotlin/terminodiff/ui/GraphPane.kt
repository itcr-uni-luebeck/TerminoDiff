package terminodiff.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import org.graphstream.ui.layout.springbox.implementations.LinLog
import org.graphstream.ui.swing_viewer.DefaultView
import org.graphstream.ui.view.Viewer
import org.hl7.fhir.r4.model.CodeSystem
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultListenableGraph
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.i18n.LocalizedStrings
import java.awt.BorderLayout
import javax.swing.JFrame

@Composable
fun ShowGraphsPanel(
    fhirContext: FhirContext,
    leftCs: CodeSystem,
    rightCs: CodeSystem,
    localizedStrings: LocalizedStrings
) {

    var showLeftWindow by remember { mutableStateOf(false) }
    var showRightWindow by remember { mutableStateOf(false) }

    Card(
        Modifier.padding(8.dp).fillMaxWidth(),
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    showGraphSwingWindowGraphStream(
                        codeSystem = leftCs,
                        localizedStrings = localizedStrings,
                        frameTitle = localizedStrings.showLeftGraphButton
                    )
                }) {
                Text(localizedStrings.showLeftGraphButton)
            }
            Text(
                text = localizedStrings.graphsOpenInOtherWindows,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            )
            Button(onClick = {
                showGraphSwingWindowGraphStream(
                    codeSystem = rightCs,
                    localizedStrings = localizedStrings,
                    frameTitle = localizedStrings.showRightGraphButton
                )
            }) {
                Text(localizedStrings.showRightGraphButton)
            }
        }
    }
}

fun showGraphSwingWindowJGraphX(
    codeSystem: CodeSystem,
    localizedStrings: LocalizedStrings,
    frameTitle: String
) {
    val graph = CodeSystemGraphBuilder().buildCodeSystemJGraphT(codeSystem)
    // TODO: 08/12/21  https://jgrapht.org/guide/UserOverview#jgraphx-adapter
    // implement JGraphX/JGraphT visualization
}

fun showGraphSwingWindowGraphStream(
    codeSystem: CodeSystem,
    localizedStrings: LocalizedStrings,
    frameTitle: String
) {
    val graph = CodeSystemGraphBuilder().buildCodeSystemGraphStream(codeSystem)
    val viewer =
        org.graphstream.ui.swing_viewer.SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD).apply {
            enableAutoLayout(LinLog())
            enableXYZfeedback(true)
        }
    val view: DefaultView = (viewer.addDefaultView(false) as DefaultView).apply {
        enableMouseOptions()
    }
    JFrame().apply {
        setSize(500, 500)
        layout = BorderLayout()
        add(view)
        title = frameTitle
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        isVisible = true
    }
}

//@Composable
//fun GraphPane(codeSystem: CodeSystem, localizedStrings: LocalizedStrings, onClose: () -> Unit) {
//    val windowState = rememberWindowState()
//    val graph by remember { mutableStateOf(CodeSystemGraphBuilder().buildCodeSystemGraph()) }
//    Window(onCloseRequest = onClose, state = windowState, title = localizedStrings.viewGraphTitle) {
//        SwingPanel(
//            background = Color.LightGray,
//            modifier = Modifier.fillMaxSize(),
//            factory = {
//                JFrame().also { frame ->
//                    frame.contentPane.add(
//                        JPanel().also { panel ->
//                            panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
//                            panel.add(
//                                DefaultView(
//                                    SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD),
//                                    "Viewer",
//                                    SwingGraphRenderer()
//                                )
//                            )
//                        })
//                    frame.title = localizedStrings.title
//                }
//
//            })
//    }
//}
/*Column(
modifier = Modifier
    .border(BorderStroke(1.dp, Color.Magenta))
) {
val graph by remember { mutableStateOf(CodeSystemGraphBuilder().buildDemoGraph()) }
SwingPanel(
    background = Color.LightGray,
    modifier = Modifier.height(height).width(width),
    factory = {
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(
                DefaultView(
                    SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD),
                    "Viewer",
                    SwingGraphRenderer()
                )
            )
        }

    }
)
}*/