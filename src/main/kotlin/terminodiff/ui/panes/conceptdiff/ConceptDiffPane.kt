package terminodiff.ui.panes.conceptdiff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mediation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.ui.panes.conceptdiff.display.DisplayDetailsDialog
import terminodiff.terminodiff.ui.panes.conceptdiff.propertydesignation.PropertyDesignationDialog
import terminodiff.terminodiff.ui.panes.conceptmap.ConceptMapDialog
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable
import terminodiff.ui.util.ToggleableChipGroup
import terminodiff.ui.util.ToggleableChipSpec
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("conceptdiffpanel")

private enum class DetailsDialogKind {
    PROPERTY_DESIGNATION, DISPLAY, DEFINITION
}

@Composable
fun ConceptDiffPanel(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
    fhirContext: FhirContext,
    onShowGraph: (String) -> Unit,
) {
    val diffColors by remember { mutableStateOf(getDiffColors(useDarkTheme = useDarkTheme)) }
    var activeFilter by remember { mutableStateOf(ToggleableChipSpec.showDifferent) }
    val chipFilteredTableData by derivedStateOf { filterDiffItems(diffDataContainer, activeFilter) }
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    val coroutineScope = rememberCoroutineScope()
    val filterSpecs by derivedStateOf {
        listOf(ToggleableChipSpec(ToggleableChipSpec.showAll, localizedStrings.showAll),
            ToggleableChipSpec(ToggleableChipSpec.showIdentical, localizedStrings.showIdentical),
            ToggleableChipSpec(ToggleableChipSpec.showDifferent, localizedStrings.showDifferent),
            ToggleableChipSpec(ToggleableChipSpec.onlyConceptDifferences, localizedStrings.onlyConceptDifferences),
            ToggleableChipSpec(ToggleableChipSpec.onlyInLeft, localizedStrings.onlyInLeft),
            ToggleableChipSpec(ToggleableChipSpec.onlyInRight, localizedStrings.onlyInRight))
    }
    val counts by derivedStateOf {
        filterSpecs.associate { it.name to filterDiffItems(diffDataContainer, it.name).shownCodes.size }
    }

    var dialogData: Pair<ConceptTableData, DetailsDialogKind>? by remember { mutableStateOf(null) }
    val conceptMapState by remember { mutableStateOf(ConceptMapState(diffDataContainer)) }
    var showConceptMapDialog: Boolean by remember { mutableStateOf(false) }

    dialogData?.let { (data, kind) ->
        val onClose: () -> Unit = { dialogData = null }
        when (kind) {
            DetailsDialogKind.PROPERTY_DESIGNATION -> PropertyDesignationDialog(data,
                localizedStrings,
                useDarkTheme,
                onClose)
            DetailsDialogKind.DISPLAY -> DisplayDetailsDialog(data = data,
                localizedStrings = localizedStrings,
                label = localizedStrings.display,
                useDarkTheme = useDarkTheme,
                onClose = onClose) { it.display }
            DetailsDialogKind.DEFINITION -> DisplayDetailsDialog(data = data,
                localizedStrings = localizedStrings,
                label = localizedStrings.definition,
                useDarkTheme = useDarkTheme,
                onClose = onClose) { it.definition }
        }
    }

    if (showConceptMapDialog) {
        ConceptMapDialog(diffDataContainer = diffDataContainer,
            conceptMapState = conceptMapState,
            localizedStrings = localizedStrings,
            fhirContext = fhirContext,
            isDarkTheme = useDarkTheme) {
            showConceptMapDialog = false
        }
    }

    Card(
        modifier = Modifier.padding(8.dp).fillMaxSize(),
        elevation = 8.dp,
        backgroundColor = colorScheme.tertiaryContainer,
        contentColor = colorScheme.onTertiaryContainer,
    ) {
        Column(Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(localizedStrings.conceptDiff,
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onTertiaryContainer)
            Row {
                FilterGroup(filterSpecs = filterSpecs, filterCounts = counts, activeFilter = activeFilter) {
                    logger.info("changed filter to $it")
                    activeFilter = it
                    coroutineScope.launch {
                        // scroll has to be invoked from a coroutine
                        lazyListState.scrollToItem(0)
                    }
                }
                Button(onClick = {
                    showConceptMapDialog = true
                },
                    elevation = ButtonDefaults.elevation(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onPrimaryContainer)) {
                    Icon(imageVector = Icons.Default.Mediation,
                        contentDescription = localizedStrings.conceptMap,
                        tint = colorScheme.onPrimaryContainer)
                    Text(localizedStrings.conceptMap)
                }
            }

            DiffDataTable(diffDataContainer = diffDataContainer,
                tableData = chipFilteredTableData,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                lazyListState = lazyListState,
                onShowPropertyDialog = {
                    dialogData = it to DetailsDialogKind.PROPERTY_DESIGNATION
                },
                onShowDisplayDetailsDialog = {
                    dialogData = it to DetailsDialogKind.DISPLAY
                },
                onShowDefinitionDetailsDialog = {
                    dialogData = it to DetailsDialogKind.DEFINITION
                },
                onShowGraph = onShowGraph)
        }
    }
}

