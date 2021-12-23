package terminodiff.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import org.xml.sax.InputSource
import terminodiff.i18n.LocalizedStrings
import java.io.File

@Composable
fun TerminoDiffTopAppBar(
    localizedStrings: LocalizedStrings,
    onLocaleChange: () -> Unit,
    onLoadLeftFile: () -> Unit,
    onLoadRightFile: () -> Unit,
    onChangeDarkTheme: () -> Unit
) {

    TopAppBar(
        title = { Text(localizedStrings.terminoDiff, color = MaterialTheme.colorScheme.onPrimaryContainer) },
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        actions = {
            MouseOverPopup(localizedStrings.toggleDarkTheme) {
                IconActionButton(
                    onClick = onChangeDarkTheme,
                    resourceRelativePath = "icons/ic-dark-mode.xml",
                    label = localizedStrings.toggleDarkTheme
                )
            }
            MouseOverPopup(localizedStrings.changeLanguage) {
                IconActionButton(
                    onClick = onLocaleChange,
                    resourceRelativePath = "icons/ic-language.xml",
                    label = localizedStrings.changeLanguage
                )
            }
            MouseOverPopup(localizedStrings.toggleDarkTheme) {
                IconActionButton(
                    onClick = onLoadLeftFile,
                    resourceRelativePath = "icons/ic-open-left.xml",
                    label = localizedStrings.loadLeftFile
                )
            }
            MouseOverPopup(localizedStrings.loadRightFile) {
                IconActionButton(
                    onClick = onLoadRightFile,
                    resourceRelativePath = "icons/ic-open-right.xml",
                    label = localizedStrings.loadRightFile
                )
            }
        }
    )
}

@Composable
private fun IconActionButton(
    onClick: () -> Unit,
    resourceRelativePath: String,
    label: String,
) {
    IconButton(onClick = onClick) {
        Icon(
            loadXmlImageVector(
                File("src/main/resources/${resourceRelativePath.trimStart('/', '\\')}"),
                LocalDensity.current
            ), label,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * https://github.com/JetBrains/compose-jb/tree/master/tutorials/Desktop_Components#tooltips
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun MouseOverPopup(
    text: String,
    content: @Composable () -> Unit
) = TooltipArea(
    tooltip = {
        Surface(
            modifier = Modifier.shadow(4.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(text = text, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(10.dp))
        }
    },
    delayMillis = 750,
    tooltipPlacement = TooltipPlacement.CursorPoint(
        offset = DpOffset(10.dp, 10.dp),
        alignment = Alignment.BottomEnd
    ),
    content = content
)

fun loadXmlImageVector(file: File, density: Density): ImageVector =
    file.inputStream().buffered().use { loadXmlImageVector(InputSource(it), density) }
