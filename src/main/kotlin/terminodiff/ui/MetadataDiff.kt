package terminodiff.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.metadata.MetadataDiff
import terminodiff.engine.metadata.MetadataDiffBuilder
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.theme.DiffColors
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.Carousel
import terminodiff.ui.util.CarouselDefaults
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.colorPairForDiffResult


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

    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(
            Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = localizedStrings.metadataDiff,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyVerticalGrid(
                    cells = GridCells.Adaptive(384.dp),
                    state = listState,
                    modifier = Modifier.padding(8.dp),
                    //modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 16.dp
                    ),
                ) {
                    items(diff.diffResults) { res ->
                        fun itemGetter(cs: CodeSystem): String? = when (res.diffItem) {
                            is MetadataDiff.MetadataStringDiffItem -> res.diffItem.instanceGetter.invoke(cs)
                            is MetadataDiff.MetadataListDiffItem -> res.diffItem.instanceGetter.invoke(cs)
                                ?.joinToString(",")
                            else -> "not yet implemented"
                        }

                        val diffColors = getDiffColors(useDarkTheme = useDarkTheme)
                        MetadataItem(
                            label = res.diffItem.label,
                            valueLeft = itemGetter(leftCs),
                            valueRight = itemGetter(rightCs),
                            comparisonResult = res,
                            localizedStrings = localizedStrings,
                            diffColors = diffColors
                        )
                    }
                }

                Carousel(
                    state = listState,
                    colors = CarouselDefaults.colors(thumbColor = MaterialTheme.colorScheme.onTertiaryContainer),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .fillMaxHeight(0.9f)
                )
            }
        }
    }
}

@Composable
fun MetadataItem(
    label: LocalizedStrings.() -> String,
    valueLeft: String?,
    valueRight: String?,
    comparisonResult: MetadataDiff.MetadataComparisonResult,
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .defaultMinSize(minHeight = 220.dp),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(4.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.invoke(localizedStrings),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(Modifier.fillMaxHeight()) {
                Column(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    readOnlyTextField(
                        value = valueLeft,
                        label = label.invoke(localizedStrings)
                    )
                    val (backgroundColor, foregroundColor) = colorPairForDiffResult(comparisonResult, diffColors)
                    DiffChip(
                        text = localizedStrings.`metadataDiffResults$`.invoke(comparisonResult.result),
                        backgroundColor = backgroundColor,
                        textColor = foregroundColor
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
        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colorScheme.secondary)
    )

