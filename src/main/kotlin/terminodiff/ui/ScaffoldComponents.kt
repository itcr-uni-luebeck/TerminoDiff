package terminodiff.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.xml.sax.InputSource
import terminodiff.i18n.LocalizedStrings
import java.io.File

@Composable
fun TerminoDiffTopAppBar(
    localizedStrings: LocalizedStrings,
    onLocaleChange: () -> Unit,
    onLoadLeftFile: () -> Unit,
    onLoadRightFile: () -> Unit,
) {
    TopAppBar(
        title = { Text(localizedStrings.terminoDiff) },
        elevation = 8.dp,
        actions = {
            IconActionButton(
                onClick = onLocaleChange,
                resourceRelativePath = "icons/ic-language.xml",
                label = localizedStrings.changeLanguage
            )
            IconActionButton(
                onClick = onLoadLeftFile,
                resourceRelativePath = "icons/ic-open-left.xml",
                label = localizedStrings.loadLeftFile
            )
            IconActionButton(
                onClick = onLoadRightFile,
                resourceRelativePath = "icons/ic-open-right.xml",
                label = localizedStrings.loadRightFile
            )
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
            ), label
        )
    }
}

fun loadXmlImageVector(file: File, density: Density): ImageVector =
    file.inputStream().buffered().use { loadXmlImageVector(InputSource(it), density) }
