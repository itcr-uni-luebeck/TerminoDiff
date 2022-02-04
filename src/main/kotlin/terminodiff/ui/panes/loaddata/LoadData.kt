package terminodiff.terminodiff.ui.panes.loaddata

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Plagiarism
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import li.flor.nativejfilechooser.NativeJFileChooser
import libraries.accompanist.pager.ExperimentalPagerApi
import libraries.accompanist.pager.HorizontalPager
import libraries.accompanist.pager.PagerState
import libraries.accompanist.pager.rememberPagerState
import libraries.pager_indicators.pagerTabIndicatorOffset
import org.apache.commons.lang3.SystemUtils
import terminodiff.engine.resources.DiffDataContainer
import terminodiff.i18n.LocalizedStrings
import terminodiff.preferences.AppPreferences
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.terminodiff.engine.resources.InputResource.Kind
import terminodiff.ui.AppIconResource
import terminodiff.ui.AppImageIcon
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun LoadDataPaneContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    localizedStrings: LocalizedStrings,
    onLoadLeftFile: () -> Unit,
    onLoadRightFile: () -> Unit,
    leftResource: InputResource?,
    rightResource: InputResource?,
    onGoButtonClick: () -> Unit,
) {
    Column(modifier.scrollable(scrollState, Orientation.Vertical)) {
        LoadedResourcesCard(localizedStrings, leftResource, rightResource, onGoButtonClick)
        LoadResourcesCards({
            // TODO: 04/02/22
        }, localizedStrings)
    }
}

@Composable
fun LoadedResourcesCard(
    localizedStrings: LocalizedStrings,
    leftResource: InputResource?,
    rightResource: InputResource?,
    onGoButtonClick: () -> Unit,
) = Card(modifier = Modifier.padding(8.dp).fillMaxWidth(),
    elevation = 8.dp,
    backgroundColor = colorScheme.secondaryContainer,
    contentColor = colorScheme.onSecondaryContainer) {
    Column(Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = localizedStrings.loadedResources,
            style = typography.titleLarge,
            color = colorScheme.onSecondaryContainer)
        Row(Modifier.fillMaxWidth().padding(4.dp).height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceAround) {
            LoadedDataColumn(leftResource, DiffDataContainer.Side.LEFT, localizedStrings)
            Box(Modifier.padding(horizontal = 4.dp)) {
                Divider(color = colorScheme.onSecondaryContainer, modifier = Modifier.fillMaxHeight().width(2.dp))
            }
            LoadedDataColumn(rightResource, DiffDataContainer.Side.RIGHT, localizedStrings)
        }

        if (leftResource != null && rightResource != null) {
            Button(onClick = onGoButtonClick,
                elevation = ButtonDefaults.elevation(8.dp),
                colors = ButtonDefaults.buttonColors(colorScheme.secondaryContainer,
                    colorScheme.onSecondaryContainer)) {
                Text(localizedStrings.calculateDiff)
            }
        }
    }
}

