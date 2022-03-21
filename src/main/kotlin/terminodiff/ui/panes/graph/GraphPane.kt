package terminodiff.ui.panes.graph

import org.hl7.fhir.r4.model.CodeSystem
import org.jgrapht.Graph
import terminodiff.engine.graph.DiffEdge
import terminodiff.engine.graph.DiffNode
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.graphs.codeSystemGraphLayoutFrame
import terminodiff.ui.graphs.diffGraphLayoutFrame

fun showDiffGraphSwingWindow(
    diffGraph: Graph<DiffNode, DiffEdge>,
    frameTitle: String,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
) = diffGraphLayoutFrame(diffGraph, useDarkTheme, localizedStrings, frameTitle)

fun showGraphSwingWindow(
    codeSystem: CodeSystem,
    frameTitle: String,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
) = codeSystemGraphLayoutFrame(codeSystem, useDarkTheme, localizedStrings, frameTitle)