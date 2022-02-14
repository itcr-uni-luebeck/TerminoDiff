package terminodiff.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.xdrop.fuzzywuzzy.FuzzySearch
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.ui.panes.loaddata.panes.LabeledTextField
import terminodiff.ui.MouseOverPopup
import java.util.*

@Composable
fun <T> LazyTable(
    modifier: Modifier = Modifier,
    columnSpecs: List<ColumnSpec<T>>,
    cellHeight: Dp = 50.dp,
    cellBorderColor: Color = colorScheme.onTertiaryContainer,
    backgroundColor: Color,
    foregroundColor: Color = colorScheme.contentColorFor(backgroundColor),
    lazyListState: LazyListState,
    zebraStripingColor: Color? = backgroundColor.copy(0.5f),
    tableData: List<T>,
    localizedStrings: LocalizedStrings,
    keyFun: (T) -> String?,
) = Column(modifier = modifier.fillMaxWidth().padding(4.dp)) {
    var currentFilterTitleToPredicate: Pair<String, (T, String) -> Boolean>? by remember { mutableStateOf(null) }
    var showFilterDialog: Boolean by remember { mutableStateOf(false) }
    var currentFilterString: String by remember { mutableStateOf("") }
    val filteredData by derivedStateOf {
        when {
            currentFilterTitleToPredicate == null -> tableData
            currentFilterString.isBlank() -> tableData
            else -> tableData.filter { // apply the filter string
                currentFilterTitleToPredicate!!.second.invoke(it, currentFilterString)
            }
        }
    }
    if (showFilterDialog) {
        currentFilterTitleToPredicate!!.let { (title, _) ->
            ShowFilterDialog(title = title,
                localizedStrings = localizedStrings,
                textFieldValue = currentFilterString,
                onClose = { filter ->
                    if (filter != null) {
                        currentFilterString = filter
                    }
                    showFilterDialog = false
                })
        }
    }

    // draw the header row
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        columnSpecs.forEach { columnSpec ->
            val filteredColumnTitle = currentFilterTitleToPredicate?.first
            HeaderCell(columnSpec = columnSpec,
                cellBorderColor = cellBorderColor,
                contentColor = foregroundColor,
                localizedStrings = localizedStrings,
                filteredColumnTitle = filteredColumnTitle,
                searchFilterPresent = currentFilterString.isNotBlank(),
                onSearchClearClick = {
                    currentFilterTitleToPredicate = null
                    currentFilterString = ""
                },
                onSearchClick = { title, filterFun ->
                    currentFilterTitleToPredicate = title to filterFun
                    showFilterDialog = true
                })
        }
    }
    Divider(color = cellBorderColor, thickness = 1.dp)

// the actual cells, contained by LazyColumn
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LazyColumn(state = lazyListState) {
            itemsIndexed(items = filteredData, key = { index, _ ->
                "$keyFun-$index"
            }) { index, data ->
                val rowBackground = when (zebraStripingColor) {
                    null -> backgroundColor
                    else -> if (index % 2 == 0) zebraStripingColor else backgroundColor
                }
                val rowForeground = colorScheme.contentColorFor(rowBackground)
                val skipped = mutableListOf<Int>()
                Row(Modifier.wrapContentHeight()) {
                    columnSpecs.forEachIndexed { specIndex, spec ->
                        if (specIndex in skipped) return@forEachIndexed
                        if (spec.mergeIf != null && spec.mergeIf.invoke(data)) {
                            val nextSpec = columnSpecs.getOrNull(specIndex + 1) ?: return@forEachIndexed
                            TableCell(modifier = Modifier.height(cellHeight),
                                weight = spec.weight + nextSpec.weight,
                                tooltipText = spec.tooltipText?.invoke(data),
                                backgroundColor = rowBackground,
                                foregroundColor = rowForeground) { spec.content(data) }
                            skipped.add(specIndex + 1)
                            return@forEachIndexed
                        }
                        TableCell(modifier = Modifier.height(cellHeight),
                            weight = spec.weight,
                            tooltipText = spec.tooltipText?.invoke(data),
                            backgroundColor = rowBackground,
                            foregroundColor = rowForeground) { spec.content(data) }
                    }
                }
            }
        }
        // the indicator for scrolling
        Carousel(state = lazyListState,
            colors = CarouselDefaults.colors(cellBorderColor),
            modifier = Modifier.padding(8.dp).width(8.dp).fillMaxHeight(0.9f))
    }
}

