package terminodiff.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.metadata.MetadataDiff
import terminodiff.engine.metadata.MetadataDiff.MetadataDiffItemResult.*
import terminodiff.engine.metadata.MetadataDiffBuilder
import terminodiff.i18n.LocalizedStrings


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetadataDiffPanel(
    fhirContext: FhirContext,
    leftCs: CodeSystem,
    rightCs: CodeSystem,
    localizedStrings: LocalizedStrings,
) {
    val builder by remember { mutableStateOf(MetadataDiffBuilder(fhirContext, leftCs, rightCs)) }
    val diff by remember { mutableStateOf(builder.build()) }
    val listState = rememberLazyListState()
    LazyVerticalGrid(
        cells = GridCells.Adaptive(384.dp),
        state = listState,
        modifier = Modifier.padding(top = 8.dp),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            bottom = 16.dp
        ),
    ) {
        items(diff.diffResults) { res ->
            fun itemGetter(cs: CodeSystem): String? = when (res.diffItem) {
                is MetadataDiff.MetadataStringDiffItem -> res.diffItem.instanceGetter.invoke(cs)
                is MetadataDiff.MetadataListDiffItem -> res.diffItem.instanceGetter.invoke(cs)?.joinToString(",")
                else -> "not yet implemented"
            }
            MetadataItem(
                res.diffItem.label,
                itemGetter(leftCs),
                itemGetter(rightCs),
                res,
                localizedStrings = localizedStrings
            )
        }
    }
}

@Composable
fun MetadataItem(
    label: LocalizedStrings.() -> String,
    valueLeft: String?,
    valueRight: String?,
    comparisonResult: MetadataDiff.MetadataComparisonResult,
    localizedStrings: LocalizedStrings
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .defaultMinSize(minHeight = 220.dp),
        elevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(4.dp).fillMaxSize().border(BorderStroke(1.dp, Color.Red)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label.invoke(localizedStrings), style = MaterialTheme.typography.h6)
            //Column(modifier = Modifier().border(BorderStroke(1.dp, Color.Green)), verticalArrangement = Arrangement.Center) {
            Box(Modifier.fillMaxHeight().border(BorderStroke(1.dp, Color.Green))) {
                Column(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center).border(BorderStroke(1.dp, Color.Blue)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    readOnlyTextField(
                        value = valueLeft,
                        label = label.invoke(localizedStrings)
                    )
                    Chip(
                        text = localizedStrings.metadataDiffResults.invoke(comparisonResult.result),
                        color = colorForResult(comparisonResult)
                    )
                    readOnlyTextField(
                        value = valueRight,
                        label = label.invoke(localizedStrings)
                    )
                }
            }

        }
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
    label: String
) =
    OutlinedTextField(
        value = value ?: "null",
        modifier = modifier.fillMaxWidth(),
        onValueChange = {},
        readOnly = true,
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