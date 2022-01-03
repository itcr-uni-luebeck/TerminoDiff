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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.xml.sax.InputSource
import terminodiff.i18n.LocalizedStrings
import java.io.InputStream

typealias ImageRelativePath = String

class AppIconResource {
    companion object {
        val icDarkMode: ImageRelativePath = "icons/ic-dark-mode.xml"
        val icChangeLanguage: ImageRelativePath = "icons/ic-language.xml"
        val icLoadLeftFile: ImageRelativePath = "icons/ic-open-left.xml"
        val icLoadRightFile: ImageRelativePath = "icons/ic-open-right.xml"
    }
}

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
                    imageRelativePath = AppIconResource.icDarkMode,
                    label = localizedStrings.toggleDarkTheme
                )
            }
            MouseOverPopup(localizedStrings.changeLanguage) {
                IconActionButton(
                    onClick = onLocaleChange,
                    imageRelativePath = AppIconResource.icChangeLanguage,
                    label = localizedStrings.changeLanguage
                )
            }
            MouseOverPopup(localizedStrings.loadLeftFile) {
                IconActionButton(
                    onClick = onLoadLeftFile,
                    imageRelativePath = AppIconResource.icLoadLeftFile,
                    label = localizedStrings.loadLeftFile
                )
            }
            MouseOverPopup(localizedStrings.loadRightFile) {
                IconActionButton(
                    onClick = onLoadRightFile,
                    imageRelativePath = AppIconResource.icLoadRightFile,
                    label = localizedStrings.loadRightFile
                )
            }
        }
    )
}

@Composable
private fun IconActionButton(
    onClick: () -> Unit,
    imageRelativePath: ImageRelativePath,
    label: String,
) {
    IconButton(onClick = onClick) {
        AppImageIcon(imageRelativePath, label)
    }
}

@Composable
fun AppImageIcon(
    relativePath: ImageRelativePath,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    AppIconResource::class.java.classLoader.getResourceAsStream(relativePath)?.let { iconStream ->
        Icon(
            loadXmlImageVector(
                iconStream,
                LocalDensity.current
            ), label,
            tint = tint
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

fun loadXmlImageVector(stream: InputStream, density: Density): ImageVector =
    stream.buffered().use { loadXmlImageVector(InputSource(it), density) }
