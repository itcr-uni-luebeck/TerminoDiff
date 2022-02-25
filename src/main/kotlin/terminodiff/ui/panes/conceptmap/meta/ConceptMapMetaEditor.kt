package terminodiff.terminodiff.ui.panes.conceptmap.meta

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.engine.conceptmap.TerminodiffConceptMap
import terminodiff.terminodiff.ui.util.LabeledTextField
import terminodiff.ui.panes.conceptmap.showJsonViewer
import java.net.MalformedURLException
import java.net.URL

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
    val scrollState = rememberScrollState()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically) {
            Text(localizedStrings.conceptMap, style = typography.titleMedium)
            Button(onClick = {
                showJsonViewer(fhirJson, isDarkTheme)
            },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary)) {
                Icon(Icons.Default.LocalFireDepartment, "JSON", tint = colorScheme.onPrimary)
                Text("JSON")
            }
        }
        ConceptMapMetaEditorForm(conceptMapState, localizedStrings, scrollState)
    }
}

@Composable
private fun ConceptMapMetaEditorForm(
    conceptMapState: ConceptMapState,
    localizedStrings: LocalizedStrings,
    scrollState: ScrollState,
) = Column(
    Modifier.fillMaxSize().verticalScroll(scrollState),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)) {
    getEditTextGroups().forEach { group ->
        Card(Modifier.fillMaxWidth(0.9f), backgroundColor = colorScheme.secondaryContainer, elevation = 8.dp) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(group.title.invoke(localizedStrings),
                    style = typography.titleSmall,
                    color = colorScheme.onTertiaryContainer)
                group.specs.forEach { spec ->
                    val valueState = spec.valueState.invoke(conceptMapState.conceptMap)
                    val validation = spec.validation?.invoke(valueState.value ?: "")
                    val isError: Boolean
                    val trailingIconVector: ImageVector?
                    val trailingIconDescription: String?
                    when (validation) {
                        null -> {
                            isError = false
                            trailingIconVector = null
                            trailingIconDescription = null
                        }
                        true -> {
                            isError = false
                            trailingIconVector = null
                            trailingIconDescription = null
                        }
                        else -> {
                            isError = true
                            trailingIconVector = Icons.Default.Error
                            trailingIconDescription = localizedStrings.invalid
                        }
                    }
                    LabeledTextField(modifier = Modifier.fillMaxWidth(0.8f),
                        singleLine = spec.singleLine,
                        readOnly = spec.readOnly,
                        value = valueState.value ?: "",
                        onValueChange = { newValue ->
                            if (valueState is MutableState) valueState.value = newValue
                        },
                        labelText = spec.title.invoke(localizedStrings),
                        isError = isError,
                        trailingIconVector = trailingIconVector,
                        trailingIconDescription = trailingIconDescription,
                        trailingIconTint = colorScheme.error)
                }
            }

        }
    }

}

data class EditTextGroup(
    val title: LocalizedStrings.() -> String,
    val specs: List<EditTextSpec>,
)

fun getEditTextGroups(): List<EditTextGroup> = listOf(
    EditTextGroup({ metadataDiff }, listOf(
        EditTextSpec(title = { id },
            valueState = { id },
            validation = Regex("""[A-Za-z0-9\-.]{1,64}""")::matches),
        EditTextSpec({ canonicalUrl }, { canonicalUrl }) { newValue ->
            when {
                newValue.isBlank() -> false
                else -> newValue.isUrl()
            }
        },
        EditTextSpec({ version }, { version }),
        EditTextSpec({ name }, { name }),
        EditTextSpec({ title }, { title }),
        //EditTextSpec({ sourceValueSet }, { sourceValueSet }),
        //EditTextSpec({ targetValueSet }, { targetValueSet }),
    )),
    // TODO: 25/02/22 add another group for the `group` parameter
)

data class EditTextSpec(
    val title: LocalizedStrings.() -> String,
    val valueState: TerminodiffConceptMap.() -> State<String?>,
    val singleLine: Boolean = true,
    val readOnly: Boolean = false,
    val validation: ((String) -> Boolean)? = { it.isNotBlank() },
)

fun String.isUrl(): Boolean = try {
    URL(this).let { true }
} catch (e: MalformedURLException) {
    false
}