package terminodiff.terminodiff.ui.panes.conceptmap.mapping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hl7.fhir.r4.model.Enumerations.ConceptMapEquivalence
import terminodiff.engine.graph.CodeSystemDiffBuilder
import terminodiff.i18n.LocalizedStrings
import terminodiff.java.ui.NeighborhoodJFrame
import terminodiff.terminodiff.engine.conceptmap.ConceptMapElement
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.engine.conceptmap.ConceptMapTarget
import terminodiff.terminodiff.ui.panes.diff.NeighborhoodDisplay
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable


@Composable
fun ConceptMappingEditorContent(
    localizedStrings: LocalizedStrings,
    conceptMapState: ConceptMapState,
    useDarkTheme: Boolean,
    codeSystemDiff: CodeSystemDiffBuilder,
) {
    val lazyListState = rememberLazyListState()
    var showGraphFor: ConceptMapElement? by remember { mutableStateOf(null) }
    val columnSpecs by derivedStateOf {
        getColumnSpecs(localizedStrings, onShowGraph = {
            showGraphFor = it
        })
    }

    if (showGraphFor != null) {
        showElementNeighborhood(showGraphFor!!, useDarkTheme, localizedStrings, codeSystemDiff)
    }

    val columnHeight: Dp by derivedStateOf {
        conceptMapState.conceptMap.group.elements.map { it.targets.size + 1 }.plus(1).maxOf { it }.times(75).dp
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
    onShowGraph: (ConceptMapElement) -> Unit,
): List<ColumnSpec<ConceptMapElement>> = listOf(codeColumnSpec(localizedStrings),
    displayColumnSpec(localizedStrings),
    graphColumnSpec(localizedStrings, onShowGraph),
    targetColumnSpec(localizedStrings),
    equivalenceColumnSpec(localizedStrings))

private fun codeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec.StringSearchableColumnSpec<ConceptMapElement>(title = localizedStrings.code,
        weight = 0.1f,
        instanceGetter = { this.code.value })

private fun displayColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec.StringSearchableColumnSpec<ConceptMapElement>(title = localizedStrings.display,
        weight = 0.2f,
        instanceGetter = { this.display.value })

@OptIn(ExperimentalMaterial3Api::class)
private fun graphColumnSpec(localizedStrings: LocalizedStrings, onShowGraph: (ConceptMapElement) -> Unit) =
    ColumnSpec<ConceptMapElement>(title = localizedStrings.graph, weight = 0.08f) { tableData ->
        CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
            IconButton(onClick = { onShowGraph.invoke(tableData) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Hub, localizedStrings.graph)
            }
        }
    }

private fun targetColumnSpec(localizedStrings: LocalizedStrings) = ColumnSpec<ConceptMapElement>(
    title = localizedStrings.target,
    weight = 0.2f,
) {
    // TODO: 28/02/22
}

private fun equivalenceColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<ConceptMapElement>(title = "equivalence", // TODO: 28/02/22
        weight = 0.2f) { element ->
        val dropdownValues by remember {
            mutableStateOf(ConceptMapEquivalence.values().filter { it != ConceptMapEquivalence.NULL }
                .associateBy { it.display })
        }
        Column(Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
            element.targets.forEachIndexed { index, target ->
                Dropdown(dropdownValues.keys.toList(), selectedText = target.equivalence.value.display) { newValue ->
                    target.equivalence.value = dropdownValues[newValue]!!
                }
                if (index < element.targets.size - 1) {
                    Divider(Modifier.fillMaxWidth(0.9f).height(1.dp), color = colorScheme.secondary)
                }
            }
            IconButton({ element.targets.add(ConceptMapTarget()) }) {
                Icon(Icons.Default.AddCircle, null)
            }
        }


    }

@Composable
private fun Dropdown(
    elements: List<String>,
    selectedText: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Box(Modifier.fillMaxWidth(0.8f)) {
            TextField(value = selectedText,
                onValueChange = { },
                readOnly = true,
                colors = TextFieldDefaults.textFieldColors(textColor = colorScheme.onSecondaryContainer,
                    backgroundColor = colorScheme.secondaryContainer),
                trailingIcon = {
                    IconButton({
                        expanded = !expanded
                    }) {
                        val icon = when (expanded) {
                            true -> Icons.Filled.ArrowDropUp
                            else -> Icons.Filled.ArrowDropDown
                        }
                        Icon(icon, null)
                    }
                })
            DropdownMenu(expanded = expanded,
                modifier = Modifier.background(colorScheme.secondaryContainer),
                onDismissRequest = { expanded = false }) {
                elements.forEach { label ->
                    DropdownMenuItem(onClick = {
                        onSelect(label)
                        expanded = false
                    }) {
                        Text(text = label, color = colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun showElementNeighborhood(
    focusElement: ConceptMapElement,
    useDarkTheme: Boolean,
    localizedStrings: LocalizedStrings,
    codeSystemDiff: CodeSystemDiffBuilder,
) {
    val neighborhoodDisplay by remember {
        mutableStateOf(NeighborhoodDisplay(focusCode = focusElement.code.value!!, codeSystemDiff = codeSystemDiff))
    }
    NeighborhoodJFrame(
        /* graph = */ neighborhoodDisplay.getNeighborhoodGraph(),
        /* focusCode = */ neighborhoodDisplay.focusCode,
        /* isDarkTheme = */ useDarkTheme,
        /* localizedStrings = */ localizedStrings,
        /* frameTitle = */ localizedStrings.graph).apply {
        addClickListener { delta ->
            val newValue = neighborhoodDisplay.changeLayers(delta)
            this.setGraph(neighborhoodDisplay.getNeighborhoodGraph())
            newValue
        }
    }
}