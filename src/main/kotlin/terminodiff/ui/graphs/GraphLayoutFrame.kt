package terminodiff.ui.graphs

import org.hl7.fhir.r4.model.CodeSystem
import org.jgrapht.Graph
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.graph.DiffEdge
import terminodiff.engine.graph.DiffNode
import terminodiff.i18n.LocalizedStrings
import terminodiff.java.ui.CodeSystemGraphJFrame
import terminodiff.java.ui.DiffGraphJFrame

/**
 * Using JUNGRAPHT from Kotlin does not seem to work reliably, as the needed builder classes are not accessible (?)
 * Hence, the meat of this functionality is written in pure Java and resides within the directory src/main/java/terminodiff/java/ui
 */
fun codeSystemGraphLayoutFrame(
    codeSystem: CodeSystem,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
    frameTitle: String
) {
    val graphBuilder = CodeSystemGraphBuilder(codeSystem = codeSystem, localizedStrings)
    CodeSystemGraphJFrame(graphBuilder.graph, useDarkTheme, localizedStrings, frameTitle) { c: String ->
        graphBuilder.nodeTree[c]?.display ?: "no display"
    }
}

fun diffGraphLayoutFrame(
    diffGraph: Graph<DiffNode, DiffEdge>,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
    frameTitle: String
) = DiffGraphJFrame(diffGraph, useDarkTheme, localizedStrings, frameTitle)