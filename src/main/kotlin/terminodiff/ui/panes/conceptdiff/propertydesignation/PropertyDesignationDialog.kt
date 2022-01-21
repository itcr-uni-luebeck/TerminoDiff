package terminodiff.terminodiff.ui.panes.conceptdiff.propertydesignation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.DesignationKey
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.concepts.PropertyDiffResult
import terminodiff.engine.graph.FhirConceptDesignation
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.graph.FhirConceptProperty
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.cursorForHorizontalResize
import terminodiff.ui.panes.conceptdiff.ConceptTableData
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun PropertyDesignationDialog(
    conceptData: ConceptTableData,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
    onClose: () -> Unit,
) {
    val propertyListState = rememberLazyListState()
    val splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.5f)
    val designationListState = rememberLazyListState()
    val diffColors by derivedStateOf { getDiffColors(useDarkTheme = useDarkTheme) }
    val propertyDiffColumnSpecs: List<ColumnSpec<PropertyDiffResult>> by derivedStateOf {
        columnSpecsDifferentProperties(localizedStrings, diffColors = diffColors)
    }
    val identicalPropertyColumnSpecs: List<ColumnSpec<FhirConceptProperty>> by derivedStateOf {
        columnSpecsIdenticalProperties(localizedStrings)
    }
    val designationDiffColumnSpecs by derivedStateOf {
        columnSpecsDifferentDesignations(localizedStrings, diffColors)
    }
    val identicalDesignationColumnSpecs by derivedStateOf {
        columnSpecsIdenticalDesignations(localizedStrings)
    }

    Dialog(onCloseRequest = onClose,
        title = localizedStrings.propertyDesignationForCode_.invoke(conceptData.code),
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(1024.dp, 512.dp))) {
        CompositionLocalProvider(LocalContentColor provides colorScheme.onBackground) {
            Column(Modifier.background(colorScheme.primaryContainer).fillMaxSize()) {
                VerticalSplitPane(splitPaneState = splitPaneState) {
                    first {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                            Text(localizedStrings.properties,
                                style = typography.titleMedium,
                                color = colorScheme.onPrimaryContainer)
                            when {
                                conceptData.isInBoth() -> DiffPropertyTable(conceptData.diff!!,
                                    propertyDiffColumnSpecs,
                                    propertyListState)
                                else -> SingleConceptPropertyTable(conceptData.leftDetails,
                                    conceptData.rightDetails,
                                    identicalPropertyColumnSpecs,
                                    propertyListState)
                            }
                        }

                    }
                    second {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                            Text(localizedStrings.designations,
                                style = typography.titleMedium,
                                color = colorScheme.onPrimaryContainer)
                            when {
                                conceptData.isInBoth() -> DiffDesignationTable(conceptData.diff!!,
                                    designationDiffColumnSpecs,
                                    designationListState)
                                else -> DesignationTable(conceptData.leftDetails,
                                    conceptData.rightDetails,
                                    identicalDesignationColumnSpecs,
                                    designationListState)
                            }
                        }
                    }
                    splitter {
                        visiblePart {
                            Box(Modifier.height(3.dp).fillMaxWidth()
                                .background(colorScheme.primary))
                        }
                        handle {
                            Box(
                                Modifier
                                    .markAsHandle()
                                    .cursorForHorizontalResize()
                                    .background(color = colorScheme.primary.copy(alpha = 0.5f))
                                    .height(9.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesignationTable(
    leftDetails: FhirConceptDetails?,
    rightDetails: FhirConceptDetails?,
    columnSpecs: List<ColumnSpec<FhirConceptDesignation>>,
    designationListState: LazyListState,
) = when (leftDetails) {
    null -> rightDetails
    else -> leftDetails
}?.designation?.let { tableData ->
    LazyTable(
        modifier = Modifier.padding(8.dp),
        columnSpecs = columnSpecs,
        tableData = tableData,
        backgroundColor = colorScheme.primaryContainer,
        zebraStripingColor = colorScheme.tertiaryContainer,
        lazyListState = designationListState
    ) {
        it.language ?: "null"
    }
}

@Composable
fun DiffDesignationTable(
    diff: ConceptDiff,
    columnSpecs: List<ColumnSpec<KeyedListDiffResult<DesignationKey, String>>>,
    designationListState: LazyListState,
) = LazyTable(modifier = Modifier.padding(8.dp),
    columnSpecs = columnSpecs,
    lazyListState = designationListState,
    tableData = diff.designationComparison,
    backgroundColor = colorScheme.primaryContainer,
    zebraStripingColor = colorScheme.tertiaryContainer) { it.key.toString() }

@Composable
fun SingleConceptPropertyTable(
    leftDetails: FhirConceptDetails?,
    rightDetails: FhirConceptDetails?,
    identicalColumnSpecs: List<ColumnSpec<FhirConceptProperty>>,
    lazyListState: LazyListState,
) = when (leftDetails) {
    null -> rightDetails
    else -> leftDetails
}?.property?.let { tableData ->
    LazyTable(modifier = Modifier.padding(8.dp),
        columnSpecs = identicalColumnSpecs,
        lazyListState = lazyListState,
        tableData = tableData,
        backgroundColor = colorScheme.primaryContainer,
        zebraStripingColor = colorScheme.tertiaryContainer) { it.propertyCode }
}

@Composable
fun DiffPropertyTable(
    conceptDiff: ConceptDiff,
    diffColumnSpecs: List<ColumnSpec<PropertyDiffResult>>,
    lazyListState: LazyListState,
) = LazyTable(modifier = Modifier.padding(8.dp),
    columnSpecs = diffColumnSpecs,
    lazyListState = lazyListState,
    tableData = conceptDiff.propertyComparison,
    backgroundColor = colorScheme.primaryContainer,
    zebraStripingColor = colorScheme.tertiaryContainer) { it.key }