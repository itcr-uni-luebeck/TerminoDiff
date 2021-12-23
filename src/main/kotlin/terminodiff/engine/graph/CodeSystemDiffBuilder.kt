package terminodiff.engine.graph

import terminodiff.engine.concepts.ConceptDiff
import java.util.*

class CodeSystemDiffBuilder(
    private val leftBuilder: CodeSystemGraphBuilder,
    private val rightBuilder: CodeSystemGraphBuilder
) {

    val conceptDifferences = TreeMap<String, ConceptDiff>()
    val onlyInLeftConcepts = mutableListOf<String>()
    val onlyInRightConcepts = mutableListOf<String>()

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
        return this
    }

}