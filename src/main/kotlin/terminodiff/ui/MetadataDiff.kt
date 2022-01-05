package terminodiff.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
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
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.Identifier
import terminodiff.engine.metadata.MetadataDiff
import terminodiff.engine.metadata.MetadataDiffBuilder
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetadataDiffPanel(
    fhirContext: FhirContext,
    leftCs: CodeSystem,
    rightCs: CodeSystem,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean
) {
    val builder by remember { mutableStateOf(MetadataDiffBuilder(fhirContext, leftCs, rightCs)) }
    val diff by remember { mutableStateOf(builder.build()) }
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
                diff = diff,
                localizedStrings = localizedStrings,
                diffColors = diffColors,
                leftCodeSystem = leftCs,
                rightCodeSystem = rightCs
            )
        }
    }
}

@Composable
fun MetadataDiffTable(
    lazyListState: LazyListState,
    diff: MetadataDiff,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    leftCodeSystem: CodeSystem,
    rightCodeSystem: CodeSystem
) {

    val columnSpecs = listOf(
        ColumnSpec.propertyColumnSpec(localizedStrings),
        ColumnSpec.resultColumnSpec(localizedStrings, diffColors),
        ColumnSpec.leftValueColumnSpec(localizedStrings, leftCodeSystem),
        ColumnSpec.rightValueColumnSpec(localizedStrings, rightCodeSystem),
        ColumnSpec.propertyColumnSpec(localizedStrings)
    )
    LazyTable(
        columnSpecs = columnSpecs,
        lazyListState = lazyListState,
        tableData = diff.diffResults,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        keyFun = { it.diffItem.label.invoke(localizedStrings) }
    )

}

private fun ColumnSpec.Companion.propertyColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<MetadataDiff.MetadataComparisonResult>(
        title = localizedStrings.property,
        weight = 0.1f
    ) {
        SelectableText(
            it.diffItem.label.invoke(localizedStrings),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }

private fun ColumnSpec.Companion.resultColumnSpec(localizedStrings: LocalizedStrings, diffColors: DiffColors) =
    ColumnSpec<MetadataDiff.MetadataComparisonResult>(title = localizedStrings.comparison, weight = 0.2f) { result ->
        val (backgroundColor, foregroundColor) = colorPairForDiffResult(result, diffColors)
        DiffChip(
            text = localizedStrings.metadataDiffResults_.invoke(result.result),
            backgroundColor = backgroundColor,
            textColor = foregroundColor,
            icon = null
        )
    }

@Composable
private fun TextForLeftRightValue(
    result: MetadataDiff.MetadataComparisonResult,
    codeSystem: CodeSystem,
    localizedStrings: LocalizedStrings
) {
    val text: String? = result.diffItem.instanceGetter.invoke(codeSystem)?.let { v ->
        when (v) {
            is String -> v
            is List<*> -> String.format("%s: [ %s ]", localizedStrings.numberItems_.invoke(v.size), v.joinToString(";") { it.toString() })
            else -> v.toString()
        }
    }
    SelectableText(text = text ?: "null", fontStyle = if (text == null) FontStyle.Italic else FontStyle.Normal)
}

private fun ColumnSpec.Companion.leftValueColumnSpec(
    localizedStrings: LocalizedStrings,
    leftCodeSystem: CodeSystem,
) =
    ColumnSpec<MetadataDiff.MetadataComparisonResult>(
        title = localizedStrings.leftValue,
        weight = 0.25f,
        mergeIf = { res ->
            res.result == MetadataDiff.MetadataDiffItemResult.IDENTICAL
        }) {
        TextForLeftRightValue(it, leftCodeSystem, localizedStrings)
    }

private fun ColumnSpec.Companion.rightValueColumnSpec(
    localizedStrings: LocalizedStrings,
    rightCodeSystem: CodeSystem,
) =
    ColumnSpec<MetadataDiff.MetadataComparisonResult>(
        title = localizedStrings.rightValue,
        weight = 0.25f
    ) {
        TextForLeftRightValue(it, rightCodeSystem, localizedStrings)
    }

@Composable
fun readOnlyTextField(
    modifier: Modifier = Modifier,
    value: String?
) =
    OutlinedTextField(
        value = value ?: "null",
        modifier = modifier.fillMaxWidth(),
        onValueChange = {},
        readOnly = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colorScheme.secondary)
    )

