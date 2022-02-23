package terminodiff.terminodiff.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState

@Composable
fun TerminodiffDialog(
    title: String,
    windowPosition: WindowPosition = WindowPosition(Alignment.Center),
    size: DpSize = DpSize(1024.dp, 512.dp),
    onCloseRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) = Dialog(onCloseRequest = onCloseRequest,
    title = title,
    state = rememberDialogState(position = windowPosition, size = size)) {
    //CompositionLocalProvider(LocalContentColor provides colorScheme.onPrimaryContainer) {
        Column(modifier = Modifier.background(colorScheme.primaryContainer).fillMaxSize().padding(4.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content)
    //}
}