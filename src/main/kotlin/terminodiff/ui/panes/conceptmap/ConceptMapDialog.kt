package terminodiff.terminodiff.ui.panes.conceptmap

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.uhn.fhir.context.FhirContext
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapSuggester
import terminodiff.terminodiff.ui.util.TerminodiffDialog

@Composable
fun ConceptMapDialog(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    fhirContext: FhirContext,
    onCloseRequest: () -> Unit,
) {
    val suggestedMap by remember { mutableStateOf(ConceptMapSuggester(diffDataContainer)) }
    val fhirJsonScrollState = rememberScrollableState { it }
    TerminodiffDialog(
        localizedStrings.conceptMap,
        onCloseRequest = onCloseRequest,
    ) {
        val fhir = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(
            suggestedMap.conceptMap.toFhir
        )
        JSONDisplay(fhir, fhirJsonScrollState)
    }
}