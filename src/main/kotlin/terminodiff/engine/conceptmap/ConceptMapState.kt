package terminodiff.terminodiff.engine.conceptmap

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import org.hl7.fhir.r4.model.ConceptMap
import org.hl7.fhir.r4.model.ConceptMap.*
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Enumerations
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.graph.GraphSide
import terminodiff.terminodiff.ui.panes.diff.NeighborhoodDisplay

class ConceptMapState(
    diffDataContainer: DiffDataContainer,
) {
    val conceptMap by mutableStateOf(TerminodiffConceptMap(diffDataContainer))
}

class TerminodiffConceptMap(diffDataContainer: DiffDataContainer) {

    val id: MutableState<String?> = mutableStateOf(null)
    val canonicalUrl: MutableState<String?> = mutableStateOf(null)
    val version: MutableState<String?> = mutableStateOf(null)
    val name: MutableState<String?> =
        mutableStateOf(null) // TODO: 28/02/22 generate this from the metadata as a suggestion
    val title: MutableState<String?> =
        mutableStateOf(null) // TODO: 28/02/22 generate this from the metadata as a suggestion
    val sourceValueSet: MutableState<String?> =
        mutableStateOf(null) // TODO: 28/02/22 generate this from the mapped concepts
    val targetValueSet: MutableState<String?> =
        mutableStateOf(null) // TODO: 28/02/22 generate this from the concepts that are being mapped to
    var group by mutableStateOf(ConceptMapGroup(diffDataContainer))


    val toFhir by derivedStateOf {
        ConceptMap().apply {
            this.id = this@TerminodiffConceptMap.id.value
            this.url = this@TerminodiffConceptMap.canonicalUrl.value
            this.version = this@TerminodiffConceptMap.version.value
            this.name = this@TerminodiffConceptMap.version.value
            this.title = this@TerminodiffConceptMap.title.value
            this.dateElement = DateTimeType.now()
            this.group.add(this@TerminodiffConceptMap.group.toFhir)
        }
    }

    override fun toString(): String {
        return "TerminodiffConceptMap(id=${id.value}, canonicalUrl=${canonicalUrl.value}, version=${version.value}, name=${name.value}, title=${title.value}, sourceValueSet=${sourceValueSet.value}, targetValueSet=${targetValueSet.value})"
    }
}

class ConceptMapGroup(diffDataContainer: DiffDataContainer) {
    val sourceUri = mutableStateOf(diffDataContainer.leftCodeSystem?.url)
    val sourceVersion = mutableStateOf(diffDataContainer.leftCodeSystem?.version)
    val targetUri = mutableStateOf(diffDataContainer.rightCodeSystem?.url)
    val targetVersion = mutableStateOf(diffDataContainer.rightCodeSystem?.version)
    val elements = mutableStateListOf<ConceptMapElement>()

    init {
        populateElements(diffDataContainer)
    }

    private fun populateElements(diff: DiffDataContainer) {
        diff.codeSystemDiff!!.combinedGraph!!.affectedVertices.forEach { vertex ->
            elements.add(ConceptMapElement(diff, vertex.code, vertex.getTooltip()))
        }
//        diff.codeSystemDiff!!.onlyInLeftConcepts.map { code ->
//            val leftConcept = diff.leftGraphBuilder!!.nodeTree[code]!!
//            elements.add(ConceptMapElement(diff, code, leftConcept.display))
//        }
    }

    override fun toString(): String {
        return "ConceptMapGroup(sourceUri=${sourceUri.value}, sourceVersion=${sourceVersion.value}, targetUri=${targetUri.value}, targetVersion=${targetVersion.value})"
    }

