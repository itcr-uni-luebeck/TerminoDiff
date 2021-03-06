package terminodiff.terminodiff.ui.panes.metadatadiff

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.i18n.SupportedLocale
import terminodiff.i18n.getStrings
import terminodiff.terminodiff.engine.metadata.*
import terminodiff.ui.MouseOverPopup
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.SelectableText
import terminodiff.ui.util.colorPairForDiffResult

fun metadataColumnSpecs(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    diffDataContainer: DiffDataContainer,
    onShowDetailsClick: (MetadataComparison) -> Unit,
) = listOf(propertyColumnSpec(localizedStrings),
    resultColumnSpec(localizedStrings, diffColors, onShowDetailsClick),
    leftValueColumnSpec(localizedStrings, diffDataContainer.leftCodeSystem!!),
    rightValueColumnSpec(localizedStrings, diffDataContainer.rightCodeSystem!!))

private fun propertyColumnSpec(localizedStrings: LocalizedStrings): ColumnSpec.StringSearchableColumnSpec<MetadataComparison> {
    val defaultStrings = getStrings(SupportedLocale.defaultLocale)
    val text: (MetadataComparison) -> String = { it.diffItem.label.invoke(localizedStrings) }
    val selectableContent: @Composable (MetadataComparison) -> Unit = { comparison ->
        SelectableText(text.invoke(comparison),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center)
    }
    return ColumnSpec.StringSearchableColumnSpec(
        title = localizedStrings.property,
        weight = 0.1f,
        instanceGetter = { text.invoke(this) },
        tooltipText = text,
    ) { comparison: MetadataComparison ->
        // add the (english) default name to the property column as a tooltip, since FHIR spec is english,
        // and translations may not always be as clear.
        val localizedName = comparison.diffItem.label.invoke(localizedStrings)
        val defaultLocalizedName = comparison.diffItem.label.invoke(defaultStrings)
        when {
            localizedName != defaultLocalizedName -> MouseOverPopup(text = defaultLocalizedName,
                content = { selectableContent.invoke(comparison) })
            else -> selectableContent.invoke(comparison)
        }
    }
}

private fun resultColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    onShowDetailsClick: (MetadataComparison) -> Unit,
) = ColumnSpec<MetadataComparison>(title = localizedStrings.comparison, weight = 0.2f, tooltipText = {
    (it as? MetadataListComparison<*, *>)?.let { listDiff ->
        localizedStrings.keyedListResult_.invoke(listDiff.detailedResult)
    }
}) { comparison ->
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
    fun renderDiffButton(onShowDetailsClick: (MetadataComparison) -> Unit) {
        FilledTonalButton(onClick = {
            if (comparison.diffItem is MetadataKeyedListDiffItem<*, *>) {
                onShowDetailsClick(comparison)
            }
        },
            colors = ButtonDefaults.filledTonalButtonColors(backgroundColor, foregroundColor)) {
            Text(text = resultText,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = fontStyle,
                color = foregroundColor)
        }
    }

    val render: @Composable () -> Unit = {
        when (comparison as? MetadataListComparison<*, *>) {
            null -> renderDiffChip()
            else -> if (comparison.detailedResult.any()) renderDiffButton(onShowDetailsClick) else renderDiffChip()
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
    result: MetadataComparison,
    codeSystem: CodeSystem,
    localizedStrings: LocalizedStrings,
    countItems: Int?,
) {
    val text: String? = result.diffItem.getRenderDisplay(codeSystem)
    val textWithCount = when (countItems) {
        null -> text
        0 -> localizedStrings.numberItems_.invoke(countItems)
        else -> "${localizedStrings.numberItems_.invoke(countItems)}: $text"
    }
    SelectableText(text = textWithCount, fontStyle = when {
        text == null -> FontStyle.Italic
        result.diffItem is StringComparisonItem && result.diffItem.drawItalic -> FontStyle.Italic
        else -> FontStyle.Normal
    })
}

private fun countText(
    metadataComparison: MetadataComparison,
    doNotCount: KeyedListDiffResultKind,
): Int? = when (val comparison = metadataComparison as? MetadataListComparison<*, *>) {
    null -> null
    else -> comparison.detailedResult.count { it.result != doNotCount }
}

private fun leftValueColumnSpec(
    localizedStrings: LocalizedStrings,
    leftCodeSystem: CodeSystem,
) = ColumnSpec.StringSearchableColumnSpec<MetadataComparison>(title = localizedStrings.leftValue, weight = 0.25f,
    instanceGetter = { this.diffItem.getRenderDisplay(leftCodeSystem) },
    mergeIf = { comparison ->
        comparison.result == MetadataComparisonResult.IDENTICAL
    }) { comparison ->
    TextForLeftRightValue(comparison,
        leftCodeSystem,
        localizedStrings,
        countText(comparison, KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT))
}

private fun rightValueColumnSpec(
    localizedStrings: LocalizedStrings,
    rightCodeSystem: CodeSystem,
) = ColumnSpec.StringSearchableColumnSpec<MetadataComparison>(
    title = localizedStrings.rightValue,
    weight = 0.25f,
    mergeIf = null,
    instanceGetter = { this.diffItem.getRenderDisplay(rightCodeSystem) }
) { comparison ->
    TextForLeftRightValue(comparison,
        rightCodeSystem,
        localizedStrings,
        countText(comparison, doNotCount = KeyedListDiffResultKind.KEY_ONLY_IN_LEFT))
}