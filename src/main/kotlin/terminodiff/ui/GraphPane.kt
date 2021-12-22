package terminodiff.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import com.mxgraph.layout.mxFastOrganicLayout
import com.mxgraph.swing.mxGraphComponent
import org.hl7.fhir.r4.model.CodeSystem
import org.jgrapht.ext.JGraphXAdapter
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.i18n.LocalizedStrings
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JApplet
import javax.swing.JFrame

@Composable
fun ShowGraphsPanel(
    fhirContext: FhirContext,
    leftCs: CodeSystem,
    rightCs: CodeSystem,
    localizedStrings: LocalizedStrings
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
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                colors = buttonColors,
                onClick = {
                    showGraphSwingWindowJGraphX(
                        codeSystem = leftCs,
                        localizedStrings = localizedStrings,
                        frameTitle = localizedStrings.showLeftGraphButton
                    )
                }) {
                Text(localizedStrings.showLeftGraphButton)
            }
            Text(
                text = localizedStrings.graphsOpenInOtherWindows,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Button(
                colors = buttonColors,
                onClick = {
                    showGraphSwingWindowJGraphX(
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
    val applet = object : JApplet() {
        val defaultSize = Dimension(530, 320)
        override fun init() {
            super.init()
            preferredSize = defaultSize
            val adapter = JGraphXAdapter(graph).apply {
                stylesheet.apply {
                    //todo implement nice stylesheet ^^
                }
            }
            val component = mxGraphComponent(adapter).apply {
                isEnabled = false
                isFoldingEnabled = true
            }
            size = defaultSize
            contentPane.add(component)
            mxFastOrganicLayout(adapter).execute(adapter.defaultParent)

        }
    }.apply {
        init()
    }
    // TODO: 08/12/21  https://jgrapht.org/guide/UserOverview#jgraphx-adapter
    // implement JGraphX/JGraphT visualization
    JFrame().apply {
        setSize(500, 500)
        layout = BorderLayout()
        contentPane.add(applet)
        title = frameTitle
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        pack()
        isVisible = true
    }
}