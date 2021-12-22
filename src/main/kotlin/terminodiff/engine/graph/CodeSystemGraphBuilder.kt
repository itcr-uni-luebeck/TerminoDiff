package terminodiff.engine.graph

import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Coding
import org.jgrapht.Graph
import org.jgrapht.graph.builder.GraphTypeBuilder

class CodeSystemGraphBuilder {

    //    fun buildCodeSystemJGraphT(codeSystem: CodeSystem): DefaultListenableGraph<String, DefaultEdge> {
//        val graph: Graph<String, DefaultEdge> =
//            DirectedMultigraph(DefaultEdge::class.java)
//        codeSystem.concept.forEach { c ->
//            graph.addVertex(c.code)
//        }
//        codeSystem.concept.forEach { c ->
//            val parentProperty = c.property.find { it.code == "parent" } ?: return@forEach
//            val target = parentProperty.valueCodeType.code ?: return@forEach
//            graph.addEdge(c.code, target)
//        }
//        return DefaultListenableGraph(graph)
//    }
    fun buildCodeSystemJGraphT(codeSystem: CodeSystem): Graph<FhirConceptNode, FhirConceptEdge> {
        val graph: Graph<FhirConceptNode, FhirConceptEdge> = GraphTypeBuilder
            .directed<FhirConceptNode, FhirConceptEdge>()
            .allowingMultipleEdges(true)
            .allowingSelfLoops(true)
            .edgeClass(FhirConceptEdge::class.java)
            .weighted(false)
            .buildGraph()

        val simplePropertyCodeTypes = codeSystem.property.filter {
            it.hasType() && it.type != CodeSystem.PropertyType.CODE
        }.map { it.code to it.type }

        val edgePropertyCodes: Set<String> = codeSystem.property.asSequence().filter {
            it.hasType() && it.type == CodeSystem.PropertyType.CODE
        }.map { it.code }.toHashSet().plus("parent").plus("child")

        codeSystem.concept.forEach { c ->
            graph.addVertex(
                FhirConceptNode(
                    code = c.code,
                    display = c.display,
                    definition = c.definition,
                    designation = c.designation.map { des ->
                        FhirConceptDesignation(
                            language = des.language,
                            use = des.use,
                            value = des.value
                        )
                    },
                    property = listOf() //todo
                )
            )
        }
        codeSystem.concept.forEach { c ->
            c.property.forEach { prop ->
                val propertyCode = prop.code
            }
            //val parentProperty = c.property.find { it.code == "parent" } ?: return@forEach
            //val target = parentProperty.valueCodeType.code ?: return@forEach
            //graph.addEdge(c.code, target)
        }

        return graph
    }
}

data class FhirConceptNode(
    val code: String,
    val display: String?,
    val definition: String?,
    val designation: List<FhirConceptDesignation>?,
    val property: List<FhirConceptSimpleProperty>?
)

data class FhirConceptDesignation(
    val language: String,
    val use: Coding,
    val value: String
)

data class FhirConceptSimpleProperty(
    val propertyCode: String,
    val type: CodeSystem.PropertyType,
    val value: String
)

data class FhirConceptEdge(
    val propertyCode: String
)