package terminodiff.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.Carousel
import terminodiff.ui.util.CarouselDefaults
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.colorPairForConceptDiffResult
import java.util.*

@Composable
fun ConceptDiffPanel(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean
) {
    val lazyListState = rememberLazyListState()
    val diffColors by remember { mutableStateOf(getDiffColors(useDarkTheme = useDarkTheme)) }
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                localizedStrings.conceptDiff,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            DiffDataTable(diffDataContainer, localizedStrings, lazyListState = lazyListState, diffColors = diffColors)
        }
    }
}

@Composable
fun DiffDataTable(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    lazyListState: LazyListState,
    diffColors: DiffColors
) {
    val differences = diffDataContainer.codeSystemDiff?.conceptDifferences
        ?: throw IllegalStateException("the difference table is empty when the concept diff is composed.")
    val leftBuilder = diffDataContainer.leftGraphBuilder
        ?: throw IllegalStateException("the left graph is empty when the concept diff is composed.")
    val rightBuilder = diffDataContainer.rightGraphBuilder
        ?: throw IllegalStateException("the right graph table is empty when the concept diff is composed.")
    TableScreen(
        conceptDiffs = differences,
        leftGraphBuilder = leftBuilder,
        rightGraphBuilder = rightBuilder,
        localizedStrings = localizedStrings,
        lazyListState = lazyListState,
        diffColors = diffColors
    )

}

@Composable
fun RowScope.TableCell(
    modifier: Modifier = Modifier,
    weight: Float,
    tooltipText: (() -> String)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.onTertiaryContainer)
            .weight(weight)
            .padding(2.dp)
    ) {
        when (tooltipText) {
            null -> content()
            else -> MouseOverPopup(tooltipText(), content = content)
        }
    }
}

data class ColumnSpec(
    val title: String,
    val weight: Float,
    val tooltipText: ((String) -> String)? = null,
    val cellContent: @Composable (String, ConceptDiff) -> Unit,
)

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
        text = localizedStrings.`conceptDiffResults$`.invoke(result),
        backgroundColor = colorsForResult.first,
        textColor = colorsForResult.second
    )
//Text(text = result?.toString() ?: "ERROR")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableScreen(
    conceptDiffs: TreeMap<String, ConceptDiff>,
    leftGraphBuilder: CodeSystemGraphBuilder,
    rightGraphBuilder: CodeSystemGraphBuilder,
    localizedStrings: LocalizedStrings,
    lazyListState: LazyListState,
    diffColors: DiffColors
) {
    val columnSpecs = listOf(
        ColumnSpec(localizedStrings.code, 0.15f) { code, _ ->
            Text(code)
        },
        ColumnSpec(localizedStrings.display, weight = 0.25f, tooltipText = { code ->
            val leftDisplay = leftGraphBuilder.nodeTree[code]?.display ?: "ERROR"
            val rightDisplay = rightGraphBuilder.nodeTree[code]?.display ?: "ERROR"
            "'$leftDisplay' vs. '$rightDisplay'"
        }) { _, diff ->
            ChipForConceptDiffResult(diff.conceptComparison, localizedStrings.display, localizedStrings, diffColors)
        },
        ColumnSpec(localizedStrings.definition, weight = 0.25f) { _, diff ->
            ChipForConceptDiffResult(diff.conceptComparison, localizedStrings.definition, localizedStrings, diffColors)
        }
    )
    // TODO: 03/01/22 implement paging somehow!
    Column(Modifier.fillMaxWidth().fillMaxHeight(0.33f).padding(16.dp)) {
        Row {
            columnSpecs.forEach { spec ->
                TableCell(weight = spec.weight) {
                    Text(spec.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LazyColumn(
                //Modifier.fillMaxSize().padding(16.dp),
                //.background(MaterialTheme.colorScheme.secondaryContainer),
                state = lazyListState
            ) {
                items(conceptDiffs.toList()) { (code, diff) ->
                    Row {
                        columnSpecs.forEach { spec ->
                            TableCell(
                                weight = spec.weight,
                                tooltipText = spec.tooltipText?.let { { spec.tooltipText.invoke(code) } },
                                content = { spec.cellContent.invoke(code, diff) })
                        }
                    }
                }
            }
            Carousel(
                state = lazyListState,
                colors = CarouselDefaults.colors(MaterialTheme.colorScheme.onTertiaryContainer),
                modifier = Modifier
                    .padding(8.dp)
                    .width(8.dp)
                    .fillMaxHeight(0.9f)
            )
        }

    }
//    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
//        // Here is the header
//        item {
//            Row(Modifier.background(Color.Gray)) {
//                TableCell(text = "Column 1", weight = column1Weight)
//                TableCell(text = "Column 2", weight = column2Weight)
//            }
//        }
//        // Here are all the lines of your table.
//        items(tableData) {
//            val (id, text) = it
//            Row(Modifier.fillMaxWidth()) {
//                TableCell(text = id.toString(), weight = column1Weight)
//                TableCell(text = text, weight = column2Weight)
//            }
//        }
//    }
}