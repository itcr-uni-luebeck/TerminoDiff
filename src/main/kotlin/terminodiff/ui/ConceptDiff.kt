package terminodiff.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.impl.theScope.fromInterop
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
    var activeFilter by remember { mutableStateOf(ToggleableChipSpec.showAll) }
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
            FilterGroup(localizedStrings, activeFilter = activeFilter) {
                logger.info("changed filter to $it")
                activeFilter = it
            }
            DiffDataTable(
                diffDataContainer, localizedStrings, diffColors = diffColors, activeFilter = activeFilter
            )
        }
    }
}

/**
 * add a static field for the unique identification of this chip
 */
val ToggleableChipSpec.Companion.showAll get() = "show-all"
val ToggleableChipSpec.Companion.showDifferent get() = "show-different"
val ToggleableChipSpec.Companion.showIdentical get() = "show-identical"
val ToggleableChipSpec.Companion.onlyInLeft get() = "show-only-in-left"
val ToggleableChipSpec.Companion.onlyInRight get() = "show-only-in-right"

@Composable
fun FilterGroup(localizedStrings: LocalizedStrings, activeFilter: String, onFilterChange: (String) -> Unit) {
    val specs = listOf(
        ToggleableChipSpec(ToggleableChipSpec.showAll, localizedStrings.showAll),
        ToggleableChipSpec(ToggleableChipSpec.showIdentical, localizedStrings.showIdentical),
        ToggleableChipSpec(ToggleableChipSpec.showDifferent, localizedStrings.showDifferent),
        ToggleableChipSpec(ToggleableChipSpec.onlyInLeft, localizedStrings.onlyInLeft),
        ToggleableChipSpec(ToggleableChipSpec.onlyInRight, localizedStrings.onlyInRight)
    )
    ToggleableChipGroup(specs, selectedItem = activeFilter, onSelectionChanged = onFilterChange)
}

@Composable
fun DiffDataTable(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    activeFilter: String
) {
    if (!diffDataContainer.isInitialized || diffDataContainer.codeSystemDiff == null) throw IllegalStateException("the diff data container is not initialized")
    val lazyListState = rememberLazyListState(0)

    TableScreen(
        activeFilter = activeFilter,
        conceptDiffs = diffDataContainer.codeSystemDiff?.conceptDifferences
            ?: throw IllegalStateException("the difference table is empty when the concept diff is composed."),
        leftGraphBuilder = diffDataContainer.leftGraphBuilder
            ?: throw IllegalStateException("the left graph is empty when the concept diff is composed."),
        rightGraphBuilder = diffDataContainer.rightGraphBuilder
            ?: throw IllegalStateException("the right graph table is empty when the concept diff is composed."),
        onlyInLeftConcepts = diffDataContainer.codeSystemDiff?.onlyInLeftConcepts
            ?: throw IllegalStateException("the only-in-left list is empty when the concept diff is composed"),
        onlyInRightConcepts = diffDataContainer.codeSystemDiff?.onlyInRightConcepts
            ?: throw IllegalStateException("the only-in-right list is empty when the concept diff is composed"),
        localizedStrings = localizedStrings,
        lazyListState = lazyListState,
        diffColors = diffColors
    )
}

@Composable
fun RowScope.TableCell(
    modifier: Modifier = Modifier,
    weight: Float,
    tooltipText: String? = null,
    showTooltipAfterContent: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier.border(1.dp, MaterialTheme.colorScheme.onTertiaryContainer).weight(weight).padding(2.dp)
    ) {
        when (tooltipText) {
            null -> Row(modifier = Modifier.matchParentSize().padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = { content() })
            else -> {
                MouseOverPopup(
                    text = tooltipText,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.border(1.dp, Color.Red),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        content()
                        if (showTooltipAfterContent) Text(
                            text = tooltipText,
                            style = MaterialTheme.typography.labelMedium,
                            overflow = TextOverflow.Clip,
                            fontStyle = if (tooltipText == "null") FontStyle.Italic else FontStyle.Normal
                        )
                    }
                }
            }
        }
    }
}

