package terminodiff.terminodiff.ui.panes.conceptdiff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.graph.FhirConceptProperty
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.ui.panes.conceptdiff.property.PropDiffData
import terminodiff.terminodiff.ui.panes.conceptdiff.property.columnSpecsDifferentProperties
import terminodiff.terminodiff.ui.panes.conceptdiff.property.columnSpecsIdenticalProperties
import terminodiff.ui.panes.conceptdiff.ConceptTableData
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable

@Composable
fun PropertyDialog(
    conceptData: ConceptTableData,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
    onClose: () -> Unit,
) {
    val listState = rememberLazyListState()
    val diffColors by derivedStateOf { getDiffColors(useDarkTheme = useDarkTheme) }
    val diffColumnSpecs by derivedStateOf { columnSpecsDifferentProperties(localizedStrings, diffColors = diffColors) }
    val identicalColumnSpecs by derivedStateOf { columnSpecsIdenticalProperties(localizedStrings) }
    Dialog(onCloseRequest = onClose,
        title = localizedStrings.propertyDesignationForCode_.invoke(conceptData.code),
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(1024.dp, 512.dp))) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            Column(Modifier.background(MaterialTheme.colorScheme.primaryContainer).fillMaxSize()) {
                when {
                    conceptData.isInBoth() -> DiffPropertyTable(conceptData.diff!!, diffColumnSpecs, listState)
                    conceptData.isOnlyInLeft() -> SingleConceptPropertyTable(conceptData.leftDetails!!,
                        localizedStrings,
                        identicalColumnSpecs,
                        listState)
                    conceptData.isOnlyInRight() -> SingleConceptPropertyTable(conceptData.rightDetails!!,
                        localizedStrings,
                        identicalColumnSpecs,
                        listState)
                }
            }
        }
    }
}

@Composable
fun SingleConceptPropertyTable(
    details: FhirConceptDetails,
    localizedStrings: LocalizedStrings,
    identicalColumnSpecs: List<ColumnSpec<FhirConceptProperty>>,
    lazyListState: LazyListState,
) =
    details.property?.let { tableData ->
        LazyTable(columnSpecs = identicalColumnSpecs,
            lazyListState = lazyListState,
            tableData = tableData,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) { it.propertyCode }
    }


@Composable
fun DiffPropertyTable(
    conceptDiff: ConceptDiff,
    diffColumnSpecs: List<ColumnSpec<KeyedListDiffResult<String, String>>>,
    lazyListState: LazyListState,
) = LazyTable(
    columnSpecs = diffColumnSpecs,
    lazyListState = lazyListState,
    tableData = conceptDiff.propertyComparison,
    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
    foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
) { it.key }