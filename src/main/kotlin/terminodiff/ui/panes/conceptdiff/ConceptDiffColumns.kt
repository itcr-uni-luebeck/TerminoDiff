@file:OptIn(ExperimentalMaterial3Api::class)

package terminodiff.ui.panes.conceptdiff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onShowPropertyDialog: (ConceptTableData) -> Unit,
    onShowDisplayDetailsDialog: (ConceptTableData) -> Unit,
    onShowDefinitionDetailsDialog: (ConceptTableData) -> Unit,
    onShowGraph: (String) -> Unit,
) = listOf(codeColumnSpec(localizedStrings),
    graphColumnSpec(localizedStrings, onShowGraph),
    displayColumnSpec(localizedStrings, diffColors, onShowDisplayDetailsDialog),
    definitionColumnSpec(localizedStrings, diffColors, onShowDefinitionDetailsDialog),
    propertyDesignationColumnSpec(localizedStrings, diffColors, onShowPropertyDialog),
    overallComparisonColumnSpec(localizedStrings, diffColors))

private fun codeColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec.StringSearchableColumnSpec<ConceptTableData>(title = localizedStrings.code,
        weight = 0.1f,
        instanceGetter = { code })

@OptIn(ExperimentalMaterial3Api::class)
private fun graphColumnSpec(localizedStrings: LocalizedStrings, onShowGraph: (String) -> Unit) =
    ColumnSpec<ConceptTableData>(title = localizedStrings.graph, weight = 0.08f) { tableData ->
        CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
            IconButton(onClick = {
                onShowGraph.invoke(tableData.code)
            }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Hub, localizedStrings.graph)
            }
        }
    }

private fun displayColumnSpec(
    localizedStrings: LocalizedStrings, diffColors: DiffColors, showDisplayDetailsDialog: (ConceptTableData) -> Unit,
) = columnSpecForProperty(localizedStrings = localizedStrings,
    title = localizedStrings.display,
    diffColors = diffColors,
    labelToFind = localizedStrings.display,
    weight = 0.25f,
    stringValueResolver = FhirConceptDetails::display,
    onDetailClick = showDisplayDetailsDialog)

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
                    }, colors = ButtonDefaults.buttonColors(diffColors.greenPair.first, diffColors.greenPair.second)) {
                        Text(text = localizedStrings.propertiesDesignationsCount(data.diff.propertyComparison.count(),
                            data.diff.designationComparison.count()), color = diffColors.yellowPair.second)
                    }
                    else -> {
                        Row {
                            Button(onClick = {
                                showPropertyDialog(data)
                            },
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
//                    colors = ButtonDefaults.outlinedButtonColors(containerColor = colorScheme.tertiaryContainer,
//                        contentColor = colorScheme.onTertiaryContainer),
                    border = BorderStroke(1.dp, colorScheme.onTertiaryContainer)) {
                    val text = when (data.isOnlyInLeft()) {
                        true -> data.leftDetails!!
                        else -> data.rightDetails!!
                    }.let { details ->
                        localizedStrings.propertiesDesignationsCount.invoke(details.property.count(),
                            details.designation.count())
                    }
                    Text(text = text, color = colorScheme.onTertiaryContainer)
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
    when (val result = data.overallComparison()) {
        ConceptTableData.OverallComparison.IDENTICAL, ConceptTableData.OverallComparison.DIFFERENT -> {
            val anyDifferent = result == ConceptTableData.OverallComparison.DIFFERENT
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
): ColumnSpec.StringSearchableColumnSpec<ConceptTableData> {
    val tooltipTextFun: (ConceptTableData) -> String? =
        { data -> tooltipForConceptProperty(data.leftDetails, data.rightDetails, stringValueResolver) }
    return ColumnSpec.StringSearchableColumnSpec(
        title = title,
        weight = weight,
        instanceGetter = tooltipTextFun,
        tooltipText = tooltipTextFun,
    ) { data ->
        val singleConcept = when {
            data.isOnlyInLeft() -> data.leftDetails!!
            data.isOnlyInRight() -> data.rightDetails!!
            else -> null
        }
        when {
            data.isInBoth() -> contentWithText(conceptData = data,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                labelToFind = labelToFind,
                text = tooltipTextFun(data),
                onDetailClick = onDetailClick)
            singleConcept != null -> { // else
                val text = stringValueResolver.invoke(singleConcept)
                val textDisplay: @Composable (Color) -> Unit = { color ->
                    NullableText(text = text,
                        color = color,
                        style = typography.labelMedium,
                        overflow = TextOverflow.Clip)
                }
                when (text != null) {
                    true -> Row(Modifier.padding(2.dp)) {
                        OutlinedButton(modifier = Modifier.padding(4.dp),
                            onClick = { onDetailClick?.invoke(data) },
//                            colors = ButtonDefaults.outlinedButtonColors(containerColor = colorScheme.tertiaryContainer,
//                                contentColor = colorScheme.onTertiaryContainer),
                            border = BorderStroke(1.dp, colorScheme.onTertiaryContainer)) {
                            textDisplay.invoke(colorScheme.onTertiaryContainer)
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
        ChipForConceptDiffResult(modifier = Modifier.padding(end = 2.dp),
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
            contentPadding = PaddingValues(vertical = 1.dp, horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(background, foreground)) {
            Text(text = localizedStrings.conceptDiffResults_.invoke(result.result),
                style = typography.bodyMedium,
                color = foreground)
        }
    }

}