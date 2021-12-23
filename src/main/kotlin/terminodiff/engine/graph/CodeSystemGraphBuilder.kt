package terminodiff.engine.graph

import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Coding
import org.jgrapht.Graph
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.ui.graphs.EdgeColorRegistry
import java.awt.Color
import java.util.*

val logger: Logger = LoggerFactory.getLogger(CodeSystemGraphBuilder::class.java)

enum class CodeSystemRole {
    LEFT, RIGHT
}

typealias PropertyMap = Map<String, CodeSystem.PropertyType>

class CodeSystemGraphBuilder(
    val codeSystem: CodeSystem, val codeSystemRole: CodeSystemRole
) {

    // store more detailed node data in a red-black tree, which can retrieve nodes in O(log n)
    val nodeTree = TreeMap<String, FhirConceptDetails>()

    /**
     * we collect those properties that can't map to a concept within the same code system
     * and add implicit properties that may not appear in the explicit list of properties (c.f. below and
     * http://www.hl7.org/fhir/codesystem-concept-properties.html)
     */
    val simplePropertyCodeTypes: PropertyMap = codeSystem.property.asSequence().filter {
        it.hasType() && it.type != CodeSystem.PropertyType.CODE
    }.map { it.code to it.type }.toSet().plus("inactive" to CodeSystem.PropertyType.CODE)
        .plus("deprecated" to CodeSystem.PropertyType.DATETIME).plus("notSelectable" to CodeSystem.PropertyType.BOOLEAN)
        .toMap()

    /**
     * properties that have a "code" type map to other nodes in the CS graph.
     * the "parent" and "child" properties are implicit in FHIR R4 and will (likely) not appear in the
     * list of properties within the CS. Hence, by converting to a set and adding parent and child,
     * they will appear in the list exactly once
     */
    val edgePropertyCodes: List<String> = codeSystem.property.asSequence().filter {
        it.hasType() && it.type == CodeSystem.PropertyType.CODE
    }.map { it.code }.toHashSet().plus("parent").plus("child").toList()

    val graph: Graph<String, FhirConceptEdge> =
        GraphTypeBuilder.directed<String, FhirConceptEdge>().allowingMultipleEdges(true).allowingSelfLoops(true)
            .edgeClass(FhirConceptEdge::class.java).weighted(false).buildGraph().also {
                generateNodesAndEdges(it, edgePropertyCodes, simplePropertyCodeTypes)
            }

    private fun generateNodesAndEdges(
        theGraph: Graph<String, FhirConceptEdge>,
        edgePropertyCodes: List<String>,
        simplePropertyCodeTypes: PropertyMap
    ) {
        codeSystem.concept.forEach { c ->
            val from = c.code!!
            if (theGraph.addVertex(from)) logger.debug("added $from")
            val conceptProperties = c.property.mapNotNull { p ->
                if (p.code in edgePropertyCodes) {
                    val to = p.valueCodeType.code
                        ?: throw UnsupportedOperationException("property ${p.code} for concept $from has no valueCode")
                    when (p.code) {
                        "child" -> addEdge(
                            theGraph = theGraph, from = to, to = from, code = "parent", logSuffix = "child edge"
                        ) // inverse order, since parent and child edges are semantically
                        // interchangeable, and dealing only with one kind is easier downstream
                        else -> addEdge(theGraph, from, to, p.code, "${p.code} edge")
                    }

                    return@mapNotNull null // we have dealt with this property sufficiently, edge comparisons are
                    // differently handled to other property comparisons
                }
                val basePropertyType = simplePropertyCodeTypes[p.code]
                    ?: throw UnsupportedOperationException("The property ${p.code} is not declared in the CodeSystem, and not implicit.")
                FhirConceptSimpleProperty(p.code, basePropertyType, p.value.toString())
            }
            c.concept?.forEach { ch ->
                val to = ch.code
                addEdge(theGraph, to, from, "parent", "child edge from concept") // see above
            }
            // store more detailed node data in a red-black tree, which can retrieve nodes in O(log n)
            nodeTree[from] = FhirConceptDetails(
                code = from, display = c.display, definition = c.definition, designation = c.designation.map { des ->
                    FhirConceptDesignation(
                        language = des.language, use = des.use, value = des.value
                    )
                }, property = conceptProperties
            )
        }
    }

    private fun addEdge(
        theGraph: Graph<String, FhirConceptEdge>, from: String, to: String, code: String, logSuffix: String
    ) {
        if (theGraph.addVertex(from)) logger.debug("added origin node $from")
        if (theGraph.addVertex(to)) // if already exists, no problem
            logger.debug("added target node $to")
        if (theGraph.addEdge(
                from,
                to,
                FhirConceptEdge(from, to, code)
            )
        ) logger.debug("added $code edge '$from' -> '$to' [$logSuffix]")
    }
}

data class FhirConceptNode(
    val code: String, var role: CodeSystemRole
)

data class FhirConceptEdge(
    val from: String, val to: String, val propertyCode: String
) {
    override fun toString(): String = propertyCode

    fun getLabel(): String = "'$from' -> '$to' [$propertyCode]"

    fun getColor(): Color = EdgeColorRegistry.getColor(propertyCode)
}

data class FhirConceptDetails(
    val code: String,
    val display: String?,
    val definition: String?,
    val designation: List<FhirConceptDesignation>?,
    val property: List<FhirConceptSimpleProperty>?
)

data class FhirConceptDesignation(
    val language: String, val use: Coding, val value: String
)

data class FhirConceptSimpleProperty(
    val propertyCode: String, val type: CodeSystem.PropertyType, val value: String
)