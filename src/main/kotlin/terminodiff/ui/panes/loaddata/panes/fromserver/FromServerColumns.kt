package terminodiff.terminodiff.ui.panes.loaddata.panes.fromserver

import androidx.compose.material.RadioButton
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.panes.loaddata.panes.fromserver.DownloadableCodeSystem
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.SelectableText


fun fromServerPaneColumnSpecs(
    localizedStrings: LocalizedStrings,
    selectedItem: DownloadableCodeSystem?,
    onCheckedChange: (DownloadableCodeSystem) -> Unit,
) = listOf(checkboxColumnSpec(localizedStrings, selectedItem, onCheckedChange),
    canonicalColumnSpec(localizedStrings),
    versionColumnSpec(localizedStrings),
    titleColumnSpec(localizedStrings),
    metaVersionColumnSpec(localizedStrings))

private fun checkboxColumnSpec(
    localizedStrings: LocalizedStrings,
    selectedItem: DownloadableCodeSystem?,
    onCheckedChange: (DownloadableCodeSystem) -> Unit,
) = ColumnSpec<DownloadableCodeSystem>(
    title = localizedStrings.select,
    weight = 0.05f,
) { thisCs ->
    RadioButton(selected = when (selectedItem) {
        null -> false
        else -> selectedItem == thisCs
    }, onClick = { onCheckedChange.invoke(thisCs) })
}

private fun canonicalColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.canonicalUrl, weight = 0.1f, content = {
        SelectableText(text = it.canonicalUrl)
    }, tooltipText = { it.canonicalUrl })

private fun titleColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.title, weight = 0.1f, content = {
        SelectableText(text = it.title)
    }, tooltipText = { it.title })

private fun versionColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.version, weight = 0.1f, content = {
        SelectableText(text = it.version)
    }, tooltipText = { it.version })

private fun metaVersionColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.metaVersion, weight = 0.1f, content = {
        SelectableText(text = it.metaVersion)
    })