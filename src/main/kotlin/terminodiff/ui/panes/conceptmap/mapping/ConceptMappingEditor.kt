package terminodiff.terminodiff.ui.panes.conceptmap.mapping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hl7.fhir.r4.model.Enumerations.ConceptMapEquivalence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.i18n.LocalizedStrings
import terminodiff.java.ui.NeighborhoodJFrame
import terminodiff.terminodiff.engine.conceptmap.ConceptMapElement
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.engine.conceptmap.ConceptMapTarget
import terminodiff.terminodiff.ui.util.AutocompleteEditText
import terminodiff.terminodiff.ui.util.Dropdown
import terminodiff.terminodiff.ui.util.EditText
import terminodiff.terminodiff.ui.util.EditTextSpec
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable
import terminodiff.ui.util.columnSpecForMultiRow

private val logger: Logger = LoggerFactory.getLogger("ConceptMappingEditor")

@Composable
fun ConceptMappingEditorContent(
    localizedStrings: LocalizedStrings,
    conceptMapState: ConceptMapState,
    useDarkTheme: Boolean,
    allConceptCodes: List<String>,
) {
    val lazyListState = rememberLazyListState()
    val dividerColor = colorScheme.onSecondaryContainer
    val columnSpecs by derivedStateOf {
        getColumnSpecs(localizedStrings, useDarkTheme, dividerColor, allConceptCodes)
    }

    val columnHeight: Dp by derivedStateOf {
        conceptMapState.conceptMap.group.elements.map { it.targets.size + 1 }.plus(1).maxOf { it }.times(60).dp
    }
    Column(Modifier.background(colorScheme.tertiaryContainer).fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LazyTable(columnSpecs = columnSpecs,
            cellHeight = columnHeight,
            tableData = conceptMapState.conceptMap.group.elements,
            localizedStrings = localizedStrings,
            backgroundColor = colorScheme.tertiaryContainer,
            zebraStripingColor = colorScheme.primaryContainer,
            lazyListState = lazyListState,
            keyFun = { it.code.value })
    }
}

private fun getColumnSpecs(
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
    dividerColor: Color,
    allConceptCodes: List<String>,
): List<ColumnSpec<ConceptMapElement>> = listOf(codeColumnSpec(localizedStrings),
    displayColumnSpec(localizedStrings),
    actionsColumnSpec(
        localizedStrings,
        useDarkTheme,
    ),
    targetColumnSpec(localizedStrings, dividerColor, allConceptCodes),
    equivalenceColumnSpec(localizedStrings, dividerColor),
    commentsColumnSpec(localizedStrings, dividerColor))

private fun codeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec.StringSearchableColumnSpec<ConceptMapElement>(title = localizedStrings.code,
        weight = 0.1f,
        instanceGetter = { this.code.value })

private fun displayColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec.StringSearchableColumnSpec<ConceptMapElement>(title = localizedStrings.display,
        weight = 0.2f,
        instanceGetter = { this.display.value })

@OptIn(ExperimentalMaterial3Api::class)
private fun actionsColumnSpec(
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
) = ColumnSpec<ConceptMapElement>(title = localizedStrings.actions, weight = 0.08f) { element ->
    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        Row(Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
            IconButton(onClick = {
                showElementNeighborhood(element, useDarkTheme, localizedStrings)
            }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Hub, localizedStrings.graph)
            }
            IconButton(onClick = {
                element.targets.add(ConceptMapTarget())
                logger.debug("Added target for $element")
            }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.AddCircle, localizedStrings.addTarget)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun targetColumnSpec(localizedStrings: LocalizedStrings, dividerColor: Color, allConceptCodes: List<String>) =
    columnSpecForMultiRow<ConceptMapElement, ConceptMapTarget>(localizedStrings.target,
        weight = 0.2f,
        elementListGetter = { it.targets },
        dividerColor = dividerColor) { td, target ->
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                IconButton(onClick = {
                    td.targets.remove(target)
                    logger.debug("Removed target $target for $td")
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.RemoveCircle, localizedStrings.addTarget)
                }
            }
            AutocompleteEditText(
                autocompleteSuggestions = allConceptCodes,
                value = target.code.value,
                localizedStrings = localizedStrings,
                validateInput = { input ->
                    when (input) {
                        !in allConceptCodes -> EditTextSpec.ValidationResult.INVALID
                        else -> EditTextSpec.ValidationResult.VALID
                    }
                }
            ) { newCode ->
                target.code.value = newCode
            }
//            EditText(data = target,
//                weight = 0.9f,
//                spec = EditTextSpec(title = null, valueState = { target.code }, validation = { s ->
//                    when (s.trim().toLowerCase(Locale.current)) {
//                        in td.suitableTargets.map { it.code.toLowerCase(Locale.current) } -> EditTextSpec.ValidationResult.VALID
//                        in allConceptCodes.map { it.toLowerCase(Locale.current) } -> EditTextSpec.ValidationResult.WARN
//                        else -> EditTextSpec.ValidationResult.INVALID
//                    }
//                }), localizedStrings = localizedStrings)
        }

    }

