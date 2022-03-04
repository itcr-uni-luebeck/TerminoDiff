package terminodiff.terminodiff.ui.panes.loaddata.panes.fromserver

import androidx.compose.material3.RadioButton
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.panes.loaddata.panes.fromserver.DownloadableCodeSystem
import terminodiff.ui.util.ColumnSpec
import terminodiff.ui.util.SelectableText


fun fromServerPaneColumnSpecs(
    localizedStrings: LocalizedStrings,
    selectedItem: DownloadableCodeSystem?,
    onCheckedChange: (DownloadableCodeSystem) -> Unit,
) = listOf(radioButtonColumnSpec(localizedStrings, selectedItem, onCheckedChange),
    canonicalColumnSpec(localizedStrings),
    versionColumnSpec(localizedStrings),
    nameColumnSpec(localizedStrings),
    titleColumnSpec(localizedStrings),
    metaVersionColumnSpec(localizedStrings))

private fun radioButtonColumnSpec(
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
    ColumnSpec.StringSearchableColumnSpec<DownloadableCodeSystem>(title = localizedStrings.canonicalUrl,
        weight = 0.1f,
        instanceGetter = { canonicalUrl })

private fun nameColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec.StringSearchableColumnSpec<DownloadableCodeSystem>(title = localizedStrings.name,
        weight = 0.1f,
        instanceGetter = { name }
    )

private fun titleColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec.StringSearchableColumnSpec<DownloadableCodeSystem>(title = localizedStrings.title,
        weight = 0.1f,
        instanceGetter = { title }
    )

private fun versionColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.version, weight = 0.1f, content = {
        SelectableText(text = it.version)
    }, tooltipText = { it.version })

private fun metaVersionColumnSpec(localizedStrings: LocalizedStrings) =
    ColumnSpec<DownloadableCodeSystem>(title = localizedStrings.metaVersion, weight = 0.05f, content = {
        SelectableText(text = it.metaVersion)
    })