@Composable
fun RowScope.LoadedDataColumn(
    resource: InputResource?,
    side: DiffDataContainer.Side,
    localizedStrings: LocalizedStrings,
) = Column(Modifier.weight(0.45f), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(localizedStrings.side_(side),
        style = typography.titleSmall,
        textDecoration = TextDecoration.Underline,
        color = colorScheme.onSecondaryContainer)
    when {
        resource == null -> Text(localizedStrings.noDataLoaded, color = colorScheme.onSecondaryContainer)
        resource.kind == Kind.FILE -> {
            val path = resource.localFile!!.canonicalFile.invariantSeparatorsPath
            val text = localizedStrings.fileFromPath_.invoke(path)
            Text(text = text, textAlign = TextAlign.Center, color = colorScheme.onSecondaryContainer)
        }
        resource.kind == Kind.FHIR_SERVER -> TODO()
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LoadResourcesCards(onLoad: (InputResource) -> Unit, localizedStrings: LocalizedStrings) =
    Card(modifier = Modifier.padding(8.dp).fillMaxSize(),
        elevation = 8.dp,
        backgroundColor = colorScheme.tertiaryContainer,
        contentColor = colorScheme.onTertiaryContainer) {
        val tabs = listOf(LoadFilesTabItem.FromFile, LoadFilesTabItem.FromTerminologyServer)
        val pagerState = rememberPagerState()
        Column(modifier = Modifier.padding(4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = localizedStrings.loadedResources,
                style = typography.titleLarge,
                color = colorScheme.onTertiaryContainer)
            Tabs(tabs = tabs, pagerState = pagerState, localizedStrings = localizedStrings)
            TabsContent(tabs = tabs, pagerState = pagerState, localizedStrings = localizedStrings)
        }
    }

private fun getFileChooser(title: String): JFileChooser {
    return when (SystemUtils.IS_OS_MAC) {
        // NativeJFileChooser hangs on Azul Zulu 17 + JavaFX on macOS 12.1 aarch64.
        // With Azul Zulu w/o JFX, currently the file browser does not work at all on a M1 MBA.
        // The behaviour of NativeJFileChooser is different on Intel Macs, where it appears to work.
        // Hence, the non-native file chooser from Swing is used instead, which is not *nearly* as nice
        // as the native dialog on Windows, but it seems to be much more stable.
        true -> JFileChooser(AppPreferences.fileBrowserDirectory)
        else -> NativeJFileChooser(AppPreferences.fileBrowserDirectory)
    }.apply {
        dialogTitle = title
        isAcceptAllFileFilterUsed = false
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+JSON (*.json)", "json", "JSON"))
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+XML (*.xml)", "xml", "XML"))
    }
}

fun showLoadFileDialog(title: String): File? = getFileChooser(title).let { chooser ->
    when (chooser.showOpenDialog(null)) {
        JFileChooser.CANCEL_OPTION -> null
        JFileChooser.APPROVE_OPTION -> {
            return@let chooser.selectedFile?.absoluteFile ?: return null
        }
        else -> null
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun Tabs(tabs: List<LoadFilesTabItem>, pagerState: PagerState, localizedStrings: LocalizedStrings) {
    val scope = rememberCoroutineScope()
    TabRow(selectedTabIndex = pagerState.currentPage,
        backgroundColor = colorScheme.tertiaryContainer,
        contentColor = colorScheme.onTertiaryContainer,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions))
        }) {
        tabs.forEachIndexed { index, tabItem ->
            LeadingIconTab(
                icon = {
                    Icon(tabItem.icon, contentDescription = null, tint = colorScheme.onTertiaryContainer)
                },
                text = {
                    Text(tabItem.title.invoke(localizedStrings), color = colorScheme.onTertiaryContainer)
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun TabsContent(tabs: List<LoadFilesTabItem>, pagerState: PagerState, localizedStrings: LocalizedStrings) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen(
            localizedStrings,
            {
                // TODO: 04/02/22  
            },
            {
                // TODO: 04/02/22  
            }
        )
    }
}

@Composable
private fun FromFileScreen(
    localizedStrings: LocalizedStrings,
    onLoadLeftFile: (InputResource) -> Unit,
    onLoadRightFile: (InputResource) -> Unit,
) = Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
    val buttonColors =
        ButtonDefaults.buttonColors(backgroundColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
    var filePath by remember { mutableStateOf("") }
    TextField(modifier = Modifier.fillMaxWidth().padding(12.dp),
        value = filePath,
        onValueChange = { filePath = it },
        label = {
            Text(localizedStrings.fileSystem, color = colorScheme.onSecondaryContainer.copy(0.75f)) // TODO: 04/02/22
        },
        trailingIcon = {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Plagiarism,
                    contentDescription = localizedStrings.fileSystem,
                    tint = colorScheme.onSecondaryContainer.copy(0.75f))
            }
        },
        colors = TextFieldDefaults.textFieldColors(backgroundColor = colorScheme.secondaryContainer,
            textColor = colorScheme.onSecondaryContainer,
            focusedIndicatorColor = colorScheme.onSecondaryContainer.copy(0.75f)))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(modifier = Modifier.padding(4.dp),
            colors = buttonColors,
            onClick = { onLoadLeftFile(InputResource(Kind.FILE, File(filePath))) },
            elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)) {
            AppImageIcon(relativePath = AppIconResource.icLoadLeftFile,
                label = localizedStrings.loadLeftFile,
                tint = buttonColors.contentColor(true).value)
            Text(localizedStrings.loadLeftFile)
        }
        Button(
            modifier = Modifier.padding(4.dp),
            onClick = { onLoadRightFile(InputResource(Kind.FILE, File(filePath))) },
            colors = buttonColors,
            elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            AppImageIcon(relativePath = AppIconResource.icLoadRightFile,
                label = localizedStrings.loadRightFile,
                tint = buttonColors.contentColor(true).value)
            Text(localizedStrings.loadRightFile)
        }
    }
}


typealias LoadListener = (InputResource) -> Unit

sealed class LoadFilesTabItem(
    val icon: ImageVector,
    val title: LocalizedStrings.() -> String,
    val screen: @Composable (LocalizedStrings, LoadListener, LoadListener) -> Unit,
) {
    object FromFile : LoadFilesTabItem(icon = Icons.Default.Save,
        title = { fileSystem },
        screen = { loc, left, right -> FromFileScreen(loc, left, right) })

    object FromTerminologyServer : LoadFilesTabItem(icon = Icons.Default.Fireplace,
        title = { fhirTerminologyServer },
        screen = { localizedStrings: LocalizedStrings, leftLoad: LoadListener, rightLoad: LoadListener -> {} })
}