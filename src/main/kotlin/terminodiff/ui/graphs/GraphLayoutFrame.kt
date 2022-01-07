package terminodiff.ui.graphs

import org.hl7.fhir.r4.model.CodeSystem
import org.jgrapht.Graph
import org.jungrapht.visualization.VisualizationViewer
import terminodiff.engine.graph.*
import terminodiff.i18n.LocalizedStrings
import terminodiff.java.ui.GraphViewer
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Using JUNGRAPHT from Kotlin does not seem to work reliably, as the needed builder classes are not accessible (?)
 * Hence, the meat of this class is written in pure Java and resides within the directory src/main/java/terminodiff/java/ui
 */
class CodeSystemGraphLayoutFrame(
    codeSystem: CodeSystem,
    title: String,
    useDarkTheme: Boolean
) : JFrame() {
    private val codeSystemGraphBuilder =
        CodeSystemGraphBuilder(codeSystem = codeSystem)

    private val container = JPanel(BorderLayout())

    private var viewer: VisualizationViewer<String, FhirConceptEdge> =
        GraphViewer.configureCodeSystemViewer(
            codeSystemGraphBuilder.graph,
            useDarkTheme
        ) { c: String -> codeSystemGraphBuilder.nodeTree[c]?.display ?: "no display" }

    init {
        super.setTitle(title)
        container.add(viewer.component)
        this.add(container)
        this.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        pack()
        isVisible = true
    }
}

class DiffGraphLayoutFrame(
    diffGraph: Graph<DiffNode, DiffEdge>,
    title: String,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings
) : JFrame() {
    private val container = JPanel(BorderLayout())
    private var viewer = GraphViewer.configureDiffGraphViewer(diffGraph, useDarkTheme, localizedStrings)

    init {
        super.setTitle(title)
        container.add(viewer.component)
        this.add(container)
        this.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        pack()
        isVisible = true
    }
}