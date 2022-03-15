package terminodiff.terminodiff.ui.releasenotes

import androidx.compose.runtime.Composable
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings

@Composable
fun ReleaseNotesWindow(
    diffDataContainer: DiffDataContainer,
    localizedStrings: LocalizedStrings,
    onCloseRequest: () -> Unit
) {

    val splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.5f)
}