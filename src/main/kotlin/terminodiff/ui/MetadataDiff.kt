package terminodiff.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.metadata.MetadataDiff
import terminodiff.engine.metadata.MetadataDiff.MetadataDiffItemResult.*
import terminodiff.engine.metadata.MetadataDiffBuilder
import terminodiff.i18n.LocalizedStrings


@Composable
fun metadataDiffPanel(
    fhirContext: FhirContext,
    leftCs: CodeSystem,
    rightCs: CodeSystem,
    localizedStrings: LocalizedStrings
) {
    val builder by remember { mutableStateOf(MetadataDiffBuilder(fhirContext, leftCs, rightCs)) }
    val diff by remember { mutableStateOf(builder.build()) }
    LazyColumn {
        diff.diffResults.forEach { res ->
            item {
                fun itemGetter(cs: CodeSystem): String? = when (res.diffItem) {
                    is MetadataDiff.MetadataStringDiffItem -> res.diffItem.instanceGetter.invoke(cs)
                    is MetadataDiff.MetadataListDiffItem -> res.diffItem.instanceGetter.invoke(cs)?.joinToString(",")
                    else -> "not yet implemented"
                }
                metadataItem(
                    res.diffItem.label,
                    itemGetter(leftCs),
                    itemGetter(rightCs),
                    res,
                    localizedStrings = localizedStrings
                )
            }
        }
    }
}

@Composable
fun metadataItem(
    label: LocalizedStrings.() -> String,
    valueLeft: String?,
    valueRight: String?,
    comparisonResult: MetadataDiff.MetadataComparisonResult,
    localizedStrings: LocalizedStrings
) {
    Row(Modifier.padding(horizontal = 5.dp, vertical = 2.dp).fillMaxWidth()) {
        readOnlyTextField(
            modifier = Modifier.weight(0.43f),
            value = valueLeft,
            label = label.invoke(localizedStrings),
            textAlign = TextAlign.Right
        )
        Chip(
            //Modifier.weight(0.05f),
            text = localizedStrings.metadataDiffResults.invoke(comparisonResult.result),
            color = colorForResult(comparisonResult)
        )
        readOnlyTextField(
            modifier = Modifier.weight(0.43f),
            value = valueRight,
            label = label.invoke(localizedStrings)
        )
    }
}

fun colorForResult(comparisonResult: MetadataDiff.MetadataComparisonResult) = when (comparisonResult.result) {
    DIFFERENT_TEXT, DIFFERENT, DIFFERENT_COUNT -> if (comparisonResult.expected) Color.Yellow else Color.Red
    else -> Color.Green
}

@Composable
fun readOnlyTextField(
    modifier: Modifier = Modifier,
    value: String?,
    label: String,
    textAlign: TextAlign = TextAlign.Left
) =
    OutlinedTextField(
        value = value ?: "null",
        modifier = modifier,
        onValueChange = {},
        readOnly = true,
        textStyle = TextStyle(textAlign = textAlign),
        label = { Text(label, textAlign = textAlign) }
    )

@Preview
@Composable
fun Chip(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
) {
    Surface(
        modifier = modifier.padding(4.dp),
        elevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = color
    ) {
        Row {
            Text(
                text = text,
                style = MaterialTheme.typography.body2,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}