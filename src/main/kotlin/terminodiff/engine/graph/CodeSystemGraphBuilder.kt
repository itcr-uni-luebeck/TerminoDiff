package terminodiff.engine.graph

import org.hl7.fhir.r4.model.*
import org.jgrapht.Graph
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.graphs.EdgeColorRegistry
import java.awt.Color
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(CodeSystemGraphBuilder::class.java)

typealias PropertyMap = Map<String, CodeSystem.PropertyType>

class CodeSystemGraphBuilder(
    val codeSystem: CodeSystem,
    private val localizedStrings: LocalizedStrings,
) {

    // store more detailed node data in a red-black tree, which can retrieve nodes in O(log n)
    val nodeTree = TreeMap<String, FhirConceptDetails>()

    /**
     * we collect those properties that can't map to a concept within the same code system
     * and add implicit properties that may not appear in the explicit list of properties (c.f. below and
     * http://www.hl7.org/fhir/codesystem-concept-properties.html)
     */
    val simplePropertyCodeTypes: PropertyMap = codeSystem.property.asSequence().filter {
        it.hasType()// && it.type != CodeSystem.PropertyType.CODE
    }.map { it.code to it.type }.toSet().plus("inactive" to CodeSystem.PropertyType.CODE)
        .plus("deprecated" to CodeSystem.PropertyType.DATETIME).plus("notSelectable" to CodeSystem.PropertyType.BOOLEAN)
        .plus("parent" to CodeSystem.PropertyType.CODE).plus("child" to CodeSystem.PropertyType.CODE)
        .toMap()

    /**
     * properties that have a "code" type map to other nodes in the CS graph.
     * the "parent" and "child" properties are implicit in FHIR R4 and will (likely) not appear in the
     * list of properties within the CS. Hence, by converting to a set and adding parent and child,
     * they will appear in the list exactly once
     */
    private val edgePropertyCodes: List<String> = codeSystem.property.asSequence().filter {
        it.hasType() && it.type == CodeSystem.PropertyType.CODE
    }.map { it.code }.toHashSet().plus("parent").plus("child").toList()

    val graph: Graph<String, FhirConceptEdge> =
        GraphTypeBuilder.directed<String, FhirConceptEdge>().allowingMultipleEdges(true).allowingSelfLoops(true)
            .edgeClass(FhirConceptEdge::class.java).weighted(false).buildGraph().also {
                generateNodesAndEdges(it, edgePropertyCodes, simplePropertyCodeTypes, localizedStrings)
            }

    private fun generateNodesAndEdges(
        theGraph: Graph<String, FhirConceptEdge>,
        edgePropertyCodes: List<String>,
        simplePropertyCodeTypes: PropertyMap,
        localizedStrings: LocalizedStrings,
    ) {
        val allCodes = codeSystem.concept.map { it.code }
        codeSystem.concept.forEach { c ->
            val from = c.code!!
            if (theGraph.addVertex(from)) logger.debug("added $from")
            val conceptProperties = c.property.map { p ->
                when (p.code) {
                    in edgePropertyCodes -> {
                        val to = p.valueCodeType.code
                            ?: throw UnsupportedOperationException("property ${p.code} for concept $from has no valueCode")
                        when {
                            p.code == "child" -> addEdge(
                                theGraph = theGraph, from = to, to = from, code = "parent", logSuffix = "child edge"
                                // inverse order, since parent and child edges are semantically
                                // interchangeable, and dealing only with one kind is easier downstream
                            )
                            to !in allCodes -> {
                                logger.debug("ignoring property '${p.code}' for concept ${c.code} -> value '$to' is not a code")
                                //this is not an edge, but something like kind=category°
                            }
                            else -> addEdge(theGraph, from, to, p.code, "${p.code} edge")
                        }
                    }
                }
                val basePropertyType = simplePropertyCodeTypes[p.code]
                    ?: throw UnsupportedOperationException("The property ${p.code} is not declared in the CodeSystem, and not implicit.")
                val propertyValue = getPropertyValue(p.value, localizedStrings)
                FhirConceptProperty(p.code, basePropertyType, propertyValue)
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
        theGraph: Graph<String, FhirConceptEdge>, from: String, to: String, code: String, logSuffix: String,
    ) {
        if (theGraph.addVertex(from)) logger.debug("added origin node $from")
        if (theGraph.addVertex(to)) // if already exists, no problem
            logger.debug("added target node $to")
        if (theGraph.addEdge(
                from, to, FhirConceptEdge(from, to, code)
            )
        ) logger.debug("added $code edge '$from' -> '$to' [$logSuffix]")
    }
}

data class FhirConceptEdge(
    val from: String, val to: String, val propertyCode: String,
) {
    fun getLabel(): String = "'$from' -> '$to' [$propertyCode]"

    fun getColor(): Color = EdgeColorRegistry.getColor(propertyCode)
}

data class FhirConceptDetails(
    val code: String,
    val display: String?,
    val definition: String?,
    val designation: List<FhirConceptDesignation>?,
    val property: List<FhirConceptProperty>?,
)

data class FhirConceptDesignation(
    val language: String?, val use: Coding?, val value: String,
)

data class FhirConceptProperty(
    val propertyCode: String, val type: CodeSystem.PropertyType, val value: String?,
)

private fun getPropertyValue(type: Type?, localizedStrings: LocalizedStrings): String? = when (type) {
    null -> null
    is CodeType -> type.code
    is Coding -> "${type.code} (${type.system}): '${type.display}'"
    is StringType -> type.value
    is IntegerType -> type.valueAsString
    is BooleanType -> localizedStrings.boolean_.invoke(type.value)
    is DateType -> type.valueAsString
    is DecimalType -> type.valueAsString
    else -> type.toString()
}