package terminodiff.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.graph.CodeSystemRole
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.graphs.SugiyamaLayoutFrame

@Composable
fun ShowGraphsPanel(
    leftCs: CodeSystem,
    rightCs: CodeSystem,
    localizedStrings: LocalizedStrings,
    useDarkTheme: Boolean,
) {
    Card(
        Modifier.padding(8.dp).fillMaxWidth(),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        val buttonColors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                colors = buttonColors,
                onClick = {
                    showGraphSwingWindowJUngraphT(
                        codeSystem = leftCs,
                        codeSystemRole = CodeSystemRole.LEFT,
                        localizedStrings = localizedStrings,
                        frameTitle = localizedStrings.showLeftGraphButton,
                        useDarkTheme = useDarkTheme
                    )
                }) {
                Text(localizedStrings.showLeftGraphButton, color = MaterialTheme.colorScheme.onPrimary)
            }
            Text(
                text = localizedStrings.graphsOpenInOtherWindows,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Button(
                colors = buttonColors,
                onClick = {
                    showGraphSwingWindowJUngraphT(
                        codeSystem = rightCs,
                        codeSystemRole = CodeSystemRole.RIGHT,
                        localizedStrings = localizedStrings,
                        frameTitle = localizedStrings.showRightGraphButton,
                        useDarkTheme = useDarkTheme
                    )
                }) {
                Text(localizedStrings.showRightGraphButton, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

fun showGraphSwingWindowJUngraphT(
    codeSystem: CodeSystem,
    codeSystemRole: CodeSystemRole,
    localizedStrings: LocalizedStrings,
    frameTitle: String,
    useDarkTheme: Boolean
) {
    SugiyamaLayoutFrame(codeSystem, codeSystemRole, frameTitle, useDarkTheme)
}