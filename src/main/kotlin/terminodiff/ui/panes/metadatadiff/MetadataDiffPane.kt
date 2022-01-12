package terminodiff.ui.panes.metadatadiff

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.MetadataDiff
import terminodiff.ui.MouseOverPopup
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetadataDiffPanel(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean
) {

    val listState = rememberLazyListState()
    val diffColors by remember { mutableStateOf(getDiffColors(useDarkTheme)) }

    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(
            Modifier.padding(8.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = localizedStrings.metadataDiff,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            MetadataDiffTable(
                lazyListState = listState,
                diffDataContainer = diffDataContainer,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
            )
        }
    }
}

@Composable
fun MetadataDiffTable(
    lazyListState: LazyListState,
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
) {

    val columnSpecs = listOf(
        ColumnSpec.propertyColumnSpec(localizedStrings),
        ColumnSpec.resultColumnSpec(localizedStrings, diffColors),
        ColumnSpec.leftValueColumnSpec(localizedStrings, diffDataContainer.leftCodeSystem!!),
        ColumnSpec.rightValueColumnSpec(localizedStrings, diffDataContainer.rightCodeSystem!!)
    )
    diffDataContainer.codeSystemDiff?.metadataDifferences?.comparisons?.let { comparisons ->
        LazyTable(
            columnSpecs = columnSpecs,
            lazyListState = lazyListState,
            tableData = comparisons,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            keyFun = { it.diffItem.label.invoke(localizedStrings) }
        )
    }
}

private fun ColumnSpec.Companion.propertyColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<MetadataDiff.MetadataComparison>(
        title = localizedStrings.property,
        weight = 0.1f
    ) { comparison ->
        SelectableText(
            comparison.diffItem.label.invoke(localizedStrings),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }

private fun ColumnSpec.Companion.resultColumnSpec(localizedStrings: LocalizedStrings, diffColors: DiffColors) =
    ColumnSpec<MetadataDiff.MetadataComparison>(title = localizedStrings.comparison, weight = 0.2f) { comparison ->
        val (backgroundColor, foregroundColor) = colorPairForDiffResult(comparison, diffColors)
        @Composable
        fun renderDiffChip() {
            DiffChip(
                text = localizedStrings.metadataDiffResults_.invoke(comparison.result),
                backgroundColor = backgroundColor,
                textColor = foregroundColor,
                fontStyle = if (comparison.explanation != null) FontStyle.Italic else FontStyle.Normal
            )
        }
        if (comparison.explanation != null) {
            MouseOverPopup(text = comparison.explanation.invoke(localizedStrings)) {
                renderDiffChip()
            }
        } else renderDiffChip()
    }

@Composable
private fun TextForLeftRightValue(
    result: MetadataDiff.MetadataComparison,
    codeSystem: CodeSystem
) {
    val text: String? = result.diffItem.renderDisplay.invoke(codeSystem)
    SelectableText(text = text ?: "null", fontStyle = if (text == null) FontStyle.Italic else FontStyle.Normal)
}

private fun ColumnSpec.Companion.leftValueColumnSpec(
    localizedStrings: LocalizedStrings,
    leftCodeSystem: CodeSystem,
) =
    ColumnSpec<MetadataDiff.MetadataComparison>(
        title = localizedStrings.leftValue,
        weight = 0.25f,
        mergeIf = { comparison ->
            comparison.result == MetadataDiff.MetadataComparisonResult.IDENTICAL
        }) {
        TextForLeftRightValue(it, leftCodeSystem)
    }

private fun ColumnSpec.Companion.rightValueColumnSpec(
    localizedStrings: LocalizedStrings,
    rightCodeSystem: CodeSystem,
) =
    ColumnSpec<MetadataDiff.MetadataComparison>(
        title = localizedStrings.rightValue,
        weight = 0.25f
    ) {
        TextForLeftRightValue(it, rightCodeSystem)
    }

