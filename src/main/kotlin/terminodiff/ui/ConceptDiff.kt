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

    val columnSpecs = buildColumnSpecs(localizedStrings, tableData, diffColors)

    TableScreen(
        tableData = tableData,
        lazyListState = lazyListState,
        columnSpecs = columnSpecs,
    )
}

@Composable
private fun buildColumnSpecs(
    localizedStrings: LocalizedStrings, tableData: TableData, diffColors: DiffColors
) = listOf(
    ConceptDiffColumnSpec.codeColumnSpec(localizedStrings), ConceptDiffColumnSpec.displayColumnSpec(
        localizedStrings, tableData.leftGraphBuilder, tableData.rightGraphBuilder, diffColors
    ), ConceptDiffColumnSpec.definitionColumnSpec(
        localizedStrings, tableData.leftGraphBuilder, tableData.rightGraphBuilder, diffColors
    ), ConceptDiffColumnSpec.propertyColumnSpec(
        localizedStrings, tableData.leftGraphBuilder, tableData.rightGraphBuilder, diffColors
    ), ConceptDiffColumnSpec.overallComparisonColumnSpec(
        localizedStrings, diffColors,
    )
)

@Composable
fun RowScope.ConceptDiffHeaderCell(
    weight: Float, text: String
) {
    Box(
        Modifier.border(1.dp, MaterialTheme.colorScheme.onTertiaryContainer).weight(weight).padding(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun RowScope.ConceptDiffTableCell(
    modifier: Modifier = Modifier, weight: Float, tooltipText: (() -> String?)? = null, content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.onTertiaryContainer).weight(weight).padding(2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            tooltipText == null || tooltipText() == null -> content()
            else -> MouseOverPopup(
                text = tooltipText()!!,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
                content = content
            )
        }
    }
}

data class ConceptDiffColumnSpec(
    val title: String,
    val weight: Float,
    val tooltipText: ((String) -> String?)? = null,
    val tooltipOnlyInX: ((String, FhirConceptDetails) -> String?)? = null,
    val cellContent: @Composable (String, ConceptDiff) -> Unit,
    val cellContentOnlyInX: @Composable (String, FhirConceptDetails, Boolean) -> Unit
) {

    companion object {

        private fun tooltipForCode(
            leftGraphBuilder: CodeSystemGraphBuilder,
            rightGraphBuilder: CodeSystemGraphBuilder,
            property: (FhirConceptDetails) -> String?
        ): (String) -> String? = { code ->
            val leftValue = leftGraphBuilder.nodeTree[code]?.let(property)
            val rightValue = rightGraphBuilder.nodeTree[code]?.let(property)
            when {
                leftValue == null && rightValue == null -> null
                leftValue == rightValue -> "'$leftValue'"
                else -> "'$leftValue' vs. '$rightValue'"
            }

        }

        @Composable
        fun codeColumnSpec(localizedStrings: LocalizedStrings): ConceptDiffColumnSpec {
            @Composable
            fun textRow(code: String) = Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SelectableText(code, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)

            }
            return ConceptDiffColumnSpec(title = localizedStrings.code, weight = 0.1f, cellContent = { code, _ ->
                textRow(code)
            }, cellContentOnlyInX = { code, _, _ -> textRow(code) })
        }

        @Composable
        private fun contentWithText(
            code: String,
            diff: ConceptDiff,
            localizedStrings: LocalizedStrings,
            diffColors: DiffColors,
            tooltipTextFun: (String) -> String?,
            labelToFind: String
        ) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                ChipForConceptDiffResult(
                    modifier = Modifier.padding(end = 2.dp),
                    conceptComparison = diff.conceptComparison,
                    labelToFind = labelToFind,
                    localizedStrings = localizedStrings,
                    diffColors = diffColors
                )
                val tooltipText = tooltipTextFun.invoke(code)
                SelectableText(
                    text = tooltipText ?: "null",
                    fontStyle = if (tooltipText == null) FontStyle.Italic else FontStyle.Normal
                )
            }
        }

        fun displayColumnSpec(
            localizedStrings: LocalizedStrings,
            leftGraphBuilder: CodeSystemGraphBuilder,
            rightGraphBuilder: CodeSystemGraphBuilder,
            diffColors: DiffColors
        ): ConceptDiffColumnSpec {
            val tooltipTextFun = tooltipForCode(
                leftGraphBuilder = leftGraphBuilder,
                rightGraphBuilder = rightGraphBuilder,
                property = FhirConceptDetails::display
            )
            return ConceptDiffColumnSpec(localizedStrings.display,
                weight = 0.25f,
                tooltipText = tooltipTextFun,
                tooltipOnlyInX = { _, concept -> concept.display },
                cellContent = { code, diff ->
                    contentWithText(
                        code = code,
                        diff = diff,
                        localizedStrings = localizedStrings,
                        diffColors = diffColors,
                        tooltipTextFun = tooltipTextFun,
                        labelToFind = localizedStrings.display
                    )
                },
                cellContentOnlyInX = { _, concept, _ ->
                    SelectableText(
                        text = concept.display ?: "null",
                        fontStyle = if (concept.display == null) FontStyle.Italic else FontStyle.Normal
                    )
                })
        }

        fun definitionColumnSpec(
            localizedStrings: LocalizedStrings,
            leftGraphBuilder: CodeSystemGraphBuilder,
            rightGraphBuilder: CodeSystemGraphBuilder,
            diffColors: DiffColors
        ): ConceptDiffColumnSpec {
            val tooltipTextFun = tooltipForCode(
                leftGraphBuilder = leftGraphBuilder,
                rightGraphBuilder = rightGraphBuilder,
                property = FhirConceptDetails::definition
            )
            return ConceptDiffColumnSpec(title = localizedStrings.definition,
                weight = 0.25f,
                tooltipText = tooltipTextFun,
                tooltipOnlyInX = { _, concept -> concept.definition },
                cellContent = { code, diff ->
                    contentWithText(
                        code = code,
                        diff = diff,
                        localizedStrings = localizedStrings,
                        diffColors = diffColors,
                        tooltipTextFun = tooltipTextFun,
                        labelToFind = localizedStrings.definition
                    )
                },
                cellContentOnlyInX = { _, concept, _ ->
                    Text(
                        text = concept.definition ?: "null",
                        fontStyle = if (concept.definition == null) FontStyle.Italic else FontStyle.Normal
                    )
                })
        }

        fun propertyColumnSpec(
            localizedStrings: LocalizedStrings,
            leftGraphBuilder: CodeSystemGraphBuilder,
            rightGraphBuilder: CodeSystemGraphBuilder,
            diffColors: DiffColors
        ): ConceptDiffColumnSpec =
            ConceptDiffColumnSpec(
                title = localizedStrings.property,
                weight = 0.25f,
                tooltipText = null,
                cellContent = { _, diff ->
                    when {
                        diff.propertyComparison.none() -> {
                            DiffChip(
                                text = localizedStrings.identical, colorPair = diffColors.greenPair
                            )
                        }
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
                                    Text(localizedStrings.numberDifferent_.invoke(diff.propertyComparison.size))
                                }
                                /*DiffChip(
                                    text = localizedStrings.numberDifferent_.invoke(diff.propertyComparison.size),
                                    colorPair = diffColors.yellowPair,
                                )*/
                            }
                        }
                    }
                },
                cellContentOnlyInX = { _, _, _ ->
                    // TODO: 04/01/22
                })

        fun overallComparisonColumnSpec(
            localizedStrings: LocalizedStrings,
            diffColors: DiffColors,
        ): ConceptDiffColumnSpec = ConceptDiffColumnSpec(title = localizedStrings.overallComparison,
            weight = 0.15f,
            tooltipText = null,
            cellContent = { _, diff ->
                val colors: Pair<Color, Color>
                val chipLabel: String
                when {
                    diff.conceptComparison.any { it.result == ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT } -> {
                        colors = diffColors.yellowPair
                        chipLabel =
                            localizedStrings.conceptDiffResults_.invoke(ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT)
                    }
                    else -> {
                        colors = diffColors.greenPair
                        chipLabel = localizedStrings.identical
                    }
                }
                DiffChip(
                    colorPair = colors, text = chipLabel, modifier = Modifier.fillMaxWidth(0.8f)
                )
            },
            cellContentOnlyInX = { _, _, onlyInLeft ->
                val chipLabel: String
                val onlyOneVersionIcon: ImageVector
                when (onlyInLeft) {
                    true -> {
                        chipLabel = localizedStrings.onlyInLeft
                        onlyOneVersionIcon = AppIconResource.loadXmlImageVector(AppIconResource.icLoadLeftFile)

                    }
                    else -> {
                        chipLabel = localizedStrings.onlyInRight
                        onlyOneVersionIcon = AppIconResource.loadXmlImageVector(AppIconResource.icLoadRightFile)
                    }
                }
                DiffChip(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colorPair = diffColors.redPair,
                    text = chipLabel,
                    icon = onlyOneVersionIcon
                )
            })
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableScreen(
    tableData: TableData,
    lazyListState: LazyListState,
    columnSpecs: List<ConceptDiffColumnSpec>,
) {
    val cellHeight = 50.dp

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // draw the header row
        Row(Modifier.fillMaxWidth()) {
            columnSpecs.forEach { spec ->
                ConceptDiffHeaderCell(weight = spec.weight, text = spec.title)
            }
        }
        // a thicker line beneath the header
        Divider(color = MaterialTheme.colorScheme.onTertiaryContainer, thickness = 1.dp)

        // draw the actual cells, contained by a LazyColumn
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LazyColumn(state = lazyListState) {
                items(tableData.shownCodes) { code ->
                    Row(
                        Modifier.wrapContentHeight()
                    ) {
                        when (code) {
                            in tableData.onlyInLeftConcepts, in tableData.onlyInRightConcepts -> {
                                val isLeft = code in tableData.onlyInLeftConcepts
                                val concept =
                                    if (isLeft) tableData.leftGraphBuilder.nodeTree[code] else tableData.rightGraphBuilder.nodeTree[code]
                                columnSpecs.forEach { spec ->
                                    ConceptDiffTableCell(modifier = Modifier.height(cellHeight),
                                        weight = spec.weight,
                                        tooltipText = { spec.tooltipOnlyInX?.invoke(code, concept!!) },
                                        content = {
                                            spec.cellContentOnlyInX.invoke(code, concept!!, isLeft)
                                        })
                                }
                            }
                            else -> {
                                val diff = tableData.conceptDiff[code]
                                    ?: throw IllegalStateException("the code $code is not found in the diff")
                                columnSpecs.forEach { spec ->
                                    ConceptDiffTableCell(modifier = Modifier.height(cellHeight),
                                        weight = spec.weight,
                                        tooltipText = { spec.tooltipText?.invoke(code) },
                                        content = { spec.cellContent.invoke(code, diff) })
                                }
                            }
                        }

                    }
                }
            }
            Carousel(
                state = lazyListState,
                colors = CarouselDefaults.colors(MaterialTheme.colorScheme.onTertiaryContainer),
                modifier = Modifier.padding(8.dp).width(8.dp).fillMaxHeight(0.9f)
            )
        }
    }
}