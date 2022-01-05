package terminodiff.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.*
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("conceptdiffpanel")

@Composable
fun ConceptDiffPanel(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
    verticalWeight: Float,
) {

    val diffColors by remember { mutableStateOf(getDiffColors(useDarkTheme = useDarkTheme)) }
    var activeFilter by remember { mutableStateOf(ToggleableChipSpec.showDifferent) }
    val tableData by remember { derivedStateOf { filterDiffItems(diffDataContainer, activeFilter) } }
    val currentCount by remember { derivedStateOf { tableData.shownCodes.size } }
    val lazyListState = rememberLazyListState(0)
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth().fillMaxHeight(verticalWeight),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                localizedStrings.conceptDiff,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            FilterGroup(localizedStrings = localizedStrings, activeFilter = activeFilter, currentCount = currentCount) {
                logger.info("changed filter to $it")
                activeFilter = it
                coroutineScope.launch {
                    // scroll has to be invoked from a coroutine
                    lazyListState.scrollToItem(0)
                }
            }
            DiffDataTable(
                diffDataContainer = diffDataContainer,
                tableData = tableData,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                lazyListState = lazyListState
            )
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
        diff.conceptComparison.any { c -> c.result == ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT } || diff.propertyComparison.any()
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

    return TableData(
        onlyInLeftConcepts, onlyInRightConcepts, shownCodes, conceptDiff, leftGraphBuilder, rightGraphBuilder
    )

}

data class TableData(
    val onlyInLeftConcepts: MutableList<String>,
    val onlyInRightConcepts: MutableList<String>,
    val shownCodes: List<String>,
    val conceptDiff: TreeMap<String, ConceptDiff>,
    val leftGraphBuilder: CodeSystemGraphBuilder,
    val rightGraphBuilder: CodeSystemGraphBuilder
)

/**
 * add a static field for the unique identification of this chip
 */
val ToggleableChipSpec.Companion.showAll get() = "show-all"
val ToggleableChipSpec.Companion.showDifferent get() = "show-different"
val ToggleableChipSpec.Companion.showIdentical get() = "show-identical"
val ToggleableChipSpec.Companion.onlyInLeft get() = "show-only-in-left"
val ToggleableChipSpec.Companion.onlyInRight get() = "show-only-in-right"
val ToggleableChipSpec.Companion.onlyConceptDifferences get() = "show-only-concept-differences"

@Composable
fun FilterGroup(
    localizedStrings: LocalizedStrings, activeFilter: String, currentCount: Int, onFilterChange: (String) -> Unit
) {
    val specs = listOf(
        ToggleableChipSpec(ToggleableChipSpec.showAll, localizedStrings.showAll),
        ToggleableChipSpec(ToggleableChipSpec.showIdentical, localizedStrings.showIdentical),
        ToggleableChipSpec(ToggleableChipSpec.showDifferent, localizedStrings.showDifferent),
        ToggleableChipSpec(ToggleableChipSpec.onlyConceptDifferences, localizedStrings.onlyConceptDifferences),
        ToggleableChipSpec(ToggleableChipSpec.onlyInLeft, localizedStrings.onlyInLeft),
        ToggleableChipSpec(ToggleableChipSpec.onlyInRight, localizedStrings.onlyInRight)
    )
    ToggleableChipGroup(
        specs, selectedItem = activeFilter, onSelectionChanged = onFilterChange, currentCount = currentCount
    )
}

@Composable
fun DiffDataTable(
    diffDataContainer: DiffDataContainer,
    tableData: TableData,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    lazyListState: LazyListState
) {
    if (!diffDataContainer.isInitialized || diffDataContainer.codeSystemDiff == null) throw IllegalStateException("the diff data container is not initialized")

    val columnSpecs = listOf(
        ColumnSpec.codeColumnSpec(localizedStrings),
        ColumnSpec.displayColumnSpec(localizedStrings, diffColors),
        ColumnSpec.definitionColumnSpec(localizedStrings, diffColors),
        ColumnSpec.propertyColumnSpec(localizedStrings, diffColors),
        ColumnSpec.overallComparisonColumnSpec(localizedStrings, diffColors)
    )

    TableScreen(
        tableData = tableData,
        lazyListState = lazyListState,
        columnSpecs = columnSpecs
    )
}

fun tooltipForCode(
    leftConcept: FhirConceptDetails?, rightConcept: FhirConceptDetails?, property: (FhirConceptDetails) -> String?
): () -> String? = {
    val leftValue = leftConcept?.let(property)
    val rightValue = rightConcept?.let(property)
    when {
        leftValue == null && rightValue == null -> null
        leftValue == rightValue -> "'$leftValue'"
        else -> "'$leftValue' vs. '$rightValue'"
    }
}

@Composable
fun contentWithText(
    diff: ConceptDiff, localizedStrings: LocalizedStrings, diffColors: DiffColors, text: String?, labelToFind: String
) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        ChipForConceptDiffResult(
            modifier = Modifier.padding(end = 2.dp),
            conceptComparison = diff.conceptComparison,
            labelToFind = labelToFind,
            localizedStrings = localizedStrings,
            diffColors = diffColors
        )
        SelectableText(
            text = text ?: "null", fontStyle = if (text == null) FontStyle.Italic else FontStyle.Normal
        )
    }
}

