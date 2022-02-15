package terminodiff.terminodiff.ui.panes.metadatadiff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.*
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.LazyTable

@Suppress("UNCHECKED_CAST")
@Composable
fun MetadataDiffDetailsDialog(
    comparison: MetadataListComparison<*, *>,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
    onClose: () -> Unit,
) {
    val listState = rememberLazyListState()
    val diffColors by derivedStateOf { getDiffColors(useDarkTheme = useDarkTheme) }
    val title by derivedStateOf { comparison.diffItem.label.invoke(localizedStrings) }
    Dialog(onCloseRequest = onClose,
        title = title,
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(1024.dp, 512.dp))) {
        Column(Modifier.background(colorScheme.primaryContainer).padding(top = 4.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = typography.titleMedium, color = colorScheme.onPrimaryContainer)
            DrawTable(comparison, localizedStrings, diffColors, listState)
        }
    }
}

/**
 * this function is much more complex that it feels like it should be, but erased generic type parameters do require
 * special care. The when block uses Kotlin Smart Casts to provide the right type parameters to the LazyTable composable
 * -> and it works well :)
 */
@Composable
private fun DrawTable(
    comparison: MetadataListComparison<*, *>,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    listState: LazyListState,
) {

    /**
     * internal function to have fewer parameters in the when block below
     */
    @Composable
    fun <KeyType : KeyedListDiffResult<*, *>> internalDrawTable(
        comparisonResult: List<KeyType>,
        columnSpecs: List<ColumnSpec<KeyType>>,
        keyFun: (KeyType) -> String? = { it.key.toString() },
    ) = LazyTable(
        modifier = Modifier.padding(8.dp),
        columnSpecs = columnSpecs,
        backgroundColor = colorScheme.primaryContainer,
        lazyListState = listState,
        zebraStripingColor = colorScheme.tertiaryContainer,
        tableData = comparisonResult,
        localizedStrings = localizedStrings,
        keyFun = keyFun,
    )
    when (comparison) {
        is IdentifierListComparison -> internalDrawTable(
            comparisonResult = comparison.detailedResult,
            columnSpecs = comparison.listDiffItem.getColumns(localizedStrings, diffColors))
        is ContactListComparison -> internalDrawTable(
            comparisonResult = comparison.detailedResult,
            columnSpecs = comparison.listDiffItem.getColumns(localizedStrings, diffColors))
        is CodeableConceptComparison -> internalDrawTable(
            comparisonResult = comparison.detailedResult,
            columnSpecs = comparison.listDiffItem.getColumns(localizedStrings, diffColors))
        is UsageContextComparison -> internalDrawTable(
            comparisonResult = comparison.detailedResult,
            columnSpecs = comparison.listDiffItem.getColumns(localizedStrings, diffColors))
        else -> Text("Not yet implemented", style = typography.headlineMedium, color = colorScheme.error)
    }
}


