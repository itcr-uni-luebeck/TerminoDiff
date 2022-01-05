package terminodiff.ui.panes.conceptdiff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.AppIconResource
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.SelectableText
import terminodiff.ui.util.colorPairForConceptDiffResult

fun ColumnSpec.Companion.codeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<ConceptTableData>(title = localizedStrings.code, weight = 0.1f, content = {
        SelectableText(it.code, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    })

fun ColumnSpec.Companion.displayColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors
) = columnSpecForProperty(
    localizedStrings = localizedStrings,
    title = localizedStrings.display,
    diffColors = diffColors,
    labelToFind = localizedStrings.display,
    weight = 0.25f,
    stringValueResolver = FhirConceptDetails::display
)

fun ColumnSpec.Companion.definitionColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
) = ColumnSpec.columnSpecForProperty(
    localizedStrings = localizedStrings,
    title = localizedStrings.definition,
    diffColors = diffColors,
    labelToFind = localizedStrings.definition,
    weight = 0.25f,
    stringValueResolver = FhirConceptDetails::definition
)

fun ColumnSpec.Companion.propertyColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors
) = ColumnSpec<ConceptTableData>(
    title = localizedStrings.property, weight = 0.25f, tooltipText = null
) { data ->
    when {
        data.isInBoth() -> {
            when {
                data.diff!!.propertyComparison.none() -> DiffChip(
                    text = localizedStrings.identical, colorPair = diffColors.greenPair
                )
                else -> {
                    // TODO: 04/01/22
                    Row {
                        Button(
                            onClick = {},
                            elevation = ButtonDefaults.elevation(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                diffColors.yellowPair.first, diffColors.yellowPair.second
                            )
                        ) {
                            Text(localizedStrings.numberDifferent_.invoke(data.diff.propertyComparison.size))
                        }
                    }
                }
            }
        }
        else -> {
            // TODO: 05/01/22
        }
    }
}

fun ColumnSpec.Companion.overallComparisonColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors
) = ColumnSpec<ConceptTableData>(
    title = localizedStrings.overallComparison,
    weight = 0.1f,
    tooltipText = null,
) { data ->
    when (data.isInBoth()) {
        true -> {
            val anyDifferent = data.diff!!.conceptComparison.any {
                it.result == ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT
            }
            val colors: Pair<Color, Color> = if (anyDifferent) diffColors.yellowPair else diffColors.greenPair
            val chipLabel: String =
                if (anyDifferent) localizedStrings.conceptDiffResults_.invoke(ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT)
                else localizedStrings.identical

            DiffChip(
                colorPair = colors, text = chipLabel, modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
        else -> {
            val chipLabel: String =
                if (data.isOnlyInLeft()) localizedStrings.onlyInLeft else localizedStrings.onlyInRight
            val onlyOneVersionIcon: ImageVector = when (data.isOnlyInLeft()) {
                true -> AppIconResource.loadXmlImageVector(AppIconResource.icLoadLeftFile)
                else -> AppIconResource.loadXmlImageVector(AppIconResource.icLoadRightFile)
            }
            DiffChip(
                modifier = Modifier.fillMaxWidth(0.8f),
                colorPair = diffColors.redPair,
                text = chipLabel,
                icon = onlyOneVersionIcon
            )
        }
    }
}

private fun ColumnSpec.Companion.columnSpecForProperty(
    localizedStrings: LocalizedStrings,
    title: String,
    diffColors: DiffColors,
    labelToFind: String,
    weight: Float,
    stringValueResolver: (FhirConceptDetails) -> String?,
): ColumnSpec<ConceptTableData> {
    val tooltipTextFun: (ConceptTableData) -> () -> String? =
        { data -> tooltipForConceptProperty(data.leftDetails, data.rightDetails, stringValueResolver) }
    return ColumnSpec(
        title = title,
        weight = weight,
        tooltipText = tooltipTextFun,
    ) { data ->
        val singleConcept = when {
            data.isOnlyInLeft() -> data.leftDetails!!
            data.isOnlyInRight() -> data.rightDetails!!
            else -> null
        }
        when {
            data.isInBoth() -> contentWithText(
                diff = data.diff!!,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                labelToFind = labelToFind,
                text = tooltipTextFun(data).invoke()
            )
            singleConcept != null -> { // else
                val text = stringValueResolver.invoke(singleConcept)
                SelectableText(
                    text = text ?: "null", fontStyle = if (text == null) FontStyle.Italic else FontStyle.Normal
                )
            }
        }
    }
}

private fun tooltipForConceptProperty(
    leftConcept: FhirConceptDetails?, rightConcept: FhirConceptDetails?, property: (FhirConceptDetails) -> String?
): () -> String? = {
    val leftValue = leftConcept?.let(property)
    val rightValue = rightConcept?.let(property)
    when {
        leftValue == null && rightValue == null -> null
        leftValue != null && rightValue == null -> leftValue.toString()
        leftValue == null && rightValue != null -> rightValue.toString()
        leftValue == rightValue -> leftValue.toString()
        else -> "'$leftValue' vs. '$rightValue'"
    }
}

@Composable
private fun contentWithText(
    diff: ConceptDiff, localizedStrings: LocalizedStrings, diffColors: DiffColors, text: String?, labelToFind: String
) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        ChipForConceptDiffResult(
            modifier = Modifier.padding(end = 2.dp),
            conceptComparison = diff.conceptComparison,
            labelToFind = labelToFind,
            localizedStrings = localizedStrings,
            diffColors = diffColors
        )
        SelectableText(
            text = text ?: "null", fontStyle = if (text == null) FontStyle.Italic else FontStyle.Normal
        )
    }
}

@Composable
private fun ChipForConceptDiffResult(
    modifier: Modifier = Modifier,
    conceptComparison: List<ConceptDiffResult>,
    labelToFind: String,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors
) {
    val result = conceptComparison.find { it.diffItem.label.invoke(localizedStrings) == labelToFind } ?: return
    val colorsForResult = colorPairForConceptDiffResult(result, diffColors)
    DiffChip(
        modifier = modifier,
        text = localizedStrings.conceptDiffResults_.invoke(result.result),
        backgroundColor = colorsForResult.first,
        textColor = colorsForResult.second,
        icon = null
    )
}