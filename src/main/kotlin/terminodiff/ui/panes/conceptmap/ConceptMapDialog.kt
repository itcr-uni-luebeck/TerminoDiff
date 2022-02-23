package terminodiff.terminodiff.ui.panes.conceptmap

import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import ca.uhn.fhir.context.FhirContext
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.ui.panes.conceptmap.mapping.ConceptMappingEditorContent
import terminodiff.terminodiff.ui.panes.conceptmap.meta.ConceptMapMetaEditorContent

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConceptMapDialog(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    fhirContext: FhirContext,
    isDarkTheme: Boolean,
    onCloseRequest: () -> Unit,
) {
    val conceptMapState by remember { mutableStateOf(ConceptMapState(diffDataContainer)) }
    val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
    Window(
        title = localizedStrings.conceptMap,
        onCloseRequest = onCloseRequest,
    ) {
        BackdropScaffold(
            scaffoldState = scaffoldState,
            appBar = {},
            backLayerBackgroundColor = colorScheme.background,
            backLayerContentColor = colorScheme.onBackground,
            frontLayerBackgroundColor = colorScheme.primaryContainer,
            frontLayerContentColor = colorScheme.onPrimaryContainer,
            stickyFrontLayer = false,
            backLayerContent = {
                ConceptMapMetaEditorContent(conceptMapState, localizedStrings, isDarkTheme, fhirContext)
            },
            frontLayerContent = {
                ConceptMappingEditorContent(conceptMapState = conceptMapState)
            }
        )
    }
}