data class ColumnSpec(
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
                leftValue == null && rightValue == null -> "null"
                leftValue == rightValue -> "'$leftValue'"
                else -> "'$leftValue' vs. '$rightValue'"
            }

        }

        @Composable
        fun codeColumnSpec(localizedStrings: LocalizedStrings): ColumnSpec {
            @Composable
            fun textRow(code: String) = Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(code, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
            return ColumnSpec(title = localizedStrings.code, weight = 0.1f, cellContent = { code, _ ->
                textRow(code)
            }, cellContentOnlyInX = { code, _, _ -> textRow(code) })
        }

        fun displayColumnSpec(
            localizedStrings: LocalizedStrings,
            leftGraphBuilder: CodeSystemGraphBuilder,
            rightGraphBuilder: CodeSystemGraphBuilder,
            diffColors: DiffColors
        ) = ColumnSpec(
            localizedStrings.display,
            weight = 0.25f,
            tooltipText = tooltipForCode(
                leftGraphBuilder = leftGraphBuilder,
                rightGraphBuilder = rightGraphBuilder,
                property = FhirConceptDetails::display
            ),
            tooltipOnlyInX = { _, concept -> concept.display },
            cellContent = { _, diff ->
                ChipForConceptDiffResult(diff.conceptComparison, localizedStrings.display, localizedStrings, diffColors)
            },
            cellContentOnlyInX = { _, concept, _ ->
                Text(
                    text = concept.display ?: "null",
                    fontStyle = if (concept.display == null) FontStyle.Italic else FontStyle.Normal
                )
            }
        )

        fun definitionColumnSpec(
            localizedStrings: LocalizedStrings,
            leftGraphBuilder: CodeSystemGraphBuilder,
            rightGraphBuilder: CodeSystemGraphBuilder,
            diffColors: DiffColors
        ) = ColumnSpec(
            title = localizedStrings.definition,
            weight = 0.25f,
            tooltipText = tooltipForCode(
                leftGraphBuilder = leftGraphBuilder,
                rightGraphBuilder = rightGraphBuilder,
                property = FhirConceptDetails::definition
            ),
            tooltipOnlyInX = { _, concept -> concept.definition },
            cellContent = { _, diff ->
                ChipForConceptDiffResult(
                    diff.conceptComparison, localizedStrings.definition, localizedStrings, diffColors
                )
            },
            cellContentOnlyInX = { _, concept, _ ->
                Text(
                    text = concept.definition ?: "null",
                    fontStyle = if (concept.definition == null) FontStyle.Italic else FontStyle.Normal
                )
            })

        fun propertyColumnSpec(
            localizedStrings: LocalizedStrings,
            leftGraphBuilder: CodeSystemGraphBuilder,
            rightGraphBuilder: CodeSystemGraphBuilder,
            diffColors: DiffColors
        ): ColumnSpec =
            ColumnSpec(
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
                                DiffChip(
                                    text = localizedStrings.numberDifferent_.invoke(diff.propertyComparison.size),
                                    colorPair = diffColors.yellowPair,
                                )
                            }
                        }
                    }
                }, cellContentOnlyInX = { _, _, _ ->
                    // TODO: 04/01/22
                })

        fun overallComparisonColumnSpec(
            localizedStrings: LocalizedStrings,
            diffColors: DiffColors,
        ): ColumnSpec = ColumnSpec(title = localizedStrings.overallComparison,
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
                    colorPair = colors, text = chipLabel
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
                        onlyOneVersionIcon = AppIconResource.loadXmlImageVector(AppIconResource.icLoadLeftFile)
                    }
                }
                DiffChip(
                    colorPair = diffColors.redPair, text = chipLabel, icon = onlyOneVersionIcon
                )
            })
    }
}

