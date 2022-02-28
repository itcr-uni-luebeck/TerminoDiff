package terminodiff.terminodiff.engine.conceptmap

import androidx.compose.runtime.*
import org.hl7.fhir.r4.model.ConceptMap
import org.hl7.fhir.r4.model.ConceptMap.*
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Enumerations
import terminodiff.engine.resources.DiffDataContainer

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
        diff.codeSystemDiff!!.onlyInLeftConcepts.map { code ->
            val leftConcept = diff.leftGraphBuilder!!.nodeTree[code]!!
            elements.add(ConceptMapElement().apply {
                this.code.value = code
                this.display.value = leftConcept.display
            })
        }
    }

    val toFhir: ConceptMapGroupComponent by derivedStateOf {
        ConceptMapGroupComponent().apply {
            this.source = this@ConceptMapGroup.sourceUri.value
            this.sourceVersion = this@ConceptMapGroup.sourceVersion.value
            this.target = this@ConceptMapGroup.targetUri.value
            this.targetVersion = this@ConceptMapGroup.targetVersion.value
            this.element.addAll(this@ConceptMapGroup.elements.map { it.toFhir })
        }
    }
}

class ConceptMapElement {
    val code: MutableState<String?> = mutableStateOf(null)
    val display: MutableState<String?> = mutableStateOf(null)
    val targets = mutableStateListOf<ConceptMapTarget>()

    val toFhir: SourceElementComponent by derivedStateOf {
        SourceElementComponent().apply {
            this.code = this@ConceptMapElement.code.value
            this.display = this@ConceptMapElement.display.value
            this.target.addAll(this@ConceptMapElement.targets.map { it.toFhir })
        }
    }
}

class ConceptMapTarget {
    val code: MutableState<String?> = mutableStateOf(null)
    val display: MutableState<String?> = mutableStateOf(null)
    val equivalence: MutableState<Enumerations.ConceptMapEquivalence> =
        mutableStateOf(Enumerations.ConceptMapEquivalence.RELATEDTO)
    val comment: MutableState<String?> = mutableStateOf(null)

    val toFhir: TargetElementComponent by derivedStateOf {
        TargetElementComponent().apply {
            this.code = this@ConceptMapTarget.code.value
            this.display = this@ConceptMapTarget.display.value
            this.comment = this@ConceptMapTarget.comment.value
            this.equivalence = this@ConceptMapTarget.equivalence.value
        }
    }
}