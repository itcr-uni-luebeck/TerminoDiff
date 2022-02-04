package terminodiff.ui.panes.conceptdiff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.AppIconResource
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.*

fun conceptDiffColumnSpecs(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    showPropertyDialog: (ConceptTableData) -> Unit,
    showDisplayDetailsDialog: (ConceptTableData) -> Unit,
    showDefinitionDetailsDialog: (ConceptTableData) -> Unit,
) = listOf(codeColumnSpec(localizedStrings),
    displayColumnSpec(localizedStrings, diffColors, showDisplayDetailsDialog),
    definitionColumnSpec(localizedStrings, diffColors, showDefinitionDetailsDialog),
    propertyDesignationColumnSpec(localizedStrings, diffColors, showPropertyDialog),
    overallComparisonColumnSpec(localizedStrings, diffColors))

private fun codeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<ConceptTableData>(title = localizedStrings.code, weight = 0.1f, content = {
        SelectableText(it.code, style = typography.bodyLarge, textAlign = TextAlign.Center)
    })

private fun displayColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors, showDisplayDetailsDialog: (ConceptTableData) -> Unit,
) = columnSpecForProperty(localizedStrings = localizedStrings,
    title = localizedStrings.display,
    diffColors = diffColors,
    labelToFind = localizedStrings.display,
    weight = 0.25f,
    stringValueResolver = FhirConceptDetails::display,
    onDetailClick = showDisplayDetailsDialog
)

private fun definitionColumnSpec(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    showDefinitionDetailsDialog: (ConceptTableData) -> Unit,
) = columnSpecForProperty(localizedStrings = localizedStrings,
    title = localizedStrings.definition,
    diffColors = diffColors,
    labelToFind = localizedStrings.definition,
    weight = 0.25f,
    stringValueResolver = FhirConceptDetails::definition,
    onDetailClick = showDefinitionDetailsDialog)

private fun propertyDesignationColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors,
    showPropertyDialog: (ConceptTableData) -> Unit,
) = ColumnSpec<ConceptTableData>(title = localizedStrings.propertiesDesignations,
    weight = 0.25f,
    tooltipText = { localizedStrings.clickForDetails },
    content = { data ->
        when {
            data.isInBoth() -> {
                val propertyDifferenceCount =
                    data.diff!!.propertyComparison.count { it.result != KeyedListDiffResultKind.IDENTICAL }
                val designationDifferenceCount =
                    data.diff.designationComparison.count { it.result != KeyedListDiffResultKind.IDENTICAL }
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
    })

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
            } || data.diff.propertyComparison.any { it.result != KeyedListDiffResultKind.IDENTICAL } || data.diff.designationComparison.any { it.result != KeyedListDiffResultKind.IDENTICAL }
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
    onDetailClick: ((ConceptTableData) -> Unit)? = null,
): ColumnSpec<ConceptTableData> {
    val tooltipTextFun: (ConceptTableData) -> String? =
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
                conceptData = data,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                labelToFind = labelToFind,
                text = tooltipTextFun(data),
                onDetailClick = onDetailClick)
            singleConcept != null -> { // else
                val text = stringValueResolver.invoke(singleConcept)
                val textDisplay: @Composable (Color) -> Unit = { color ->
                    NullableText(
                        text = text,
                        color = color,
                        style = typography.labelMedium,
                        overflow = TextOverflow.Clip)
                }
                when (text != null) {
                    true -> Row(Modifier.padding(2.dp)) {
                        OutlinedButton(
                            modifier = Modifier.padding(2.dp),
                            onClick = { onDetailClick?.invoke(data) },
                            elevation = ButtonDefaults.elevation(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiaryContainer)
                        ) {
                            textDisplay.invoke(MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                    else -> Row(modifier = Modifier.padding(2.dp).fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        textDisplay(LocalContentColor.current)
                    }
                }

            }
        }
    }
}

private fun tooltipForConceptProperty(
    leftConcept: FhirConceptDetails?, rightConcept: FhirConceptDetails?, property: (FhirConceptDetails) -> String?,
): String? {
    val leftValue = leftConcept?.let(property)
    val rightValue = rightConcept?.let(property)
    return when {
        leftValue == null && rightValue == null -> null
        leftValue != null && rightValue == null -> leftValue.toString()
        leftValue == null && rightValue != null -> rightValue.toString()
        leftValue == rightValue -> leftValue.toString()
        else -> "'$leftValue' vs. '$rightValue'"
    }
}

@Composable
private fun contentWithText(
    conceptData: ConceptTableData,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    text: String?,
    labelToFind: String,
    onDetailClick: ((ConceptTableData) -> Unit)? = null,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically) {
        ChipForConceptDiffResult(
            modifier = Modifier.padding(end = 2.dp),
            conceptData = conceptData,
            labelToFind = labelToFind,
            localizedStrings = localizedStrings,
            diffColors = diffColors,
            onDetailClick = onDetailClick)
        SelectableText(text = text, style = typography.labelMedium)
    }
}

@Composable
private fun ChipForConceptDiffResult(
    modifier: Modifier = Modifier,
    conceptData: ConceptTableData,
    labelToFind: String,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    onDetailClick: ((ConceptTableData) -> Unit)?,
) {
    val result = conceptData.diff!!.conceptComparison.find { it.diffItem.label.invoke(localizedStrings) == labelToFind }
        ?: return
    val (background, foreground) = colorPairForConceptDiffResult(result, diffColors)
    when (onDetailClick) {
        null -> DiffChip(modifier = modifier,
            text = localizedStrings.conceptDiffResults_.invoke(result.result),
            backgroundColor = background,
            textColor = foreground,
            icon = null)
        else -> Button(onClick = {
            onDetailClick(conceptData)
        },
            elevation = ButtonDefaults.elevation(4.dp),
            contentPadding = PaddingValues(1.dp),
            colors = ButtonDefaults.buttonColors(background, foreground)) {
            Text(text = localizedStrings.conceptDiffResults_.invoke(result.result),
                style = typography.bodyMedium,
                color = foreground)
        }
    }

}