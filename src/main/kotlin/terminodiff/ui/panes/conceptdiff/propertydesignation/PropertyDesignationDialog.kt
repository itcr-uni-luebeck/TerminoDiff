package terminodiff.terminodiff.ui.panes.conceptdiff.propertydesignation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import terminodiff.terminodiff.ui.util.TerminodiffDialog
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

    TerminodiffDialog(
        title = localizedStrings.propertyDesignationForCode_.invoke(conceptData.code),
        onCloseRequest = onClose
    ) {
        VerticalSplitPane(splitPaneState = splitPaneState) {
            first {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)) {
                    Text(localizedStrings.properties,
                        style = typography.titleMedium,
                        color = colorScheme.onPrimaryContainer)
                    when {
                        conceptData.isInBoth() -> DiffPropertyTable(
                            conceptDiff = conceptData.diff!!,
                            diffColumnSpecs = propertyDiffColumnSpecs,
                            lazyListState = propertyListState,
                            localizedStrings = localizedStrings
                        )
                        else -> SingleConceptPropertyTable(
                            leftDetails = conceptData.leftDetails,
                            rightDetails = conceptData.rightDetails,
                            identicalColumnSpecs = identicalPropertyColumnSpecs,
                            lazyListState = propertyListState,
                            localizedStrings = localizedStrings,
                        )
                    }
                }

            }
            second {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)) {
                    Text(localizedStrings.designations,
                        style = typography.titleMedium,
                        color = colorScheme.onPrimaryContainer)
                    when {
                        conceptData.isInBoth() -> DiffDesignationTable(
                            diff = conceptData.diff!!,
                            columnSpecs = designationDiffColumnSpecs,
                            designationListState = designationListState,
                            localizedStrings = localizedStrings,
                        )
                        else -> DesignationTable(
                            leftDetails = conceptData.leftDetails,
                            rightDetails = conceptData.rightDetails,
                            columnSpecs = identicalDesignationColumnSpecs,
                            designationListState = designationListState,
                            localizedStrings = localizedStrings
                        )
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

@Composable
fun DesignationTable(
    leftDetails: FhirConceptDetails?,
    rightDetails: FhirConceptDetails?,
    columnSpecs: List<ColumnSpec<FhirConceptDesignation>>,
    designationListState: LazyListState,
    localizedStrings: LocalizedStrings,
) = when (leftDetails) {
    null -> rightDetails
    else -> leftDetails
}?.designation?.let { tableData ->
    LazyTable(
        modifier = Modifier.padding(8.dp),
        columnSpecs = columnSpecs,
        backgroundColor = colorScheme.primaryContainer,
        lazyListState = designationListState,
        zebraStripingColor = colorScheme.tertiaryContainer,
        tableData = tableData,
        localizedStrings = localizedStrings,
    ) {
        it.language ?: "null"
    }
}

@Composable
fun DiffDesignationTable(
    diff: ConceptDiff,
    columnSpecs: List<ColumnSpec<KeyedListDiffResult<DesignationKey, String>>>,
    designationListState: LazyListState,
    localizedStrings: LocalizedStrings,
) = LazyTable(
    modifier = Modifier.padding(8.dp),
    columnSpecs = columnSpecs,
    backgroundColor = colorScheme.primaryContainer,
    lazyListState = designationListState,
    zebraStripingColor = colorScheme.tertiaryContainer,
    tableData = diff.designationComparison,
    localizedStrings = localizedStrings,
) { it.key.toString() }

@Composable
fun SingleConceptPropertyTable(
    leftDetails: FhirConceptDetails?,
    rightDetails: FhirConceptDetails?,
    identicalColumnSpecs: List<ColumnSpec<FhirConceptProperty>>,
    lazyListState: LazyListState,
    localizedStrings: LocalizedStrings,
) = when (leftDetails) {
    null -> rightDetails
    else -> leftDetails
}?.property?.let { tableData ->
    LazyTable(
        modifier = Modifier.padding(8.dp),
        columnSpecs = identicalColumnSpecs,
        backgroundColor = colorScheme.primaryContainer,
        lazyListState = lazyListState,
        zebraStripingColor = colorScheme.tertiaryContainer,
        tableData = tableData,
        localizedStrings = localizedStrings,
    ) { it.propertyCode }
}

@Composable
fun DiffPropertyTable(
    conceptDiff: ConceptDiff,
    diffColumnSpecs: List<ColumnSpec<PropertyDiffResult>>,
    lazyListState: LazyListState,
    localizedStrings: LocalizedStrings,
) = LazyTable(
    modifier = Modifier.padding(8.dp),
    columnSpecs = diffColumnSpecs,
    backgroundColor = colorScheme.primaryContainer,
    lazyListState = lazyListState,
    zebraStripingColor = colorScheme.tertiaryContainer,
    tableData = conceptDiff.propertyComparison,
    localizedStrings = localizedStrings,
) { it.key }