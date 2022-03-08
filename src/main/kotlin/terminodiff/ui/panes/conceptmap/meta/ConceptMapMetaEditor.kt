package terminodiff.terminodiff.ui.panes.conceptmap.meta

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import libraries.sahruday.carousel.*
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.engine.conceptmap.TerminodiffConceptMap
import terminodiff.terminodiff.ui.util.EditTextGroup
import terminodiff.terminodiff.ui.util.EditTextGroupSpec
import terminodiff.terminodiff.ui.util.EditTextSpec
import terminodiff.terminodiff.ui.util.isUrl
import terminodiff.ui.panes.conceptmap.showJsonViewer

@Composable
fun ConceptMapMetaEditorContent(
    conceptMapState: ConceptMapState,
    localizedStrings: LocalizedStrings,
    isDarkTheme: Boolean,
    fhirContext: FhirContext,
) {
    val fhirJson by derivedStateOf {
        fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(conceptMapState.conceptMap!!.toFhir)
    }
    val scrollState = rememberCarouselScrollState()
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
        Column(Modifier.weight(0.98f), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                showJsonViewer(fhirJson, isDarkTheme)
            },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary)) {
                Icon(Icons.Default.LocalFireDepartment, "JSON", tint = colorScheme.onPrimary)
                Text("JSON")
            }
            ConceptMapMetaEditorForm(conceptMapState, localizedStrings, scrollState)
        }
        Column(Modifier.fillMaxHeight().weight(0.02f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Carousel(state = scrollState,
                Modifier.fillMaxHeight(0.8f).width(2.dp),
                colors = CarouselDefaults.colors(thumbColor = colorScheme.onPrimaryContainer,
                    backgroundColor = colorScheme.onPrimaryContainer.copy(0.25f)))
        }

    }
}

@Composable
private fun ConceptMapMetaEditorForm(
    conceptMapState: ConceptMapState,
    localizedStrings: LocalizedStrings,
    scrollState: CarouselScrollState,
) = Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)) {
    getEditTextGroups().forEach { group ->
        EditTextGroup(group, localizedStrings, data = conceptMapState.conceptMap!!)
    }
}

private val mandatoryUrlValidator: (String) -> EditTextSpec.ValidationResult = { newValue ->
    when {
        newValue.isBlank() -> EditTextSpec.ValidationResult.INVALID
        newValue.isUrl() -> EditTextSpec.ValidationResult.VALID
        else -> EditTextSpec.ValidationResult.INVALID
    }
}

private val recommendedUrlValidator: (String) -> EditTextSpec.ValidationResult = { newValue ->
    when {
        newValue.isBlank() -> EditTextSpec.ValidationResult.WARN
        newValue.isUrl() -> EditTextSpec.ValidationResult.VALID
        else -> EditTextSpec.ValidationResult.INVALID
    }
}

fun getEditTextGroups(): List<EditTextGroupSpec<TerminodiffConceptMap>> = listOf(EditTextGroupSpec({ metadataDiff },
    listOf(
        EditTextSpec(title = { id }, valueState = { id }, validation = { input ->
            when (Regex("""[A-Za-z0-9\-.]{1,64}""").matches(input)) {
                true -> EditTextSpec.ValidationResult.VALID
                else -> EditTextSpec.ValidationResult.INVALID
            }
        }),
        EditTextSpec({ canonicalUrl }, { canonicalUrl }, validation = mandatoryUrlValidator),
        EditTextSpec({ version }, { version }) {
            when {
                it.isBlank() -> EditTextSpec.ValidationResult.INVALID
                Regex("""^(\d+\.\d+\.\d+(-[A-Za-z0-9]+)?)|\d{8}${'$'}""").matches(it) -> EditTextSpec.ValidationResult.VALID
                else -> EditTextSpec.ValidationResult.WARN
            }
        },
        EditTextSpec({ name }, { name }),
        EditTextSpec({ title }, { title }),
        EditTextSpec({ sourceValueSet }, { sourceValueSet }, validation = recommendedUrlValidator),
        EditTextSpec({ targetValueSet }, { targetValueSet }, validation = recommendedUrlValidator),
    )),
    EditTextGroupSpec({ group },
        listOf(EditTextSpec({ sourceUri }, { group.sourceUri }, validation = mandatoryUrlValidator),
            EditTextSpec({ sourceVersion }, { group.sourceVersion }),
            EditTextSpec({ targetUri }, { group.targetUri }, validation = mandatoryUrlValidator),
            EditTextSpec({ targetVersion }, { group.targetVersion }))))

