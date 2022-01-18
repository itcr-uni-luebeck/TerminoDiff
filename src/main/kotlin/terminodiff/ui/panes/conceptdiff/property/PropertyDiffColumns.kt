package terminodiff.terminodiff.ui.panes.conceptdiff.property

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.graph.FhirConceptProperty
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.AppIconResource
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.SelectableText

typealias PropDiffData = KeyedListDiffResult<String, String>

fun columnSpecsDifferentProperties(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
): List<ColumnSpec<PropDiffData>> = listOf(propertyCodeColumnSpec(localizedStrings) { it.key },
    propertyComparisonColumnSpec(localizedStrings, diffColors),
    leftValueColumnSpec(localizedStrings),
    rightValueColumnSpec(localizedStrings))

fun columnSpecsIdenticalProperties(
    localizedStrings: LocalizedStrings,
): List<ColumnSpec<FhirConceptProperty>> = listOf(propertyCodeColumnSpec(localizedStrings) { it.propertyCode },
    propertyTypeColumnSpec(localizedStrings),
    propertyValueColumnSpec(localizedStrings))

private fun <T> propertyCodeColumnSpec(localizedStrings: LocalizedStrings, codeGetter: (T) -> String) =
    ColumnSpec<T>(localizedStrings.code, 0.2f) {
        SelectableText(codeGetter.invoke(it), color = MaterialTheme.colorScheme.onPrimaryContainer)
    }

private fun propertyTypeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<FhirConceptProperty>(localizedStrings.property, weight = 0.2f) {
        textForLeftRightValue(it.type.name)
        // TODO: 18/01/22 localized string
    }

private fun propertyValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<FhirConceptProperty>(localizedStrings.rightValue, weight = 0.6f) {
        textForLeftRightValue(it.value)
        // TODO: 18/01/22 localized string
    }

private fun propertyComparisonColumnSpec(localizedStrings: LocalizedStrings, diffColors: DiffColors) =
    ColumnSpec<PropDiffData>(localizedStrings.comparison, weight = 0.2f) { diffData ->
        val colorPair: Pair<Color, Color>
        val chipText: String
        var chipIcon: ImageVector? = null
        when (diffData.kind) {
            KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL -> {
                colorPair = diffColors.greenPair
                chipText = localizedStrings.identical
            }
            KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_LEFT -> {
                colorPair = diffColors.redPair
                chipText = localizedStrings.onlyInLeft
                chipIcon = AppIconResource.loadXmlImageVector(AppIconResource.icLoadLeftFile)
            }
            KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT -> {
                colorPair = diffColors.redPair
                chipText = localizedStrings.onlyInRight
                chipIcon = AppIconResource.loadXmlImageVector(AppIconResource.icLoadRightFile)
            }
            else -> {
                colorPair = diffColors.yellowPair
                chipText = localizedStrings.differentValue
            }
        }
        DiffChip(text = chipText, colorPair = colorPair, icon = chipIcon)
    }

private fun leftValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<PropDiffData>(title = localizedStrings.leftValue,
        weight = 0.4f,
        mergeIf = { it.kind == KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL }) {
        if (it.kind != KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_LEFT) {
            textForLeftRightValue(it.leftValue?.joinToString())
        }
    }

private fun rightValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<PropDiffData>(title = localizedStrings.rightValue, weight = 0.4f) {
        if (it.kind != KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT) textForLeftRightValue(it.rightValue?.joinToString())
    }

@Composable
private fun textForLeftRightValue(
    value: Any?,
) {
    val text = value?.toString()
    SelectableText(text = text ?: "null", fontStyle = when (text) {
        null -> FontStyle.Italic
        else -> FontStyle.Normal
    }, color = MaterialTheme.colorScheme.onPrimaryContainer)
}