private fun commentsColumnSpec(localizedStrings: LocalizedStrings, dividerColor: Color) =
    columnSpecForMultiRow<ConceptMapElement, ConceptMapTarget>(title = localizedStrings.comments,
        weight = 0.2f,
        elementListGetter = { it.targets },
        dividerColor = dividerColor) { _, target ->
        EditText(data = target,
            spec = EditTextSpec(title = null, valueState = { comment }, validation = null),
            localizedStrings = localizedStrings)
    }

private fun equivalenceColumnSpec(
    localizedStrings: LocalizedStrings,
    dividerColor: Color,
): ColumnSpec<ConceptMapElement> {
    return columnSpecForMultiRow(title = localizedStrings.equivalence,
        weight = 0.2f,
        elementListGetter = { it.targets },
        dividerColor = dividerColor) { _, target ->
        Dropdown(
            elements = ConceptMapEquivalenceDisplay.values().toList(),
            elementDisplay = { it.displayIndent() },
            textFieldDisplay = { it.display },
            fontStyle = { if (it.recommendedUse) FontStyle.Normal else FontStyle.Italic },
            selectedElement = ConceptMapEquivalenceDisplay.fromEquivalence(target.equivalence.value),
        ) { newValue ->
            target.equivalence.value = newValue.equivalence
        }
    }
}

private fun showElementNeighborhood(
    focusElement: ConceptMapElement,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
) {
    val neighborhoodDisplay = focusElement.neighborhood
    NeighborhoodJFrame(
        /* graph = */ neighborhoodDisplay.getNeighborhoodGraph(),
        /* focusCode = */ neighborhoodDisplay.focusCode,
        /* isDarkTheme = */ useDarkTheme,
        /* localizedStrings = */ localizedStrings,
        /* frameTitle = */ localizedStrings.graphFor_.invoke(focusElement.code.value)).apply {
        addClickListener { delta ->
            val newValue = neighborhoodDisplay.changeLayers(delta)
            this.setGraph(neighborhoodDisplay.getNeighborhoodGraph())
            newValue
        }
    }
}

enum class ConceptMapEquivalenceDisplay(
    val level: Int,
    val display: String,
    val equivalence: ConceptMapEquivalence,
    val recommendedUse: Boolean = false,
) {
    RELATEDTO(0, "Related To", ConceptMapEquivalence.RELATEDTO, true), EQUIVALENT(1,
        "Equivalent",
        ConceptMapEquivalence.EQUIVALENT,
        true),
    EQUAL(2, "Equal", ConceptMapEquivalence.EQUAL), WIDER(1, "Wider", ConceptMapEquivalence.WIDER, true), SUBSUMES(1,
        "Subsumes",
        ConceptMapEquivalence.SUBSUMES),
    NARROWER(1, "Narrower", ConceptMapEquivalence.NARROWER, true), SPECIALIZES(1,
        "Specializes",
        ConceptMapEquivalence.SPECIALIZES),
    INEXACT(1, "Inexact", ConceptMapEquivalence.INEXACT), UNMATCHED(0,
        "Unmatched",
        ConceptMapEquivalence.UNMATCHED),
    DISJOINT(1, "Disjoint", ConceptMapEquivalence.DISJOINT, true);

    fun displayIndent(): String = "${" ".repeat(this.level * 4)}${this.display}"

    companion object {
        fun fromEquivalence(equivalence: ConceptMapEquivalence?): ConceptMapEquivalenceDisplay? =
            equivalence?.let { valueOf(it.name) }
    }
}

