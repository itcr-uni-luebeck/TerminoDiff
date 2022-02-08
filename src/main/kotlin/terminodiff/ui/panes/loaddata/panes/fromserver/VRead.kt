package terminodiff.terminodiff.ui.panes.loaddata.panes.fromserver

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.ui.panes.loaddata.panes.fromserver.DownloadableCodeSystem

@Composable
fun VReadDialog(
    resource: InputResource,
    ktorClient: HttpClient,
    coroutineContext: CoroutineScope,
    fhirBaseUrl: String,
    localizedStrings: LocalizedStrings,
    onCloseRequest: () -> Unit,
) {

    val vReadVersions : List<DownloadableCodeSystem>? by produceState<List<DownloadableCodeSystem>?>(null, resource, fhirBaseUrl) {
        withContext(Dispatchers.IO) {
            Thread.sleep(1000)
        }
        value = emptyList()
        // TODO: 08/02/22 make a http request to _history here
    }

    Dialog(onCloseRequest, rememberDialogState(), title = localizedStrings.vRead) {
        Column {
            Text(resource.resourceUrl ?: "null")
            if (vReadVersions != null) {
                Text("non-null")
            }
        }
    }
}