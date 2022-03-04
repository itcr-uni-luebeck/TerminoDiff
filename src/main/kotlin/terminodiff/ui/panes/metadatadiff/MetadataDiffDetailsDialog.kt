package terminodiff.terminodiff.ui.panes.metadatadiff

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.*
import terminodiff.terminodiff.ui.util.TerminodiffDialog
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
    TerminodiffDialog(
        title = title,
        onCloseRequest = onClose,
    ) {
        Text(text = title, style = typography.titleMedium, color = colorScheme.onPrimaryContainer)
        DrawTable(comparison, localizedStrings, diffColors, listState)
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


