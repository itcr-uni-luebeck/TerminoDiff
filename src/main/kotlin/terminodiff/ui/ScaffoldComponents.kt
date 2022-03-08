package terminodiff.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mediation
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
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
import ca.uhn.fhir.context.FhirContext
import org.xml.sax.InputSource
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.conceptmap.ConceptMapState
import terminodiff.terminodiff.ui.panes.conceptmap.ConceptMapDialog
import terminodiff.ui.panes.graph.showDiffGraphSwingWindow
import terminodiff.ui.panes.graph.showGraphSwingWindow
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
    diffDataContainer: DiffDataContainer?,
    conceptMapState: ConceptMapState?,
    showGraphButtons: Boolean,
    useDarkTheme: Boolean,
    fhirContext: FhirContext,
    onLocaleChange: () -> Unit,
    onChangeDarkTheme: () -> Unit,
    onReload: () -> Unit,
    onShowLoadScreen: () -> Unit,
) {

    var showConceptMapDialog by remember { mutableStateOf(false) }

    if (showConceptMapDialog && conceptMapState != null && diffDataContainer != null) {
        ConceptMapDialog(diffDataContainer = diffDataContainer,
            conceptMapState = conceptMapState,
            localizedStrings = localizedStrings,
            fhirContext = fhirContext,
            isDarkTheme = useDarkTheme) {
            showConceptMapDialog = false
        }
    }

    TopAppBar(title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.padding(end = 16.dp),
                text = localizedStrings.terminoDiff,
                color = colorScheme.onPrimaryContainer)
            AppImageIcon(relativePath = AppIconResource.icUniLuebeck,
                label = localizedStrings.uniLuebeck,
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxHeight(0.8f))
        }
    }, backgroundColor = colorScheme.primaryContainer, contentColor = colorScheme.onPrimaryContainer, actions = {
        val outlinedColors = ButtonDefaults.outlinedButtonColors(containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer)
        val filledColors = ButtonDefaults.buttonColors(containerColor = colorScheme.onPrimaryContainer,
            contentColor = colorScheme.primaryContainer)
        val border = BorderStroke(1.dp, colorScheme.onPrimaryContainer)
        if (diffDataContainer?.codeSystemDiff != null && showGraphButtons) {
            Row(modifier = Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(colors = outlinedColors, border = border, onClick = {
                    showGraphSwingWindow(codeSystem = diffDataContainer.leftCodeSystem!!,
                        frameTitle = localizedStrings.showLeftGraphButton,
                        useDarkTheme = useDarkTheme,
                        localizedStrings = localizedStrings)
                }) {
                    Text(localizedStrings.showLeftGraphButton)
                }

                Button(colors = filledColors, onClick = {
                    showDiffGraphSwingWindow(diffGraph = diffDataContainer.codeSystemDiff!!.differenceGraph,
                        frameTitle = localizedStrings.diffGraph,
                        useDarkTheme = useDarkTheme,
                        localizedStrings = localizedStrings)
                }) {
                    Text(localizedStrings.diffGraph)
                }

                Button(onClick = {
                    showConceptMapDialog = true
                }, enabled = conceptMapState != null, colors = filledColors) {
                    Icon(imageVector = Icons.Default.Mediation, contentDescription = localizedStrings.conceptMap)
                    Text(localizedStrings.conceptMap)
                }

                OutlinedButton(colors = outlinedColors, border = border, onClick = {
                    showGraphSwingWindow(codeSystem = diffDataContainer.rightCodeSystem!!,
                        frameTitle = localizedStrings.showRightGraphButton,
                        useDarkTheme = useDarkTheme,
                        localizedStrings = localizedStrings)
                }) {
                    Text(localizedStrings.showRightGraphButton)
                }
            }
        }

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

        MouseOverPopup(localizedStrings.openResources) {
            IconActionButton(onClick = onShowLoadScreen,
                imageVector = Icons.Default.FolderOpen,
                label = localizedStrings.reload)
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
private fun IconActionButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    label: String,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = imageVector,
            contentDescription = label,
            tint = colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun AppImageIcon(
    relativePath: ImageRelativePath,
    label: String,
    tint: Color = colorScheme.onPrimaryContainer,
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
    backgroundColor: Color = colorScheme.tertiaryContainer,
    foregroundColor: Color = colorScheme.onTertiaryContainer,
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
fun Modifier.cursorForHorizontalResize(): Modifier = pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))