fun filterDiffItems(diffDataContainer: DiffDataContainer, activeFilter: String): TableData {
    val leftGraphBuilder = diffDataContainer.leftGraphBuilder ?: throw NullPointerException()
    val rightGraphBuilder = diffDataContainer.rightGraphBuilder ?: throw NullPointerException()
    val onlyInLeftConcepts = diffDataContainer.codeSystemDiff?.onlyInLeftConcepts ?: throw NullPointerException()
    val onlyInRightConcepts = diffDataContainer.codeSystemDiff?.onlyInRightConcepts ?: throw NullPointerException()
    val conceptDiff = diffDataContainer.codeSystemDiff?.conceptDifferences ?: throw NullPointerException()
    val differentCodesInDiff = conceptDiff.filterValues { diff ->
        when {
            diff.conceptComparison.any { c -> c.result == ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT } -> true
            diff.propertyComparison.any { p -> p.result != KeyedListDiffResultKind.IDENTICAL } -> true
            diff.designationComparison.any { d -> d.result != KeyedListDiffResultKind.IDENTICAL } -> true
            else -> false
        }
    }.keys
    val sameCodesInDiff = conceptDiff.keys.minus(differentCodesInDiff)

    val shownCodes = when (activeFilter) {
        ToggleableChipSpec.showDifferent -> onlyInLeftConcepts.plus(onlyInRightConcepts).plus(differentCodesInDiff)
        ToggleableChipSpec.onlyInLeft -> onlyInLeftConcepts
        ToggleableChipSpec.onlyInRight -> onlyInRightConcepts
        ToggleableChipSpec.showIdentical -> sameCodesInDiff
        ToggleableChipSpec.onlyConceptDifferences -> differentCodesInDiff
        else -> onlyInLeftConcepts.plus(onlyInRightConcepts).plus(conceptDiff.keys) // show all
    }.toSortedSet().toList()

    return TableData(onlyInLeftConcepts,
        onlyInRightConcepts,
        shownCodes,
        conceptDiff,
        leftGraphBuilder,
        rightGraphBuilder)

}

data class TableData(
    val onlyInLeftConcepts: MutableList<String>,
    val onlyInRightConcepts: MutableList<String>,
    val shownCodes: List<String>,
    val conceptDiff: TreeMap<String, ConceptDiff>,
    val leftGraphBuilder: CodeSystemGraphBuilder,
    val rightGraphBuilder: CodeSystemGraphBuilder,
)

@Composable
fun FilterGroup(
    filterSpecs: List<ToggleableChipSpec>,
    filterCounts: Map<String, Int>,
    activeFilter: String,
    onFilterChange: (String) -> Unit,
) = ToggleableChipGroup(specs = filterSpecs,
    selectedItem = activeFilter,
    onSelectionChanged = onFilterChange,
    filterCounts = filterCounts)

@Composable
fun DiffDataTable(
    diffDataContainer: DiffDataContainer,
    tableData: TableData,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    lazyListState: LazyListState,
    onShowPropertyDialog: (ConceptTableData) -> Unit,
    onShowDisplayDetailsDialog: (ConceptTableData) -> Unit,
    onShowDefinitionDetailsDialog: (ConceptTableData) -> Unit,
    onShowGraph: (String) -> Unit,
) {
    if (diffDataContainer.codeSystemDiff == null) throw IllegalStateException("the diff data container is not initialized")

    val columnSpecs by derivedStateOf {
        conceptDiffColumnSpecs(localizedStrings = localizedStrings,
            diffColors = diffColors,
            onShowPropertyDialog = onShowPropertyDialog,
            onShowDisplayDetailsDialog = onShowDisplayDetailsDialog,
            onShowDefinitionDetailsDialog = onShowDefinitionDetailsDialog,
            onShowGraph = onShowGraph)
    }

    TableScreen(
        tableData = tableData,
        lazyListState = lazyListState,
        columnSpecs = columnSpecs,
        localizedStrings = localizedStrings,
    )
}

data class ConceptTableData(
    val code: String,
    val leftDetails: FhirConceptDetails?,
    val rightDetails: FhirConceptDetails?,
    val diff: ConceptDiff?,
) {
    fun isOnlyInLeft() = leftDetails != null && rightDetails == null
    fun isOnlyInRight() = leftDetails == null && rightDetails != null
    fun isInBoth() = diff != null
}

@Composable
fun TableScreen(
    tableData: TableData,
    lazyListState: LazyListState,
    columnSpecs: List<ColumnSpec<ConceptTableData>>,
    localizedStrings: LocalizedStrings,
) {
    val containedData: List<ConceptTableData> by derivedStateOf {
        tableData.shownCodes.map { code ->
            ConceptTableData(code = code,
                leftDetails = tableData.leftGraphBuilder.nodeTree[code],
                rightDetails = tableData.rightGraphBuilder.nodeTree[code],
                diff = tableData.conceptDiff[code])
        }
    }
    LazyTable(columnSpecs = columnSpecs,
        backgroundColor = colorScheme.tertiaryContainer,
        lazyListState = lazyListState,
        zebraStripingColor = colorScheme.primaryContainer,
        tableData = containedData,
        localizedStrings = localizedStrings,
        countLabel = localizedStrings.concepts_) { it.code }
}