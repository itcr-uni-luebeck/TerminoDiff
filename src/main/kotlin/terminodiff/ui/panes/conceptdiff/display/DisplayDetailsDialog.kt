package terminodiff.terminodiff.ui.panes.conceptdiff.display

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.ui.util.TerminodiffDialog
import terminodiff.ui.panes.conceptdiff.ConceptTableData
import terminodiff.ui.theme.getDiffColors
import terminodiff.ui.util.DiffChip
import terminodiff.ui.util.colorPairForConceptDiffResult


@Composable
fun DisplayDetailsDialog(
    data: ConceptTableData,
    localizedStrings: LocalizedStrings,
    label: String,
    useDarkTheme: Boolean,
    onClose: () -> Unit,
    dataGetter: (FhirConceptDetails) -> String?,
) {
    val diffColors by derivedStateOf { getDiffColors(useDarkTheme = useDarkTheme) }
    TerminodiffDialog(
        title = label,
        windowPosition = WindowPosition(Alignment.Center),
        size = DpSize(512.dp, 400.dp),
        onCloseRequest = onClose
    ) {
        if (data.leftDetails != null) CardForDisplay(data.leftDetails, localizedStrings.leftValue, dataGetter)
        if (data.isInBoth()) {
            val result =
                data.diff!!.conceptComparison.find { it.diffItem.label.invoke(localizedStrings) == label } ?: return@TerminodiffDialog
            val (background, foreground) = colorPairForConceptDiffResult(result, diffColors)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                DiffChip(
                    Modifier.fillMaxWidth(0.5f).height(50.dp),
                    text = localizedStrings.conceptDiffResults_.invoke(result.result),
                    backgroundColor = background,
                    textColor = foreground)
            }
        }
        if (data.rightDetails != null) CardForDisplay(data.rightDetails, localizedStrings.rightValue, dataGetter)
    }
}

@Composable
private fun CardForDisplay(
    fhirConceptDetails: FhirConceptDetails,
    title: String,
    dataGetter: (FhirConceptDetails) -> String?,
) =
    Card(modifier = Modifier.padding(4.dp).fillMaxWidth(),
        backgroundColor = colorScheme.secondaryContainer,
        contentColor = colorScheme.onSecondaryContainer) {
        Column(modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSecondaryContainer)
            val text = dataGetter.invoke(fhirConceptDetails)
            TextField(modifier = Modifier.fillMaxWidth(0.9f),
                value = text ?: "",
                onValueChange = {},
                readOnly = true)
        }
    }