    val toFhir: ConceptMapGroupComponent by derivedStateOf {
        ConceptMapGroupComponent().apply {
            this.source = this@ConceptMapGroup.sourceUri.value
            this.sourceVersion = this@ConceptMapGroup.sourceVersion.value
            this.target = this@ConceptMapGroup.targetUri.value
            this.targetVersion = this@ConceptMapGroup.targetVersion.value
            this.element.addAll(this@ConceptMapGroup.elements.map { it.toFhir }
                .filter(SourceElementComponent::hasTarget))
        }
    }
}

class ConceptMapElement(diffDataContainer: DiffDataContainer, code: String, display: String?) {
    val code: MutableState<String> = mutableStateOf(code)
    val display: MutableState<String?> = mutableStateOf(display)

    val neighborhood by derivedStateOf {
        NeighborhoodDisplay(this.code.value, diffDataContainer.codeSystemDiff!!)
    }

    val suitableTargets by derivedStateOf {
        // the list of targets is calculated from the neighborhood graph of the current vertex
        neighborhood.getNeighborhoodGraph().vertexSet().filter { it.code != code } // the node itself can't be mapped to
            .filter { it.side == GraphSide.BOTH } // we can only map to nodes that are shared across versions
            .filter { v ->
                val linkingEdges = diffDataContainer.codeSystemDiff!!.combinedGraph!!.graph.edgeSet().filter { e ->
                    (v.code == e.toCode && code == e.fromCode) || (v.code == e.fromCode && code == e.toCode)
                }
                return@filter linkingEdges.any { it.side != GraphSide.BOTH }
            } // if the edge that links the current node, and the `v` node, is in both, disregard this node
    }

    val targets = mutableStateListOf<ConceptMapTarget>().apply {
        suitableTargets.forEach { t ->
            this.add(ConceptMapTarget(diffDataContainer).apply {
                this.code.value = t.code
                // TODO: 01/03/22 infer the equivalence as a best guess
                this.equivalence.value = when {
                    else -> null
                }
            })
        }
    }

    val toFhir: SourceElementComponent by derivedStateOf {
        SourceElementComponent().apply {
            this.code = this@ConceptMapElement.code.value
            this.display = this@ConceptMapElement.display.value
            this.target.addAll(this@ConceptMapElement.targets.filter { it.equivalence.value != null }.map { it.toFhir })
        }
    }

    override fun toString(): String {
        return "ConceptMapElement(code=${code.value}, display=${display.value})"
    }
}

class ConceptMapTarget(diffDataContainer: DiffDataContainer) {
    val code: MutableState<String?> = mutableStateOf(null)
    val display: String? by derivedStateOf {
        code.value?.let { c -> diffDataContainer.rightGraphBuilder?.nodeTree?.get(c)?.display }
    }
    val equivalence: MutableState<Enumerations.ConceptMapEquivalence?> = mutableStateOf(null)
    val comment: MutableState<String?> = mutableStateOf(null)

    var isAutomaticallySet by mutableStateOf(true)
    private val valid by derivedStateOf {
        code.value != null && (!isAutomaticallySet && equivalence.value != null) || (isAutomaticallySet)
    }

    val state by derivedStateOf {
        when {
            !valid -> MappingState.INVALID
            valid && isAutomaticallySet -> MappingState.AUTO
            else -> MappingState.VALID
        }
    }


    val toFhir: TargetElementComponent by derivedStateOf {
        TargetElementComponent().apply {
            this.code = this@ConceptMapTarget.code.value
            this.display = this@ConceptMapTarget.display
            this.comment = this@ConceptMapTarget.comment.value
            this.equivalence = this@ConceptMapTarget.equivalence.value
        }
    }

    enum class MappingState(val image: ImageVector, val description: LocalizedStrings.() -> String) {
        AUTO(Icons.Default.AutoAwesome, { automatic }),
        VALID(Icons.Default.Verified, { ok }),
        INVALID(Icons.Default.Error, { invalid })
    }

    override fun toString(): String {
        return "ConceptMapTarget(code=${code.value}, display=${display}, equivalence=${equivalence.value}, comment=${comment.value}, state=${state})"
    }
}