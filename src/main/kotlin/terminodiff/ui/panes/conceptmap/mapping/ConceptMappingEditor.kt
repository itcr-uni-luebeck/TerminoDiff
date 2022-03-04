@file:OptIn(ExperimentalMaterial3Api::class)

package terminodiff.terminodiff.ui.panes.conceptmap.mapping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hl7.fhir.r4.model.Enumerations.ConceptMapEquivalence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.java.ui.NeighborhoodJFrame
import terminodiff.terminodiff.engine.conceptmap.ConceptMapElement
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.engine.conceptmap.ConceptMapTarget
import terminodiff.terminodiff.ui.util.AutocompleteEditText
import terminodiff.terminodiff.ui.util.Dropdown
import terminodiff.terminodiff.ui.util.EditText
import terminodiff.terminodiff.ui.util.EditTextSpec
import terminodiff.ui.MouseOverPopup
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable
import terminodiff.ui.util.columnSpecForMultiRow
import java.util.*
import javax.swing.JOptionPane

private val logger: Logger = LoggerFactory.getLogger("ConceptMappingEditor")

@Composable
fun ConceptMappingEditorContent(
    localizedStrings: LocalizedStrings,
    conceptMapState: ConceptMapState,
    diffDataContainer: DiffDataContainer,
    useDarkTheme: Boolean,
    allConceptCodes: SortedMap<String, String>,
) {
    val lazyListState = rememberLazyListState()
    val dividerColor = colorScheme.onSecondaryContainer
    val columnSpecs by derivedStateOf {
        getColumnSpecs(diffDataContainer, localizedStrings, useDarkTheme, dividerColor, allConceptCodes)
    }

    val columnHeight: Dp by derivedStateOf {
        conceptMapState.conceptMap.group.elements.map { it.targets.size + 1 }.plus(1).maxOf { it }.times(60).dp
    }
    Column(Modifier.background(colorScheme.tertiaryContainer).fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MappingStatus(conceptMapState, localizedStrings)
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

@Composable
fun MappingStatus(conceptMapState: ConceptMapState, localizedStrings: LocalizedStrings) {
    val elements by derivedStateOf { conceptMapState.conceptMap.group.elements }
    val mappableCount by derivedStateOf { elements.size }
    val automappedCount by derivedStateOf {
        elements.sumOf { it.targets.count { t -> t.isAutomaticallySet } }
    }
    val validCount by derivedStateOf {
        elements.sumOf { it.targets.count { t -> t.state == ConceptMapTarget.MappingState.VALID } }
    }
    Row(Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically) {
        Text(buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(localizedStrings.mappableCount_(mappableCount))
            }
            append("; ")
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(localizedStrings.automappedCount_(automappedCount))
            }
            append("; ")
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(localizedStrings.validAcceptedCount_(validCount))
            }
        }, color = colorScheme.onTertiaryContainer, style = typography.titleLarge)

        Button(onClick = {
            askAcceptAll(conceptMapState, localizedStrings)
        }, colors = ButtonDefaults.buttonColors(backgroundColor = colorScheme.primary), enabled = automappedCount > 0) {
            Icon(Icons.Default.DoneAll, localizedStrings.acceptAll, tint = colorScheme.onPrimary)
            Text(text = localizedStrings.acceptAll, color = colorScheme.onPrimary)
        }
    }
}

private fun askAcceptAll(conceptMapState: ConceptMapState, localizedStrings: LocalizedStrings) {
    val options = listOf(localizedStrings.no, localizedStrings.yes).toTypedArray()
    when (JOptionPane.showOptionDialog(/* parentComponent = */ null,
        /* message = */ localizedStrings.reallyAcceptAll,
        /* title = */ localizedStrings.areYouSure,
        /* optionType = */ JOptionPane.YES_NO_OPTION,
        /* messageType = */ JOptionPane.QUESTION_MESSAGE,
        /* icon = */ null,
        /* options = */ options,
        /* initialValue = */ options[0])) {
        options.indexOf(localizedStrings.yes) -> {
            conceptMapState.acceptAll()
        }
    }
}

private fun getColumnSpecs(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
    dividerColor: Color,
    allConceptCodes: SortedMap<String, String>,
): List<ColumnSpec<ConceptMapElement>> = listOf(codeColumnSpec(localizedStrings),
    displayColumnSpec(localizedStrings),
    actionsColumnSpec(diffDataContainer, localizedStrings, useDarkTheme),
    equivalenceColumnSpec(localizedStrings, dividerColor),
    targetColumnSpec(localizedStrings, dividerColor, allConceptCodes),
    commentsColumnSpec(localizedStrings, dividerColor),
    targetStatusColumnSpec(localizedStrings, dividerColor))

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
    diffDataContainer: DiffDataContainer,
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
                element.targets.add(ConceptMapTarget(diffDataContainer).apply {
                    isAutomaticallySet = false
                })
                logger.debug("Added target for $element")
            }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.AddCircle, localizedStrings.addTarget)
            }
        }
    }
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
            target.isAutomaticallySet = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun targetColumnSpec(
    localizedStrings: LocalizedStrings,
    dividerColor: Color,
    allConceptCodes: SortedMap<String, String>,
) = columnSpecForMultiRow<ConceptMapElement, ConceptMapTarget>(localizedStrings.target,
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
        AutocompleteEditText(autocompleteSuggestions = allConceptCodes,
            value = target.code.value,
            localizedStrings = localizedStrings,
            validateInput = { input ->
                when (input) {
                    !in allConceptCodes -> EditTextSpec.ValidationResult.INVALID
                    else -> EditTextSpec.ValidationResult.VALID
                }
            }) { newCode ->
            target.code.value = newCode
            target.isAutomaticallySet = false
        }
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

private fun targetStatusColumnSpec(localizedStrings: LocalizedStrings, dividerColor: Color) =
    columnSpecForMultiRow<ConceptMapElement, ConceptMapTarget>(title = localizedStrings.status,
        weight = 0.08f,
        elementListGetter = { it.targets },
        dividerColor = dividerColor) { _, target ->
        val description = target.state.description.invoke(localizedStrings)
        Column(Modifier.height(56.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            MouseOverPopup(text = description) {
                val icon: @Composable (Color?) -> Unit =
                    { Icon(target.state.image, description, tint = it ?: LocalContentColor.current) }
                if (target.state == ConceptMapTarget.MappingState.AUTO) {
                    FloatingActionButton(modifier = Modifier.size(48.dp),
                        onClick = { target.isAutomaticallySet = false },
                        backgroundColor = colorScheme.tertiary) {
                        icon(colorScheme.onTertiary)
                    }
                } else {
                    icon(null)
                }

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

