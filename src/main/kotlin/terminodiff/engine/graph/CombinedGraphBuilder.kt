package terminodiff.terminodiff.engine.graph

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.jgrapht.Graph
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.jgrapht.traverse.AbstractGraphIterator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.graphs.ColorRegistry
import terminodiff.ui.graphs.Registry

private val logger: Logger = LoggerFactory.getLogger(CombinedGraphBuilder::class.java)

class CombinedGraphBuilder {

    fun getSubgraph(focusCode: String, layers: Int): CombinedGraph {
        val focusConcept =
            graph.nodeByCode(focusCode) ?: throw IllegalStateException("The focus concept is not in the combined graph")
        val diffEdgeTraversal = DiffEdgeTraversal(graph, focusConcept, layers)
        return diffEdgeTraversal.traverse()
    }

    val affectedEdges = mutableStateListOf<CombinedEdge>()
    val affectedVertices = mutableStateListOf<CombinedVertex>()

    fun populateAffected() {
        graph.edgeSet().filter { it.side != GraphSide.BOTH }.let {
            affectedEdges.clear()
            affectedEdges.addAll(it)
        }
        graph.vertexSet().filter { it.side != GraphSide.BOTH }.toSet().let { vertices ->
            affectedVertices.clear()
            val allVertices = vertices.plus(affectedEdges.mapNotNull { graph.nodeByCode(it.toCode) }).plus(affectedEdges.mapNotNull { graph.nodeByCode(it.fromCode) })
            affectedVertices.addAll(allVertices)
        }
    }

    val graph: CombinedGraph by mutableStateOf(emptyGraph())
}

private fun emptyGraph(): CombinedGraph =
    GraphTypeBuilder.directed<CombinedVertex, CombinedEdge>().allowingSelfLoops(false).weighted(false)
        .edgeClass(CombinedEdge::class.java).buildGraph()

data class CombinedEdge(
    val fromCode: String,
    val toCode: String,
    val property: String,
    val side: GraphSide,
) {
    val weight: Int
        get() = when (side) {
            GraphSide.LEFT, GraphSide.RIGHT -> 0
            else -> 1
        }

    fun getTooltip() = "'$fromCode' -> '$toCode' [$property]"

    fun getColor() = ColorRegistry.getColor(Registry.SIDES, side.name)
}

data class CombinedVertex(
    val code: String,
    val displayLeft: String? = null,
    val displayRight: String? = null,
    val side: GraphSide,
) {
    fun getTooltip(localizedStrings: LocalizedStrings): String = localizedStrings.displayAndInWhich_(getTooltip(), side)

    fun getTooltip(): String? = when (side) {
        GraphSide.LEFT -> displayLeft
        GraphSide.RIGHT -> displayRight
        else -> if (displayLeft == displayRight) displayRight else "$displayLeft vs. $displayRight"
    }

    fun getColor() = ColorRegistry.getColor(Registry.SIDES, side.name)
}

fun CombinedGraph.addCombinedEdge(edge: CombinedEdge) {
    val fromNode = this.vertexSet().find { it.code == edge.fromCode } ?: return
    val toNode = this.vertexSet().find { it.code == edge.toCode } ?: return
    this.addEdge(fromNode, toNode, edge)
}

fun CombinedGraph.getEdgesConnectedToVertex(vertex: CombinedVertex) = this.edgeSet().filter {
    it.fromCode == vertex.code || it.toCode == vertex.code
}

fun CombinedGraph.nodeByCode(code: String) = this.vertexSet().find { it.code == code }

typealias CombinedGraph = Graph<CombinedVertex, CombinedEdge>

class DiffEdgeTraversal(
    graph: CombinedGraph,
    val startingVertex: CombinedVertex,
    val radius: Int,
) : AbstractGraphIterator<CombinedVertex, CombinedEdge>(graph) {
    private val logger: Logger = LoggerFactory.getLogger(DiffEdgeTraversal::class.java)
    private val subgraph: CombinedGraph = emptyGraph()

    private val edgeStack = ArrayDeque<CombinedEdge>()
    private val visitedNodeDepths = mutableMapOf<CombinedVertex, Int>()
    private val visitedEdges = mutableSetOf<CombinedEdge>()
    var iteration = 0

    init {
        val edgesFromStarting = graph.getEdgesConnectedToVertex(startingVertex).sortedBy { it.toCode }
        visitedNodeDepths[startingVertex] = 1
        edgeStack.addAll(edgesFromStarting)
    }

    override fun hasNext(): Boolean = edgeStack.isNotEmpty()

    @Suppress("DuplicatedCode")
    override fun next(): CombinedVertex {
        val currentEdge = edgeStack.removeLast()
        visitedEdges.add(currentEdge)
        val sourceNode = graph.nodeByCode(currentEdge.fromCode)!!
        val targetNode = graph.nodeByCode(currentEdge.toCode)!!
        return if (sourceNode !in visitedNodeDepths.keys && targetNode in visitedNodeDepths.keys) {
            //flip the edge interpretation
            val flippedSourceDepth = visitedNodeDepths[targetNode]!!
            val flippedOutgoingEdgesUptoLevel = graph.getEdgesConnectedToVertex(sourceNode).filter {
                flippedSourceDepth + currentEdge.weight + it.weight <= radius
            }.filter { it !in visitedEdges }
            edgeStack.addAll(flippedOutgoingEdgesUptoLevel)
            visitedNodeDepths[sourceNode] = flippedSourceDepth + currentEdge.weight
            sourceNode
        } else {
            val sourceDepth = visitedNodeDepths[sourceNode]!!
            val outgoingEdgesUptoLevel = graph.getEdgesConnectedToVertex(targetNode).filter {
                sourceDepth + currentEdge.weight + it.weight <= radius
            }.filter { it !in visitedEdges }
            edgeStack.addAll(outgoingEdgesUptoLevel)
            visitedNodeDepths[targetNode] = sourceDepth + currentEdge.weight
            targetNode
        }
    }

    internal fun traverse(): CombinedGraph {
        while (hasNext()) {
            val nextNode = next()
            logger.debug("Iteration ${++iteration}")
            logger.debug("  - Next vertex: $nextNode")
            logger.debug("  - Edge stack (${edgeStack.count()}): ${edgeStack.joinToString()}")
            logger.debug("  - Node depths (${visitedNodeDepths.count()}): ${
                visitedNodeDepths.entries.joinToString {
                    "${it.key.code} (${it.key.side}): ${it.value}"
                }
            }")
            logger.debug("  - visited edges (${visitedEdges.count()}): ${visitedEdges.joinToString()}")
        }
        visitedNodeDepths.keys.forEach(subgraph::addVertex)
        visitedEdges.forEach(subgraph::addCombinedEdge)
        logger.info("Built subgraph for focus concept '${startingVertex.code}' (${startingVertex.side}) with " + "radius=$radius in $iteration iterations. Got ${subgraph.vertexSet().size} vertices and ${subgraph.edgeSet().size} edges.")
        return subgraph
    }
}