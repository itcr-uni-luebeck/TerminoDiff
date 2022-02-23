package terminodiff.terminodiff.engine.conceptmap

import androidx.compose.runtime.*
import org.hl7.fhir.r4.model.ConceptMap
import org.hl7.fhir.r4.model.ConceptMap.*
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Enumerations
import terminodiff.engine.resources.DiffDataContainer

class ConceptMapSuggester(
    diffDataContainer: DiffDataContainer,
) {
    val conceptMap by mutableStateOf(TerminodiffConceptMap(diffDataContainer))
}

class TerminodiffConceptMap(diffDataContainer: DiffDataContainer) {
    var canonicalUrl: String? by mutableStateOf(null)
    var version: String? by mutableStateOf(null)
    var name: String? by mutableStateOf(null)
    var title: String? by mutableStateOf(null)
    var group by mutableStateOf(ConceptMapGroup(diffDataContainer))

    val toFhir by derivedStateOf {
        ConceptMap().apply {
            this.url = this@TerminodiffConceptMap.canonicalUrl
            this.version = this@TerminodiffConceptMap.version
            this.name = this@TerminodiffConceptMap.version
            this.title = this@TerminodiffConceptMap.title
            this.dateElement = DateTimeType.now()
            this.group.add(this@TerminodiffConceptMap.group.toFhir)
        }
    }
}

class ConceptMapGroup(diffDataContainer: DiffDataContainer) {
    var sourceUri: String? by mutableStateOf(diffDataContainer.leftCodeSystem?.url)
    var sourceVersion: String? by mutableStateOf(diffDataContainer.leftCodeSystem?.version)
    var targetUri: String? by mutableStateOf(diffDataContainer.rightCodeSystem?.url)
    var targetVersion: String? by mutableStateOf(diffDataContainer.rightCodeSystem?.version)
    val elements = mutableStateListOf<ConceptMapElement>()

    val toFhir: ConceptMapGroupComponent by derivedStateOf {
        ConceptMapGroupComponent().apply {
            this.source = this@ConceptMapGroup.sourceUri
            this.sourceVersion = this@ConceptMapGroup.sourceVersion
            this.target = this@ConceptMapGroup.targetUri
            this.targetVersion = this@ConceptMapGroup.targetVersion
            this.element.addAll(this@ConceptMapGroup.elements.map { it.toFhir })
        }
    }
}

class ConceptMapElement {
    var code: String? by mutableStateOf(null)
    var display: String? by mutableStateOf(null)
    val targets = mutableStateListOf<ConceptMapTarget>()

    val toFhir: SourceElementComponent by derivedStateOf {
        SourceElementComponent().apply {
            this.code = this@ConceptMapElement.code
            this.display = this@ConceptMapElement.display
            this.target.addAll(this@ConceptMapElement.targets.map { it.toFhir })
        }
    }
}

class ConceptMapTarget {
    var code: String? by mutableStateOf(null)
    var display: String? by mutableStateOf(null)
    var equivalence: Enumerations.ConceptMapEquivalence by mutableStateOf(Enumerations.ConceptMapEquivalence.RELATEDTO)
    var comment: String? by mutableStateOf(null)

    val toFhir: TargetElementComponent by derivedStateOf {
        TargetElementComponent().apply {
            this.code = this@ConceptMapTarget.code
            this.display = this@ConceptMapTarget.display
            this.comment = this@ConceptMapTarget.comment
            this.equivalence = this@ConceptMapTarget.equivalence
        }
    }
}