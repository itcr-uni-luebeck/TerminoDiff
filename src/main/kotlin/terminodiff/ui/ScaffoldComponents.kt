package terminodiff.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.xml.sax.InputSource
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import java.awt.Cursor
import java.io.InputStream

typealias ImageRelativePath = String

class AppIconResource {
    companion object {
        const val icDarkMode: ImageRelativePath = "icons/ic-dark-mode.xml"
        const val icChangeLanguage: ImageRelativePath = "icons/ic-language.xml"
        const val icLoadLeftFile: ImageRelativePath = "icons/ic-open-left.xml"
        const val icLoadRightFile: ImageRelativePath = "icons/ic-open-right.xml"
        const val icReload: ImageRelativePath = "icons/ic-reload.xml"
        const val icUniLuebeck: ImageRelativePath = "uzl-logo.xml"

        fun loadFile(relativePath: ImageRelativePath): InputStream? =
            AppIconResource::class.java.classLoader.getResourceAsStream(relativePath)

        @Composable
        fun loadXmlImageVector(stream: InputStream): ImageVector =
            stream.buffered().use { loadXmlImageVector(InputSource(it), LocalDensity.current) }

        @Composable
        fun loadXmlImageVector(relativePath: ImageRelativePath): ImageVector =
            loadFile(relativePath)?.let { loadXmlImageVector(it) }
                ?: throw IllegalArgumentException("the file $relativePath could not be loaded")
    }
}

@Composable
fun TerminoDiffTopAppBar(
    localizedStrings: LocalizedStrings,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    onReload: () -> Unit,
) {

    TopAppBar(title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.padding(end = 16.dp),
                text = localizedStrings.terminoDiff,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            AppImageIcon(
                relativePath = AppIconResource.icUniLuebeck,
                label = localizedStrings.uniLuebeck,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxHeight(0.8f)
            )
        }
    },
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        actions = {
            MouseOverPopup(localizedStrings.toggleDarkTheme) {
                IconActionButton(onClick = onChangeDarkTheme,
                    imageRelativePath = AppIconResource.icDarkMode,
                    label = localizedStrings.toggleDarkTheme)
            }
            MouseOverPopup(localizedStrings.changeLanguage) {
                IconActionButton(onClick = onLocaleChange,
                    imageRelativePath = AppIconResource.icChangeLanguage,
                    label = localizedStrings.changeLanguage)
            }
            MouseOverPopup(localizedStrings.reload) {
                IconActionButton(onClick = onReload,
                    imageRelativePath = AppIconResource.icReload,
                    label = localizedStrings.reload)
            }
        })
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
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier: Modifier = Modifier,
) {
    AppIconResource.loadFile(relativePath)?.let { iconStream ->
        Icon(modifier = modifier,
            imageVector = AppIconResource.loadXmlImageVector(iconStream),
            contentDescription = label,
            tint = tint)
    }
}

/**
 * https://github.com/JetBrains/compose-jb/tree/master/tutorials/Desktop_Components#tooltips
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun MouseOverPopup(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    foregroundColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    content: @Composable () -> Unit,
) = TooltipArea(tooltip = {
    Surface(modifier = Modifier.shadow(4.dp), color = backgroundColor, shape = RoundedCornerShape(4.dp)) {
        Text(text = text, color = foregroundColor, modifier = Modifier.padding(10.dp))
    }
},
    delayMillis = 750,
    tooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(10.dp, 10.dp), alignment = Alignment.BottomEnd),
    content = content)


@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.cursorForHorizontalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))