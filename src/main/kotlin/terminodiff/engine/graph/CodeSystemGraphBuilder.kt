package terminodiff.engine.graph

import org.graphstream.graph.implementations.MultiGraph
import org.hl7.fhir.r4.model.CodeSystem
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultListenableGraph
import org.jgrapht.graph.DirectedMultigraph

class CodeSystemGraphBuilder {

    fun buildCodeSystemJGraphT(codeSystem: CodeSystem): DefaultListenableGraph<String, DefaultEdge> {
        val graph: Graph<String, DefaultEdge> =
            DirectedMultigraph(DefaultEdge::class.java)
        codeSystem.concept.forEach { c ->
            graph.addVertex(c.code)
        }
        codeSystem.concept.forEach { c ->
            val parentProperty = c.property.find { it.code == "parent" } ?: return@forEach
            val target = parentProperty.valueCodeType.code ?: return@forEach
            graph.addEdge(c.code, target)
        }
        return DefaultListenableGraph(graph)
    }

    fun buildCodeSystemGraphStream(codeSystem: CodeSystem, idSuffix: String = "right"): MultiGraph {
        val graph = MultiGraph("${codeSystem.id}-$idSuffix")
        codeSystem.concept.forEach { c ->
            graph.addNode(c.code).apply {
                setAttribute("ui.label", c.code)
            }
        }
        codeSystem.concept.forEach { c: CodeSystem.ConceptDefinitionComponent ->
            val parentProperty = c.property.find { it.code == "parent" } ?: return@forEach
            val target = parentProperty.valueCodeType.code ?: return@forEach
            val edgeId = "${c.code}-$target"
            graph.addEdge(edgeId, c.code, target, true)
        }
        return graph
    }
}