@Composable
fun ShowFilterDialog(
    title: String,
    localizedStrings: LocalizedStrings,
    textFieldValue: String,
    onClose: (String?) -> Unit,
) {
    var inputText: String by remember { mutableStateOf(textFieldValue) }
    Dialog(onCloseRequest = {
        onClose(null)
    }) {
        Column(modifier = Modifier.fillMaxSize().background(colorScheme.primaryContainer),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally) {
            LabeledTextField(value = inputText, onValueChange = { inputText = it }, labelText = title)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(modifier = Modifier.wrapContentSize(),
                    onClick = { onClose(null) },
                    colors = ButtonDefaults.buttonColors(colorScheme.tertiary, colorScheme.onTertiary)) {
                    Text(localizedStrings.closeReject, color = colorScheme.onTertiary)
                }
                Button(modifier = Modifier.wrapContentSize(), onClick = {
                    onClose(inputText)
                }, colors = ButtonDefaults.buttonColors(colorScheme.secondary, colorScheme.onSecondary)) {
                    Text(localizedStrings.closeAccept, color = colorScheme.onSecondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> RowScope.HeaderCell(
    columnSpec: ColumnSpec<T>,
    cellBorderColor: Color,
    contentColor: Color,
    localizedStrings: LocalizedStrings,
    searchFilterPresent: Boolean,
    onSearchClearClick: (() -> Unit),
    onSearchClick: (String, (T, String) -> Boolean) -> Unit,
    filteredColumnTitle: String?,
) {
    Box(Modifier.border(1.dp, cellBorderColor).weight(columnSpec.weight).fillMaxHeight().padding(2.dp)) {
        Row(modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = columnSpec.title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center)
            if (columnSpec.searchPredicate != null) {
                val enableSearch = when (filteredColumnTitle) {
                    null -> true
                    else -> !searchFilterPresent || filteredColumnTitle == columnSpec.title
                }
                CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                    MouseOverPopup(localizedStrings.search) {
                        IconButton(
                            modifier = Modifier.size(32.dp).padding(4.dp),
                            onClick = { onSearchClick(columnSpec.title, columnSpec.searchPredicate) },
                            enabled = enableSearch) {
                            Icon(Icons.Default.Search,
                                contentDescription = localizedStrings.search,
                                tint = when (enableSearch) {
                                    true -> contentColor
                                    else -> contentColor.copy(0.5f)
                                })
                        }
                    }
                    if (searchFilterPresent && filteredColumnTitle == columnSpec.title) {
                        MouseOverPopup(text = localizedStrings.clearSearch) {
                            IconButton(
                                modifier = Modifier.size(32.dp).padding(4.dp),
                                onClick = onSearchClearClick) {
                                Icon(Icons.Default.Backspace,
                                    contentDescription = localizedStrings.clearSearch,
                                    tint = contentColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    modifier: Modifier = Modifier,
    weight: Float,
    tooltipText: String?,
    backgroundColor: Color,
    foregroundColor: Color,
    content: @Composable () -> Unit,
) = Row(modifier = modifier.border(1.dp, colorScheme.onTertiaryContainer).weight(weight).padding(2.dp)
    .background(backgroundColor),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically) {
    CompositionLocalProvider(LocalContentColor provides foregroundColor) {
        when (tooltipText) {
            null -> content()
            else -> MouseOverPopup(text = tooltipText,
                backgroundColor = colorScheme.primaryContainer,
                foregroundColor = colorScheme.onPrimaryContainer,
                content = content)
        }
    }
}

open class ColumnSpec<T>(
    val title: String,
    val weight: Float,
    val searchPredicate: ((T, String) -> Boolean)? = null,
    val tooltipText: ((T) -> String?)? = null,
    val mergeIf: ((T) -> Boolean)? = null,
    val content: @Composable (T) -> Unit,
) {
    companion object

    class StringSearchableColumnSpec<T>(
        title: String,
        weight: Float,
        instanceGetter: T.() -> String?,
        mergeIf: ((T) -> Boolean)? = null,
        tooltipText: ((T) -> String?)? = null,
        content: @Composable (T) -> Unit,
    ) : ColumnSpec<T>(
        title = title,
        weight = weight,
        searchPredicate = { value, search ->
            when (val instanceValue = instanceGetter.invoke(value)?.lowercase(Locale.getDefault())) {
                null -> false
                else -> {
                    val fuzzyScore = FuzzySearch.partialRatio(instanceValue, search.lowercase(Locale.getDefault()))
                    fuzzyScore >= 75
                }
            }
        },
        mergeIf = mergeIf,
        tooltipText = tooltipText,
        content = content
    ) {
        /**
         * constructor overload that takes care of drawing the content by providing a tooltip and content as selectable text, with default styling
         */
        constructor(
            title: String,
            weight: Float,
            instanceGetter: T.() -> String?,
            mergeIf: ((T) -> Boolean)? = null,
        ) : this(
            title = title,
            weight = weight,
            instanceGetter = instanceGetter,
            mergeIf = mergeIf,
            tooltipText = { it.instanceGetter() },
            content = {
                SelectableText(text = it.instanceGetter())
            },
        )
    }
}