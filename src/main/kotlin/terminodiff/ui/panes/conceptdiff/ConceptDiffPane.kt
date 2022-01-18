package terminodiff.ui.panes.conceptdiff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.ui.panes.conceptdiff.PropertyDialog
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.*
import java.awt.Window
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("conceptdiffpanel")

@Composable
fun ConceptDiffPanel(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
) {
    val diffColors by remember { mutableStateOf(getDiffColors(useDarkTheme = useDarkTheme)) }
    var activeFilter by remember { mutableStateOf(ToggleableChipSpec.showDifferent) }
    val tableData by derivedStateOf { filterDiffItems(diffDataContainer, activeFilter) }
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

    var propertyDialogData: ConceptTableData? by remember { mutableStateOf(null) }

    propertyDialogData?.let { conceptTableData ->
        PropertyDialog(conceptTableData, localizedStrings, useDarkTheme) {
            propertyDialogData = null
        }
    }

    Card(
        modifier = Modifier.padding(8.dp).fillMaxSize(),//.fillMaxHeight(verticalWeight),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(localizedStrings.conceptDiff,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
            FilterGroup(filterSpecs = filterSpecs, filterCounts = counts, activeFilter = activeFilter) {
                logger.info("changed filter to $it")
                activeFilter = it
                coroutineScope.launch {
                    // scroll has to be invoked from a coroutine
                    lazyListState.scrollToItem(0)
                }
            }
            DiffDataTable(diffDataContainer = diffDataContainer,
                tableData = tableData,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                lazyListState = lazyListState,
                showPropertyDialog = {
                    propertyDialogData = it
                    logger.info("showing details dialog for concept ${it.code}")
                })
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
            diff.propertyComparison.any { p -> p.kind != KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL } -> true
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
    showPropertyDialog: (ConceptTableData) -> Unit,
) {
    if (diffDataContainer.codeSystemDiff == null) throw IllegalStateException("the diff data container is not initialized")

    val columnSpecs = conceptDiffColumnSpecs(localizedStrings, diffColors, showPropertyDialog)

    TableScreen(tableData = tableData, lazyListState = lazyListState, columnSpecs = columnSpecs)
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
    tableData: TableData, lazyListState: LazyListState, columnSpecs: List<ColumnSpec<ConceptTableData>>,
) {
    val containedData: List<ConceptTableData> = tableData.shownCodes.map { code ->
        ConceptTableData(code = code,
            leftDetails = tableData.leftGraphBuilder.nodeTree[code],
            rightDetails = tableData.rightGraphBuilder.nodeTree[code],
            diff = tableData.conceptDiff[code])
    }
    LazyTable(columnSpecs = columnSpecs,
        lazyListState = lazyListState,
        tableData = containedData,
        keyFun = { it.code },
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer)
}