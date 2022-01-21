package terminodiff.terminodiff.ui.panes.conceptdiff.propertydesignation

import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.concepts.DesignationKey
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.engine.concepts.PropertyDiffResult
import terminodiff.engine.graph.FhirConceptDesignation
import terminodiff.engine.graph.FhirConceptProperty
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.formatCoding
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.chipForDiffResult
import terminodiff.ui.util.textForValue

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
    mergeIf = { it.result == KeyedListDiffResultKind.IDENTICAL }) {
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
        mergeIf = { it.result == KeyedListDiffResultKind.IDENTICAL }) {
        if (it.result != KeyedListDiffResultKind.KEY_ONLY_IN_LEFT) {
            textForValue(it.leftValue, limit = 10)
        }
    }

private fun rightPropertyValueColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<PropertyDiffResult>(title = localizedStrings.rightValue, weight = 0.4f) {
        if (it.result != KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT) textForValue(it.rightValue?.joinToString())
    }