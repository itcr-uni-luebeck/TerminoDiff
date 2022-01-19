package terminodiff.ui.panes.conceptdiff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import terminodiff.engine.concepts.ConceptDiff
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.AppIconResource
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.SelectableText
import terminodiff.ui.util.colorPairForConceptDiffResult

fun conceptDiffColumnSpecs(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    showPropertyDialog: (ConceptTableData) -> Unit,
) = listOf(codeColumnSpec(localizedStrings),
    displayColumnSpec(localizedStrings, diffColors),
    definitionColumnSpec(localizedStrings, diffColors),
    propertyDesignationColumnSpec(localizedStrings, diffColors, showPropertyDialog),
    overallComparisonColumnSpec(localizedStrings, diffColors))

private fun codeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<ConceptTableData>(title = localizedStrings.code, weight = 0.1f, content = {
        SelectableText(it.code, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    })

private fun displayColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors,
) = columnSpecForProperty(localizedStrings = localizedStrings,
    title = localizedStrings.display,
    diffColors = diffColors,
    labelToFind = localizedStrings.display,
    weight = 0.25f,
    stringValueResolver = FhirConceptDetails::display)

private fun definitionColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
) = columnSpecForProperty(localizedStrings = localizedStrings,
    title = localizedStrings.definition,
    diffColors = diffColors,
    labelToFind = localizedStrings.definition,
    weight = 0.25f,
    stringValueResolver = FhirConceptDetails::definition)

private fun propertyDesignationColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors,
    showPropertyDialog: (ConceptTableData) -> Unit,
) = ColumnSpec<ConceptTableData>(title = localizedStrings.propertiesDesignations,
    weight = 0.25f,
    tooltipText = null) { data ->
    when {
        data.isInBoth() -> {
            val propertyDifferenceCount =
                data.diff!!.propertyComparison.count { it.result != KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL }
            val designationDifferenceCount =
                data.diff.designationComparison.count { it.result != KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL }
            when {
                propertyDifferenceCount == 0 && designationDifferenceCount == 0 -> Button(onClick = {
                    showPropertyDialog(data)
                },
                    elevation = ButtonDefaults.elevation(4.dp),
                    colors = ButtonDefaults.buttonColors(diffColors.greenPair.first, diffColors.greenPair.second)) {
                    Text(text = localizedStrings.propertiesDesignationsCount(data.diff.propertyComparison.count(),
                        data.diff.designationComparison.count()), color = diffColors.yellowPair.second)
                }
                else -> {
                    Row {
                        Button(onClick = {
                            showPropertyDialog(data)
                        },
                            elevation = ButtonDefaults.elevation(4.dp),
                            colors = ButtonDefaults.buttonColors(diffColors.yellowPair.first,
                                diffColors.yellowPair.second)) {
                            Text(text = localizedStrings.propertiesDesignationsCountDelta.invoke(data.diff.propertyComparison.count() to propertyDifferenceCount,
                                data.diff.designationComparison.count() to designationDifferenceCount),
                                color = diffColors.yellowPair.second)
                        }
                    }
                }
            }
        }
        else -> {
            OutlinedButton(onClick = {
                showPropertyDialog(data)
            },
                elevation = ButtonDefaults.elevation(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiaryContainer)) {
                val text = when (data.isOnlyInLeft()) {
                    true -> data.leftDetails!!
                    else -> data.rightDetails!!
                }.let { details ->
                    localizedStrings.propertiesDesignationsCount.invoke(details.property.count(),
                        details.designation.count())
                }
                Text(text = text, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
    }
}

private fun overallComparisonColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors,
) = ColumnSpec<ConceptTableData>(
    title = localizedStrings.overallComparison,
    weight = 0.25f,
    tooltipText = null,
) { data ->
    when (data.isInBoth()) {
        true -> {
            val anyDifferent = data.diff!!.conceptComparison.any {
                it.result == ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT
            } || data.diff.propertyComparison.any { it.result != KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL }
                    || data.diff.designationComparison.any { it.result != KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL }
            val colors: Pair<Color, Color> = if (anyDifferent) diffColors.yellowPair else diffColors.greenPair
            val chipLabel: String =
                if (anyDifferent) localizedStrings.conceptDiffResults_.invoke(ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT)
                else localizedStrings.identical

            DiffChip(colorPair = colors, text = chipLabel, modifier = Modifier.fillMaxWidth(0.8f))
        }
        else -> {
            val chipLabel: String =
                if (data.isOnlyInLeft()) localizedStrings.onlyInLeft else localizedStrings.onlyInRight
            val onlyOneVersionIcon: ImageVector = when (data.isOnlyInLeft()) {
                true -> AppIconResource.loadXmlImageVector(AppIconResource.icLoadLeftFile)
                else -> AppIconResource.loadXmlImageVector(AppIconResource.icLoadRightFile)
            }
            DiffChip(modifier = Modifier.fillMaxWidth(0.8f),
                colorPair = diffColors.redPair,
                text = chipLabel,
                icon = onlyOneVersionIcon)
        }
    }
}

private fun columnSpecForProperty(
    localizedStrings: LocalizedStrings,
    title: String,
    diffColors: DiffColors,
    labelToFind: String,
    @Suppress("SameParameterValue") weight: Float,
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
            data.isInBoth() -> contentWithText(diff = data.diff!!,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                labelToFind = labelToFind,
                text = tooltipTextFun(data).invoke())
            singleConcept != null -> { // else
                SelectableText(text = stringValueResolver.invoke(singleConcept))
            }
        }
    }
}

private fun tooltipForConceptProperty(
    leftConcept: FhirConceptDetails?, rightConcept: FhirConceptDetails?, property: (FhirConceptDetails) -> String?,
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
    diff: ConceptDiff, localizedStrings: LocalizedStrings, diffColors: DiffColors, text: String?, labelToFind: String,
) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        ChipForConceptDiffResult(modifier = Modifier.padding(end = 2.dp),
            conceptComparison = diff.conceptComparison,
            labelToFind = labelToFind,
            localizedStrings = localizedStrings,
            diffColors = diffColors)
        SelectableText(text = text)
    }
}

@Composable
private fun ChipForConceptDiffResult(
    modifier: Modifier = Modifier,
    conceptComparison: List<ConceptDiffResult>,
    labelToFind: String,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
) {
    val result = conceptComparison.find { it.diffItem.label.invoke(localizedStrings) == labelToFind } ?: return
    val colorsForResult = colorPairForConceptDiffResult(result, diffColors)
    DiffChip(modifier = modifier,
        text = localizedStrings.conceptDiffResults_.invoke(result.result),
        backgroundColor = colorsForResult.first,
        textColor = colorsForResult.second,
        icon = null)
}