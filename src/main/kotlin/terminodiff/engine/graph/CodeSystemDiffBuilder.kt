package terminodiff.engine.graph

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import org.jgrapht.Graph
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.MetadataDiff
import terminodiff.ui.graphs.EdgeColorRegistry
import java.awt.Color
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("CodeSystemDiffBuilder")

class CodeSystemDiffBuilder(
    private val leftBuilder: CodeSystemGraphBuilder,
    private val rightBuilder: CodeSystemGraphBuilder,
    private val localizedStrings: LocalizedStrings
) {

    val metadataDifferences by derivedStateOf {
        MetadataDiff(leftBuilder.codeSystem, rightBuilder.codeSystem, localizedStrings).also { metadataDiff ->
            val count = metadataDiff.comparisons.count { it.result == MetadataDiff.MetadataComparisonResult.DIFFERENT }
            logger.info("Built metadata diff, $count difference(-s)")
        }
    }
    val conceptDifferences by mutableStateOf(TreeMap<String, ConceptDiff>())
    val onlyInLeftConcepts = mutableListOf<String>()
    val onlyInRightConcepts = mutableListOf<String>()
    val differenceGraph: Graph<DiffNode, DiffEdge> by derivedStateOf {
        GraphTypeBuilder.directed<DiffNode, DiffEdge>().allowingSelfLoops(true).allowingMultipleEdges(true)
            .weighted(false).edgeClass(DiffEdge::class.java).buildGraph()
    }

    fun build(): CodeSystemDiffBuilder {
        leftBuilder.nodeTree.mapNotNull { (code, leftConcept) ->
            // if not found in the rhs, we will add it to the concept diff immediately and continue
            val rightConcept = rightBuilder.nodeTree[code] ?: let {
                onlyInLeftConcepts.add(code)
                return@mapNotNull null
            }
            code to ConceptDiff.compareConcept(
                leftConcept = leftConcept,
                rightConcept = rightConcept,
                leftProperties = leftBuilder.simplePropertyCodeTypes,
                rightProperties = rightBuilder.simplePropertyCodeTypes
            )
        }.forEach { (code, conceptDiff) ->
            conceptDifferences[code] = conceptDiff
        }
        onlyInRightConcepts.addAll(rightBuilder.nodeTree.keys.filter { it !in conceptDifferences.keys })
        buildDiffGraph()
        logger.info(
            "Built diff graph, ${differenceGraph.vertexSet().count()} vertices, ${
                differenceGraph.edgeSet().count()
            } edges"
        )
        logger.info("only in left graph: ${onlyInLeftConcepts.size} concepts")
        logger.info("only in right graph: ${onlyInRightConcepts.size} concepts")
        logger.debug("Diff edges: {}", differenceGraph.edgeSet().joinToString("; "))
        return this
    }

    private fun edgesOnlyInX(
        graphBuilder: CodeSystemGraphBuilder, otherGraphBuilder: CodeSystemGraphBuilder, kind: DiffGraphElementKind
    ) =
        graphBuilder.graph.edgeSet().minus(otherGraphBuilder.graph.edgeSet()).also {
            logger.debug("only in $kind: {}", it.joinToString(separator = "; "))
        }.mapNotNull { edge ->
            val toConcept = graphBuilder.nodeTree[edge.to]
            val fromConcept = graphBuilder.nodeTree[edge.from]
            when {
                toConcept == null -> {
                    logger.warn("the code '${edge.to}' for property '${edge.propertyCode}' was not found in $kind")
                    return@mapNotNull null
                }
                fromConcept == null -> {
                    logger.warn("the code '${edge.from}' for property '${edge.propertyCode}' was not found in $kind")
                    return@mapNotNull null
                }
                else -> DiffEdge(
                    fromCode = edge.from,
                    fromDisplay = fromConcept.display,
                    toCode = edge.to,
                    toDisplay = toConcept.display,
                    propertyCode = edge.propertyCode,
                    inWhich = kind
                )
            }
        }

    private fun buildDiffGraph() {
        // add those vertices that are only in one of the graphs, this is easy
        differenceGraph.addAllVertices(onlyInLeftConcepts.map { code ->
            DiffNode(code, leftBuilder.nodeTree[code]!!.display, DiffGraphElementKind.LEFT)
        })
        differenceGraph.addAllVertices(onlyInRightConcepts.map { code ->
            DiffNode(code, rightBuilder.nodeTree[code]!!.display, DiffGraphElementKind.RIGHT)
        })

        val edgesOnlyInLeft = edgesOnlyInX(leftBuilder, rightBuilder, DiffGraphElementKind.LEFT)
        val edgesOnlyInRight = edgesOnlyInX(rightBuilder, leftBuilder, DiffGraphElementKind.RIGHT)

        addVerticesForEdges(edgesOnlyInLeft)
        addVerticesForEdges(edgesOnlyInRight)

        val diffEdges = edgesOnlyInLeft.plus(edgesOnlyInRight).map {
            val fromNode = differenceGraph.vertexSet().find { v -> v.code == it.fromCode }!!
            val toNode = differenceGraph.vertexSet().find { v -> v.code == it.toCode }!!
            Triple(fromNode, toNode, it)
        }

        differenceGraph.addAllEdges(diffEdges)
    }

    private fun addVerticesForEdges(edgeList: List<DiffEdge>) {
        edgeList.forEach {
            differenceGraph.addVertex(DiffNode(it.fromCode, it.fromDisplay, DiffGraphElementKind.BOTH))
            differenceGraph.addVertex(DiffNode(it.toCode, it.toDisplay, DiffGraphElementKind.BOTH))
        }
    }
}

fun <V, E> Graph<V, E>.addAllVertices(vertices: List<V>) = vertices.forEach(this::addVertex)
fun Graph<DiffNode, DiffEdge>.addAllEdges(edges: List<Triple<DiffNode, DiffNode, DiffEdge>>) =
    edges.forEach { this.addEdge(it.first, it.second, it.third) }

enum class DiffGraphElementKind {
    BOTH, LEFT, RIGHT
}

data class DiffNode(
    val code: String, val display: String?, val inWhich: DiffGraphElementKind
) {
    override fun hashCode(): Int = code.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiffNode

        if (code != other.code) return false

        return true
    }

    fun getTooltip(localizedStrings: LocalizedStrings) = localizedStrings.displayAndInWhich_(display, inWhich)
    fun getColor(): Color = EdgeColorRegistry.getDiffGraphColor(inWhich)
}

data class DiffEdge(
    val fromCode: String,
    val fromDisplay: String?,
    val toCode: String,
    val toDisplay: String?,
    val propertyCode: String,
    val inWhich: DiffGraphElementKind
) {
    fun getTooltip(): String = "'$fromCode' -> '$toCode' [$propertyCode]"
    fun getColor(): Color = EdgeColorRegistry.getDiffGraphColor(inWhich)
}