package terminodiff.terminodiff.ui.panes.conceptmap.meta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.ui.panes.conceptmap.showJsonViewer

@Composable
fun ConceptMapMetaEditorContent(
    conceptMapState: ConceptMapState,
    localizedStrings: LocalizedStrings,
    isDarkTheme: Boolean,
    fhirContext: FhirContext,
) {
    val fhirJson by derivedStateOf {
        fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(conceptMapState.conceptMap.toFhir)
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text(localizedStrings.conceptMap, style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                showJsonViewer(fhirJson, isDarkTheme)
            },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Icon(Icons.Default.LocalFireDepartment, "JSON", tint = MaterialTheme.colorScheme.onPrimary)
                Text("JSON")
            }
        }
        ConceptMapMetaEditorForm(conceptMapState, localizedStrings)
    }
}

@Composable
private fun ConceptMapMetaEditorForm(conceptMapState: ConceptMapState, localizedStrings: LocalizedStrings) = Column {

}

//private fun EditText(
//    //todo
//) = TextField(
//    //todo
//)