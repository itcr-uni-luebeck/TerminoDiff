package terminodiff.ui.graphs

import org.hl7.fhir.r4.model.CodeSystem
import org.jungrapht.visualization.VisualizationViewer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.graph.CodeSystemRole
import terminodiff.engine.graph.FhirConceptEdge
import terminodiff.java.ui.SugiyamaLayoutViewer
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Using JUNGRAPHT from Kotlin does not seem to work reliably, as the needed builder classes are not accessible (?)
 * Hence, the meat of this class is written in pure Java and resides within the directory src/main/java/terminodiff/java/ui
 */
class SugiyamaLayoutFrame(
    codeSystem: CodeSystem,
    codeSystemRole: CodeSystemRole,
    title: String,
    useDarkTheme: Boolean
) : JFrame() {
    private val codeSystemGraphBuilder =
        CodeSystemGraphBuilder(codeSystem = codeSystem, codeSystemRole = codeSystemRole)

    private val container = JPanel(BorderLayout())

    private var viewer: VisualizationViewer<String, FhirConceptEdge> =
        SugiyamaLayoutViewer.configureViewer(
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