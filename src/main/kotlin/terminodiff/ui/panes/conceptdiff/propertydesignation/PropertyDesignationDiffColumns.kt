package terminodiff.terminodiff.ui.panes.conceptdiff.propertydesignation

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.concepts.DesignationKey
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.concepts.PropertyDiffResult
import terminodiff.engine.graph.FhirConceptDesignation
import terminodiff.engine.graph.FhirConceptProperty
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.formatCoding
import terminodiff.ui.AppIconResource
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.SelectableText

typealias DesignationDiffResult = KeyedListDiffResult<DesignationKey, String>

fun columnSpecsDifferentProperties(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
): List<ColumnSpec<PropertyDiffResult>> = listOf(propertyCodeColumnSpec(localizedStrings) { it.key },
    propertyComparisonColumnSpec(localizedStrings, diffColors),
    propertyTypeColumnSpec(localizedStrings) { it.propertyType },
    leftPropertyValueColumnSpec(localizedStrings),
    rightPropertyValueColumnSpec(localizedStrings))

fun columnSpecsIdenticalProperties(
    localizedStrings: LocalizedStrings,
): List<ColumnSpec<FhirConceptProperty>> = listOf(propertyCodeColumnSpec(localizedStrings) { it.propertyCode },
    propertyTypeColumnSpec(localizedStrings) { it.type },
    propertyValueColumnSpec(localizedStrings))

fun columnSpecsDifferentDesignations(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
): List<ColumnSpec<DesignationDiffResult>> = listOf(designationLanguageColumnSpec(localizedStrings) { it.key.first },
    designationUseColumnSpec(localizedStrings) { it.key.second },
    designationComparisonColumnSpec(localizedStrings, diffColors),
    leftDesignationValueColumnSpec(localizedStrings),
    rightDesignationValueColumnSpec(localizedStrings))

fun columnSpecsIdenticalDesignations(
    localizedStrings: LocalizedStrings,
): List<ColumnSpec<FhirConceptDesignation>> = listOf(designationLanguageColumnSpec(localizedStrings) { it.language },
    designationUseColumnSpec(localizedStrings) { it.use?.let(::formatCoding) },
    designationValueColumnSpec(localizedStrings))

private fun <T> designationLanguageColumnSpec(localizedStrings: LocalizedStrings, languageGetter: (T) -> String?) =
    ColumnSpec<T>(localizedStrings.language, weight = 0.2f) {
        textForValue(languageGetter.invoke(it))
    }

private fun <T> designationUseColumnSpec(localizedStrings: LocalizedStrings, useGetter: (T) -> String?) =
    ColumnSpec<T>(localizedStrings.useContext, weight = 0.2f) {
        textForValue(useGetter.invoke(it))
    }

private fun designationValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<FhirConceptDesignation>(localizedStrings.value, weight = 0.33f) {
        textForValue(it.value)
    }

private fun designationComparisonColumnSpec(localizedStrings: LocalizedStrings, diffColors: DiffColors) =
    ColumnSpec<DesignationDiffResult>(localizedStrings.comparison, weight = 0.2f) { diffData ->
        chipForDiffResult(localizedStrings, diffColors, diffData.result)
    }

private fun leftDesignationValueColumnSpec(localizedStrings: LocalizedStrings) = ColumnSpec<DesignationDiffResult>(
    localizedStrings.leftValue,
    weight = 0.2f,
    mergeIf = { it.result == KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL }) {
    textForValue(it.leftValue)
}

private fun rightDesignationValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<DesignationDiffResult>(localizedStrings.rightValue, weight = 0.2f) {
        textForValue(it.rightValue)
    }


fun <T> propertyCodeColumnSpec(localizedStrings: LocalizedStrings, codeGetter: (T) -> String) =
    ColumnSpec<T>(localizedStrings.code, 0.2f) {
        textForValue(codeGetter.invoke(it))
    }

private fun <T> propertyTypeColumnSpec(localizedStrings: LocalizedStrings, typeGetter: (T) -> CodeSystem.PropertyType) =
    ColumnSpec<T>(localizedStrings.propertyType, weight = 0.2f) {
        textForValue(typeGetter.invoke(it).name)
    }

private fun propertyValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<FhirConceptProperty>(localizedStrings.value, weight = 0.6f) {
        textForValue(it.value)
    }

private fun propertyComparisonColumnSpec(localizedStrings: LocalizedStrings, diffColors: DiffColors) =
    ColumnSpec<PropertyDiffResult>(localizedStrings.comparison, weight = 0.2f) { diffData ->
        chipForDiffResult(localizedStrings, diffColors, diffData.result)
    }

private fun leftPropertyValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<PropertyDiffResult>(title = localizedStrings.leftValue,
        weight = 0.4f,
        mergeIf = { it.result == KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL }) {
        if (it.result != KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_LEFT) {
            textForValue(it.leftValue, limit = 10)
        }
    }

private fun rightPropertyValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<PropertyDiffResult>(title = localizedStrings.rightValue, weight = 0.4f) {
        if (it.result != KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT) textForValue(it.rightValue?.joinToString())
    }

@Composable
private fun chipForDiffResult(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    result: KeyedListDiffResult.KeyedListDiffResultKind,
) {
    val colorPair: Pair<Color, Color>
    val chipText: String
    var chipIcon: ImageVector? = null
    when (result) {
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

@Composable
private fun textForValue(
    value: Any?,
    limit: Int = 3,
) = SelectableText(text = when (value) {
    is List<*> -> value.joinToString(limit = limit)
    else -> value?.toString()
}, color = LocalContentColor.current)