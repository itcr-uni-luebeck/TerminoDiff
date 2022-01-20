package terminodiff.ui.panes.metadatadiff

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.terminodiff.engine.metadata.MetadataComparisonResult
import terminodiff.terminodiff.engine.metadata.MetadataDiff
import terminodiff.terminodiff.engine.metadata.MetadataKeyedListDiffItem
import terminodiff.terminodiff.engine.metadata.StringComparisonItem
import terminodiff.ui.MouseOverPopup
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.*

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

    val columnSpecs = listOf(propertyColumnSpec(localizedStrings),
        resultColumnSpec(localizedStrings, diffColors),
        leftValueColumnSpec(localizedStrings, diffDataContainer.leftCodeSystem!!),
        rightValueColumnSpec(localizedStrings, diffDataContainer.rightCodeSystem!!))
    diffDataContainer.codeSystemDiff?.metadataDifferences?.comparisons?.let { comparisons ->
        LazyTable(columnSpecs = columnSpecs,
            lazyListState = lazyListState,
            tableData = comparisons,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            zebraStripingColor = MaterialTheme.colorScheme.primaryContainer,
            keyFun = { it.diffItem.label.invoke(localizedStrings) })
    }
}

private fun propertyColumnSpec(localizedStrings: LocalizedStrings): ColumnSpec<MetadataDiff.MetadataComparison> {
    val defaultStrings = getStrings(SupportedLocale.getDefaultLocale())
    val selectableContent: @Composable (MetadataDiff.MetadataComparison) -> Unit = { comparison ->
        SelectableText(comparison.diffItem.label.invoke(localizedStrings),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center)
    }
    return ColumnSpec(title = localizedStrings.property, weight = 0.1f, content = { comparison ->
        // add the (english) default name to the property column as a tooltip, since FHIR spec is english.
        val localizedName = comparison.diffItem.label.invoke(localizedStrings)
        val defaultLocalizedName = comparison.diffItem.label.invoke(defaultStrings)
        when {
            localizedName != defaultLocalizedName -> MouseOverPopup(text = defaultLocalizedName,
                content = { selectableContent.invoke(comparison) })
            else -> selectableContent.invoke(comparison)
        }
    })
}

private fun resultColumnSpec(localizedStrings: LocalizedStrings, diffColors: DiffColors) =
    ColumnSpec<MetadataDiff.MetadataComparison>(title = localizedStrings.comparison, weight = 0.2f) { comparison ->
        val (backgroundColor, foregroundColor) = colorPairForDiffResult(comparison, diffColors)
        val resultText = localizedStrings.metadataDiffResults_.invoke(comparison.result)
        val fontStyle = if (comparison.explanation != null) FontStyle.Italic else FontStyle.Normal

        @Composable
        fun renderDiffChip() {
            DiffChip(text = resultText,
                backgroundColor = backgroundColor,
                textColor = foregroundColor,
                fontStyle = fontStyle)
        }

        @Composable
        fun renderDiffButton() {
            Button(
                onClick = {
                    if (comparison.diffItem is MetadataKeyedListDiffItem<*, *>) {
                        logger.warn("clicked diff button for ${comparison.diffItem.label.invoke(localizedStrings)}")
                        // TODO: 19/01/22 nyi dialog -> this can be achieved by displaying the three params of MetadataListDiffItem as columns :)
                    }
                },
                elevation = ButtonDefaults.elevation(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor, foregroundColor)
            ) {
                Text(text = resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = fontStyle,
                    color = foregroundColor)
            }
        }

        val render: @Composable () -> Unit = {
            when (comparison.diffItem) {
                is MetadataKeyedListDiffItem<*, *> -> renderDiffButton()
                //comparison.diffItem is MetadataListDiffItem<*, *, *> && comparison.result == MetadataComparisonResult.DIFFERENT -> renderDiffButton()
                else -> renderDiffChip()
            }
        }
        if (comparison.explanation != null) {
            MouseOverPopup(text = comparison.explanation.invoke(localizedStrings)) {
                render()
            }
        } else render()
    }

@Composable
private fun TextForLeftRightValue(
    result: MetadataDiff.MetadataComparison,
    codeSystem: CodeSystem,
) {
    val text: String? = result.diffItem.getRenderDisplay(codeSystem)
    SelectableText(text = text, fontStyle = when {
        text == null -> FontStyle.Italic
        result.diffItem is StringComparisonItem && result.diffItem.drawItalic -> FontStyle.Italic
        else -> FontStyle.Normal
    })
}

private fun leftValueColumnSpec(
    localizedStrings: LocalizedStrings,
    leftCodeSystem: CodeSystem,
) = ColumnSpec<MetadataDiff.MetadataComparison>(title = localizedStrings.leftValue,
    weight = 0.25f,
    mergeIf = { comparison ->
        comparison.result == MetadataComparisonResult.IDENTICAL
    }) {
    TextForLeftRightValue(it, leftCodeSystem)
}

private fun rightValueColumnSpec(
    localizedStrings: LocalizedStrings,
    rightCodeSystem: CodeSystem,
) = ColumnSpec<MetadataDiff.MetadataComparison>(title = localizedStrings.rightValue, weight = 0.25f) {
    TextForLeftRightValue(it, rightCodeSystem)
}

