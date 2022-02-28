package terminodiff.terminodiff.ui.panes.loaddata

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import libraries.accompanist.pager.ExperimentalPagerApi
import libraries.accompanist.pager.rememberPagerState
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.terminodiff.engine.resources.InputResource.Kind
import terminodiff.terminodiff.ui.panes.loaddata.panes.*
import terminodiff.ui.*
import terminodiff.ui.panes.loaddata.panes.fromserver.FromServerScreenWrapper

@Composable
fun LoadDataPaneContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    localizedStrings: LocalizedStrings,
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
    leftResource: InputResource?,
    rightResource: InputResource?,
    fhirContext: FhirContext,
    onGoButtonClick: () -> Unit,
) {
    Column(modifier.scrollable(scrollState, Orientation.Vertical)) {
        LoadedResourcesCard(localizedStrings, leftResource, rightResource, onGoButtonClick)
        LoadResourcesCards(onLoadLeft, onLoadRight, localizedStrings, fhirContext)
    }
}

@Composable
fun ColumnScope.LoadedResourcesCard(
    localizedStrings: LocalizedStrings,
    leftResource: InputResource?,
    rightResource: InputResource?,
    onGoButtonClick: () -> Unit,
) = Card(modifier = Modifier.padding(8.dp).fillMaxWidth().weight(0.15f),
    elevation = 8.dp,
    backgroundColor = colorScheme.secondaryContainer,
    contentColor = colorScheme.onSecondaryContainer) {
    Column(Modifier.padding(4.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
        when {
            leftResource != null && rightResource != null -> {
                Button(
                    modifier = Modifier.weight(0.3f),
                    onClick = onGoButtonClick,
                    elevation = ButtonDefaults.elevation(8.dp),
                    colors = ButtonDefaults.buttonColors(colorScheme.primary,
                        colorScheme.onPrimary)) {
                    Text(localizedStrings.calculateDiff, color = colorScheme.onPrimary)
                }
            }
            else -> {
                Text(text = localizedStrings.loadedResources,
                    style = typography.titleLarge,
                    color = colorScheme.onSecondaryContainer)
            }
        }

        Row(modifier = Modifier.padding(4.dp).fillMaxWidth().weight(0.7f),
            horizontalArrangement = Arrangement.SpaceAround) {
            ResourceDescription(Modifier.weight(0.45f), localizedStrings, leftResource, DiffDataContainer.Side.LEFT)
            Divider(color = colorScheme.onSecondaryContainer, modifier = Modifier.width(2.dp).fillMaxHeight())
            ResourceDescription(Modifier.weight(0.45f), localizedStrings, rightResource, DiffDataContainer.Side.RIGHT)
        }
    }
}

@Composable
fun ResourceDescription(
    modifier: Modifier = Modifier,
    localizedStrings: LocalizedStrings,
    resource: InputResource?,
    side: DiffDataContainer.Side,
) = Column(modifier = modifier.wrapContentHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
    val text by derivedStateOf { formatText(resource, localizedStrings) }
    Text(text = localizedStrings.side_(side),
        style = typography.titleMedium,
        textDecoration = TextDecoration.Underline,
        color = colorScheme.onSecondaryContainer)
    Row(modifier = Modifier.align(Alignment.CenterHorizontally).height(IntrinsicSize.Min)) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = colorScheme.onSecondaryContainer,
            maxLines = 3,
            softWrap = true,
        )
    }
}

private fun formatText(resource: InputResource?, localizedStrings: LocalizedStrings): AnnotatedString {
    val stringDescription = when {
        resource == null -> localizedStrings.noDataLoaded
        resource.kind == Kind.FILE -> {
            val path = resource.localFile!!.canonicalFile.invariantSeparatorsPath
            localizedStrings.fileFromPath_.invoke(path)
        }
        resource.kind == Kind.FHIR_SERVER -> {
            val url = resource.resourceUrl!!
            localizedStrings.fileFromUrl_.invoke(url)
        }
        resource.kind == Kind.VREAD -> {
            val url = resource.resourceUrl!!
            val metaVersion = resource.downloadableCodeSystem!!.metaVersion
            localizedStrings.vreadFromUrlAndMetaVersion_.invoke(url, metaVersion!!)
        }
        else -> ""
    }
    return AnnotatedString(stringDescription)
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ColumnScope.LoadResourcesCards(
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
    localizedStrings: LocalizedStrings,
    fhirContext: FhirContext,
) = Card(modifier = Modifier.padding(8.dp).fillMaxWidth().weight(0.66f, true),
    elevation = 8.dp,
    backgroundColor = colorScheme.tertiaryContainer,
    contentColor = colorScheme.onTertiaryContainer) {
    val tabs = listOf(LoadFilesTabItem.FromFile, LoadFilesTabItem.FromTerminologyServer)
    val pagerState = rememberPagerState()
    Column(modifier = Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Tabs(tabs = tabs, pagerState = pagerState, localizedStrings = localizedStrings)
        TabsContent(tabs = tabs,
            pagerState = pagerState,
            localizedStrings = localizedStrings,
            fhirContext = fhirContext
        ) { LoadFilesTabItem.LoadFilesScreenData(onLoadLeft, onLoadRight) }
    }
}

sealed class LoadFilesTabItem(
    icon: ImageVector,
    title: LocalizedStrings.() -> String,
    screen: @Composable (LocalizedStrings, FhirContext, LoadFilesScreenData) -> Unit,
) : TabItem<LoadFilesTabItem.LoadFilesScreenData>(icon, title, screen) {

    object FromFile : LoadFilesTabItem(icon = Icons.Default.Save,
        title = { fileSystem },
        screen = { strings, _, data ->
            FromFileScreenWrapper(strings, data.onLoadLeft, data.onLoadRight)
        })

    object FromTerminologyServer : LoadFilesTabItem(icon = Icons.Default.Fireplace,
        title = { fhirTerminologyServer },
        screen = { strings, fhirContext, data ->
            FromServerScreenWrapper(strings, data.onLoadLeft, data.onLoadRight, fhirContext)
        })

    class LoadFilesScreenData(
        val onLoadLeft: LoadListener,
        val onLoadRight: LoadListener,
    ) : ScreenData
}