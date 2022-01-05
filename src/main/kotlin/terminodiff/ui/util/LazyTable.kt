package terminodiff.ui.util

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import terminodiff.ui.MouseOverPopup

@Composable
fun <T> LazyTable(
    modifier: Modifier = Modifier,
    columnSpecs: List<ColumnSpec<T>>,
    cellHeight: Dp = 50.dp,
    cellBorderColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    backgroundColor: Color,
    lazyListState: LazyListState,
    tableData: List<T>,
    keyFun: (T) -> Any
) {
    val contentColor = contentColorFor(backgroundColor)
    Column(modifier = modifier.padding(16.dp)) {
        // draw the header cells
        Row(modifier.fillMaxWidth()) {
            columnSpecs.forEach { HeaderCell(it, cellBorderColor, contentColor) }
        }
        Divider(color = cellBorderColor, thickness = 1.dp)

        // the actual cells, contained by LazyColumn
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LazyColumn(state = lazyListState) {
                items(items = tableData, key = keyFun) { data ->
                    val skipped = mutableListOf<Int>()
                    Row(Modifier.wrapContentHeight()) {
                        columnSpecs.forEachIndexed { specIndex, spec ->
                            if (specIndex in skipped) return@forEachIndexed
                            if (spec.mergeIf != null) {
                                if (spec.mergeIf.invoke(data)) {
                                    val nextSpec = columnSpecs.getOrNull(specIndex + 1) ?: return@forEachIndexed
                                    TableCell(
                                        modifier = modifier.height(cellHeight),
                                        weight = spec.weight + nextSpec.weight,
                                        content = {
                                            spec.content(data)
                                        }
                                    )
                                    skipped.add(specIndex + 1)
                                    return@forEachIndexed
                                }
                            }
                            TableCell(
                                modifier = Modifier.height(cellHeight),
                                weight = spec.weight,
                                content = {
                                    spec.content(data)
                                }
                            )
                        }
                    }
                }
            }
            Carousel(
                state = lazyListState,
                colors = CarouselDefaults.colors(cellBorderColor),
                modifier = Modifier.padding(8.dp).width(8.dp).fillMaxHeight(0.9f)
            )
        }
    }
}

@Composable
fun RowScope.HeaderCell(
    spec: ColumnSpec<*>,
    cellBorderColor: Color,
    contentColor: Color
) {
    Box(
        Modifier.border(1.dp, cellBorderColor).weight(spec.weight).padding(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = spec.title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun RowScope.TableCell(
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

data class ColumnSpec<T>(
    val title: String,
    val weight: Float,
    val tooltipText: ((T) -> () -> String?)? = null,
    val mergeIf: ((T) -> Boolean)? = null,
    val content: @Composable (T) -> Unit,
) {
    companion object
}