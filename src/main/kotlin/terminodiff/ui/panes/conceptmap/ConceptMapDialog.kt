package terminodiff.terminodiff.ui.panes.conceptmap

import androidx.compose.runtime.Composable
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.ui.util.TerminodiffDialog

@Composable
fun ConceptMapDialog(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    onCloseRequest: () -> Unit
) {
    TerminodiffDialog(
        localizedStrings.conceptMap,
        onCloseRequest = onCloseRequest,
    ) {

    }
}