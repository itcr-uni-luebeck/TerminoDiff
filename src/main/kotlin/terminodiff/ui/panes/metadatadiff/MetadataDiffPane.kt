package terminodiff.ui.panes.metadatadiff

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.MetadataComparison
import terminodiff.terminodiff.engine.metadata.MetadataListComparison
import terminodiff.terminodiff.ui.panes.metadatadiff.MetadataDiffDetailsDialog
import terminodiff.terminodiff.ui.panes.metadatadiff.metadataColumnSpecs
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.LazyTable

private val logger: Logger = LoggerFactory.getLogger("MetadataDiffPanel")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetadataDiffPanel(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
) {

    val listState = rememberLazyListState()
    val diffColors by remember { mutableStateOf(getDiffColors(useDarkTheme)) }
    var listDetailsDialogData: MetadataListComparison<*, *>? by remember { mutableStateOf(null) }

    listDetailsDialogData?.let { listDetailsData ->
        MetadataDiffDetailsDialog(listDetailsData, localizedStrings, useDarkTheme) {
            listDetailsDialogData = null
        }
    }

    Card(
        modifier = Modifier.padding(8.dp).fillMaxSize(),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(Modifier.padding(8.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text = localizedStrings.metadataDiff,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer)

            MetadataDiffTable(lazyListState = listState,
                diffDataContainer = diffDataContainer,
                localizedStrings = localizedStrings,
                diffColors = diffColors) { comparison ->
                when (val listComparison = comparison as? MetadataListComparison<*, *>) {
                    null -> return@MetadataDiffTable
                    else -> {
                        logger.info("clicked details button for ${listComparison.diffItem.label.invoke(localizedStrings)}")
                        listDetailsDialogData = listComparison}
                }
            }
        }
    }
}

@Composable
fun MetadataDiffTable(
    lazyListState: LazyListState,
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    onShowDetailsClick: (MetadataComparison) -> Unit,
) =
    diffDataContainer.codeSystemDiff?.metadataDifferences?.comparisons?.let { comparisons ->
        LazyTable(columnSpecs = metadataColumnSpecs(localizedStrings,
            diffColors,
            diffDataContainer,
            onShowDetailsClick),
            lazyListState = lazyListState,
            tableData = comparisons,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            zebraStripingColor = MaterialTheme.colorScheme.primaryContainer,
            keyFun = { it.diffItem.label.invoke(localizedStrings) })
    }