@Composable
private fun ChipForConceptDiffResult(
    conceptComparison: List<ConceptDiffResult>,
    labelToFind: String,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors
) {
    val result = conceptComparison.find { it.diffItem.label.invoke(localizedStrings) == labelToFind } ?: return
    val colorsForResult = colorPairForConceptDiffResult(result, diffColors)
    DiffChip(
        text = localizedStrings.conceptDiffResults_.invoke(result.result),
        backgroundColor = colorsForResult.first,
        textColor = colorsForResult.second,
        icon = null
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableScreen(
    conceptDiffs: TreeMap<String, ConceptDiff>,
    leftGraphBuilder: CodeSystemGraphBuilder,
    rightGraphBuilder: CodeSystemGraphBuilder,
    localizedStrings: LocalizedStrings,
    lazyListState: LazyListState,
    diffColors: DiffColors,
    activeFilter: String,
    onlyInLeftConcepts: List<String>,
    onlyInRightConcepts: List<String>
) {
    val columnSpecs = listOf(
        ColumnSpec.codeColumnSpec(localizedStrings),
        ColumnSpec.displayColumnSpec(localizedStrings, leftGraphBuilder, rightGraphBuilder, diffColors),
        ColumnSpec.definitionColumnSpec(localizedStrings, leftGraphBuilder, rightGraphBuilder, diffColors),
        ColumnSpec.propertyColumnSpec(localizedStrings, leftGraphBuilder, rightGraphBuilder, diffColors),
        ColumnSpec.overallComparisonColumnSpec(
            localizedStrings, diffColors,
        )
    )
    val cellHeight = 50.dp
    val differentCodesInDiff = conceptDiffs.filterValues { diff ->
        diff.conceptComparison.any { c -> c.result == ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT } || diff.propertyComparison.any()
    }.keys
    val sameCodesInDiff = conceptDiffs.keys.minus(differentCodesInDiff)

    val shownCodes = when (activeFilter) {
        ToggleableChipSpec.showDifferent -> onlyInLeftConcepts.plus(onlyInRightConcepts).plus(differentCodesInDiff)
        ToggleableChipSpec.onlyInLeft -> onlyInLeftConcepts
        ToggleableChipSpec.onlyInRight -> onlyInRightConcepts
        ToggleableChipSpec.showIdentical -> sameCodesInDiff
        else -> onlyInLeftConcepts.plus(onlyInRightConcepts).plus(conceptDiffs.keys)
    }.toSortedSet().toList() //make the list unique!
    Text(shownCodes.size.toString())
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth()) {
            columnSpecs.forEach { spec ->
                TableCell(weight = spec.weight) {
                    Text(
                        text = spec.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LazyColumn(state = lazyListState) {
                items(shownCodes) { code ->
                    Row(
                        Modifier.wrapContentHeight()
                    ) {
                        when (code) {
                            in onlyInLeftConcepts, in onlyInRightConcepts -> {
                                val isLeft = code in onlyInLeftConcepts
                                val concept =
                                    if (isLeft) leftGraphBuilder.nodeTree[code] else rightGraphBuilder.nodeTree[code]
                                columnSpecs.forEach { spec ->
                                    TableCell(modifier = Modifier.height(cellHeight),
                                        weight = spec.weight,
                                        tooltipText = spec.tooltipOnlyInX?.invoke(code, concept!!),
                                        showTooltipAfterContent = false,
                                        content = {
                                            spec.cellContentOnlyInX.invoke(code, concept!!, isLeft)
                                        })
                                }
                            }
                            else -> {
                                val diff = conceptDiffs[code]
                                    ?: throw IllegalStateException("the code $code is not found in the diff")
                                columnSpecs.forEach { spec ->
                                    TableCell(modifier = Modifier.height(cellHeight),
                                        weight = spec.weight,
                                        tooltipText = spec.tooltipText?.invoke(code),
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