@Composable
private fun ChipForConceptDiffResult(
    modifier: Modifier = Modifier,
    conceptComparison: List<ConceptDiffResult>,
    labelToFind: String,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors
) {
    val result = conceptComparison.find { it.diffItem.label.invoke(localizedStrings) == labelToFind } ?: return
    val colorsForResult = colorPairForConceptDiffResult(result, diffColors)
    DiffChip(
        modifier = modifier,
        text = localizedStrings.conceptDiffResults_.invoke(result.result),
        backgroundColor = colorsForResult.first,
        textColor = colorsForResult.second,
        icon = null
    )
}

private fun ColumnSpec.Companion.codeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<ConceptTableData>(title = localizedStrings.code, weight = 0.1f, content = {
        SelectableText(it.code, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    })

private fun ColumnSpec.Companion.displayColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors
) = columnSpecForProperty(
    localizedStrings = localizedStrings, diffColors = diffColors, labelToFind = localizedStrings.display,
    weight = 0.25f, stringValueResolver = FhirConceptDetails::display
)

private fun ColumnSpec.Companion.columnSpecForProperty(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    labelToFind: String,
    weight: Float,
    stringValueResolver: (FhirConceptDetails) -> String?,
): ColumnSpec<ConceptTableData> {
    val tooltipTextFun: (ConceptTableData) -> () -> String? =
        { data -> tooltipForCode(data.leftDetails, data.rightDetails, stringValueResolver) }
    return ColumnSpec(
        title = localizedStrings.display,
        weight = weight,
        tooltipText = tooltipTextFun,
    ) { data ->
        val singleConcept = when {
            data.isOnlyInLeft() -> data.leftDetails!!
            data.isOnlyInRight() -> data.rightDetails!!
            else -> null
        }
        when {
            data.isInBoth() -> contentWithText(
                diff = data.diff!!,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                labelToFind = labelToFind,
                text = tooltipTextFun(data).invoke()
            )
            singleConcept != null -> { // else
                val text = stringValueResolver.invoke(singleConcept)
                SelectableText(
                    text = text ?: "null", fontStyle = if (text == null) FontStyle.Italic else FontStyle.Normal
                )
            }
        }
    }
}

private fun ColumnSpec.Companion.definitionColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
) = ColumnSpec.columnSpecForProperty(
    localizedStrings = localizedStrings, diffColors = diffColors, labelToFind = localizedStrings.definition,
    weight = 0.25f, stringValueResolver = FhirConceptDetails::definition
)

private fun ColumnSpec.Companion.propertyColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors
) = ColumnSpec<ConceptTableData>(
    title = localizedStrings.property,
    weight = 0.25f,
    tooltipText = null
) { data ->
    when {
        data.isInBoth() -> {
            when {
                data.diff!!.propertyComparison.none() -> DiffChip(
                    text = localizedStrings.identical, colorPair = diffColors.greenPair
                )
                else -> {
                    // TODO: 04/01/22
                    Row {
                        Button(
                            onClick = {},
                            elevation = ButtonDefaults.elevation(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                diffColors.yellowPair.first, diffColors.yellowPair.second
                            )
                        ) {
                            Text(localizedStrings.numberDifferent_.invoke(data.diff.propertyComparison.size))
                        }
                    }
                }
            }
        }
        else -> {
            // TODO: 05/01/22
        }
    }
}

private fun ColumnSpec.Companion.overallComparisonColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors
) = ColumnSpec<ConceptTableData>(
    title = localizedStrings.overallComparison,
    weight = 0.1f,
    tooltipText = null,
) { data ->
    when (data.isInBoth()) {
        true -> {
            val anyDifferent = data.diff!!.conceptComparison.any {
                it.result == ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT
            }
            val colors: Pair<Color, Color> = if (anyDifferent) diffColors.yellowPair else diffColors.greenPair
            val chipLabel: String =
                if (anyDifferent) localizedStrings.conceptDiffResults_.invoke(ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT)
                else localizedStrings.identical

            DiffChip(
                colorPair = colors, text = chipLabel, modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
        else -> {
            val chipLabel: String =
                if (data.isOnlyInLeft()) localizedStrings.onlyInLeft else localizedStrings.onlyInRight
            val onlyOneVersionIcon: ImageVector = when (data.isOnlyInLeft()) {
                true -> AppIconResource.loadXmlImageVector(AppIconResource.icLoadLeftFile)
                else -> AppIconResource.loadXmlImageVector(AppIconResource.icLoadRightFile)
            }
            DiffChip(
                modifier = Modifier.fillMaxWidth(0.8f),
                colorPair = diffColors.redPair,
                text = chipLabel,
                icon = onlyOneVersionIcon
            )
        }
    }
}

data class ConceptTableData(
    val code: String,
    val leftDetails: FhirConceptDetails?,
    val rightDetails: FhirConceptDetails?,
    val diff: ConceptDiff?
) {
    fun isOnlyInLeft() = leftDetails != null && rightDetails == null
    fun isOnlyInRight() = leftDetails == null && rightDetails != null
    fun isInBoth() = diff != null
}

@Composable
fun TableScreen(
    tableData: TableData,
    lazyListState: LazyListState,
    columnSpecs: List<ColumnSpec<ConceptTableData>>
) {
    val containedData: List<ConceptTableData> = tableData.shownCodes.map { code ->
        ConceptTableData(
            code = code,
            leftDetails = tableData.leftGraphBuilder.nodeTree[code],
            rightDetails = tableData.rightGraphBuilder.nodeTree[code],
            diff = tableData.conceptDiff[code]
        )
    }
    LazyTable(
        columnSpecs = columnSpecs,
        lazyListState = lazyListState,
        tableData = containedData,
        keyFun = { it.code },
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
    )
}