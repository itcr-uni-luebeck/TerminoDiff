package terminodiff.terminodiff.ui.panes.loaddata

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
    leftResource: InputResource?,
    rightResource: InputResource?,
    onGoButtonClick: () -> Unit,
) {
    Column(modifier.scrollable(scrollState, Orientation.Vertical)) {
        LoadedResourcesCard(localizedStrings, leftResource, rightResource, onGoButtonClick)
        LoadResourcesCards(onLoadLeft, onLoadRight, localizedStrings)
    }
}

@Composable
fun DividedTwoColumns(
    left: @Composable RowScope.(Modifier) -> Unit,
    right: @Composable RowScope.(Modifier) -> Unit,
    dividerColor: Color,
    rowModifier: Modifier = Modifier,
) = Row(modifier = rowModifier.fillMaxWidth().height(IntrinsicSize.Max),
    horizontalArrangement = Arrangement.SpaceAround) {
    left(Modifier.weight(0.45f))
    Divider(color = dividerColor, modifier = Modifier.width(2.dp).fillMaxHeight())
    right(Modifier.weight(0.45f))
}

@Composable
fun LoadedResourcesCard(
    localizedStrings: LocalizedStrings,
    leftResource: InputResource?,
    rightResource: InputResource?,
    onGoButtonClick: () -> Unit,
) = Card(modifier = Modifier.padding(8.dp).fillMaxWidth().wrapContentHeight(),
    elevation = 8.dp,
    backgroundColor = colorScheme.secondaryContainer,
    contentColor = colorScheme.onSecondaryContainer) {
    Column(Modifier.padding(4.dp).fillMaxWidth().wrapContentHeight().border(1.dp, Color.Green),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = localizedStrings.loadedResources,
            style = typography.titleLarge,
            color = colorScheme.onSecondaryContainer)
        Row(modifier = Modifier.padding(4.dp).fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceAround) {
            ResourceDescription(Modifier.weight(0.45f), localizedStrings, leftResource, DiffDataContainer.Side.LEFT)
            Divider(color = colorScheme.onSecondaryContainer, modifier = Modifier.width(2.dp).fillMaxHeight())
            ResourceDescription(Modifier.weight(0.45f), localizedStrings, rightResource, DiffDataContainer.Side.RIGHT)
        }
        /*DividedTwoColumns(left = { modifier ->
            LoadedDataColumn(leftResource, DiffDataContainer.Side.LEFT, localizedStrings, modifier = modifier)
        }, right = { modifier ->
            LoadedDataColumn(rightResource, DiffDataContainer.Side.RIGHT, localizedStrings, modifier = modifier)
        }, dividerColor = colorScheme.onSecondaryContainer, rowModifier = Modifier.padding(4.dp))*/

        if (leftResource != null && rightResource != null) {
            Button(onClick = onGoButtonClick,
                elevation = ButtonDefaults.elevation(8.dp),
                colors = ButtonDefaults.buttonColors(colorScheme.primaryContainer, colorScheme.onPrimaryContainer)) {
                Text(localizedStrings.calculateDiff, color = colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun ResourceDescription(
    modifier: Modifier = Modifier,
    localizedStrings: LocalizedStrings,
    resource: InputResource?,
    side: DiffDataContainer.Side,
) = Column(
    modifier = modifier.wrapContentHeight(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val text by derivedStateOf { formatText(resource, localizedStrings) }
    Text(text = localizedStrings.side_(side),
        style = typography.titleMedium,
        textDecoration = TextDecoration.Underline,
        color = colorScheme.onSecondaryContainer)
    //val text = formatText(resource, localizedStrings)
    Row(modifier = Modifier.align(Alignment.CenterHorizontally).height(IntrinsicSize.Min)) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = colorScheme.onSecondaryContainer,
            //modifier = Modifier.weight(1f).fillMaxWidth(),
            maxLines = 3,
            softWrap = true,
        )
    }
}

private fun formatText(resource: InputResource?, localizedStrings: LocalizedStrings) : AnnotatedString {
    val stringDescription = when {
        resource == null -> localizedStrings.noDataLoaded
        resource.kind == Kind.FILE -> {
            val path = resource.localFile!!.canonicalFile.invariantSeparatorsPath
            localizedStrings.fileFromPath_.invoke(path)
        }
        resource.kind == Kind.FHIR_SERVER -> "FHIR SERVER NOT IMPLEMENTED"
        else -> ""
    }
    return AnnotatedString(stringDescription)
}

@Composable
fun LoadedDataColumn(
    resource: InputResource?,
    side: DiffDataContainer.Side,
    localizedStrings: LocalizedStrings,
    modifier: Modifier = Modifier,
) = Column(modifier = modifier.wrapContentHeight().border(1.dp, Color.White),
    horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = localizedStrings.side_(side),
        style = typography.titleSmall,
        textDecoration = TextDecoration.Underline,
        color = colorScheme.onSecondaryContainer)
    when {
        resource == null -> Text(localizedStrings.noDataLoaded, color = colorScheme.onSecondaryContainer)
        resource.kind == Kind.FILE -> {
            val path = resource.localFile!!.canonicalFile.invariantSeparatorsPath
            val text = localizedStrings.fileFromPath_.invoke(path)
            Text(text = text,
                textAlign = TextAlign.Center,
                color = colorScheme.onSecondaryContainer,
                modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                softWrap = true,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis)
        }
        resource.kind == Kind.FHIR_SERVER -> TODO()
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LoadResourcesCards(onLoadLeft: LoadListener, onLoadRight: LoadListener, localizedStrings: LocalizedStrings) = Card(
    modifier = Modifier.padding(8.dp).fillMaxSize(),
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
        TabsContent(tabs = tabs,
            pagerState = pagerState,
            localizedStrings = localizedStrings,
            onLoadLeft = onLoadLeft,
            onLoadRight = onLoadRight)
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
private fun TabsContent(
    tabs: List<LoadFilesTabItem>,
    pagerState: PagerState,
    localizedStrings: LocalizedStrings,
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen(localizedStrings, onLoadLeft, onLoadRight)
    }
}

@Composable
private fun FromFileScreenWrapper(
    localizedStrings: LocalizedStrings,
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
) {
    var selectedPath: String by remember { mutableStateOf("") }
    val selectedFile: File by derivedStateOf { File(selectedPath) }
    FromFileScreen(localizedStrings = localizedStrings,
        selectedFile = selectedFile,
        selectedPath = selectedPath,
        onChangeFilePath = {
            selectedPath = it ?: ""
        },
        onLoadLeftFile = onLoadLeft,
        onLoadRightFile = onLoadRight)
}

@Composable
private fun FromFileScreen(
    localizedStrings: LocalizedStrings,
    selectedFile: File?,
    onChangeFilePath: (String?) -> Unit,
    onLoadLeftFile: (InputResource) -> Unit,
    onLoadRightFile: (InputResource) -> Unit,
    selectedPath: String,
) = Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
    val buttonColors =
        ButtonDefaults.buttonColors(backgroundColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
    val isValidPath by derivedStateOf {
        when {
            selectedFile == null -> false
            selectedFile.exists() -> true
            else -> false
        }
    }
    TextField(modifier = Modifier.fillMaxWidth().padding(12.dp),
        value = selectedPath,
        onValueChange = onChangeFilePath,
        label = {
            Text(localizedStrings.fileSystem, color = colorScheme.onSecondaryContainer.copy(0.75f)) // TODO: 04/02/22
        },
        trailingIcon = {
            IconButton(onClick = {
                val newFile = showLoadFileDialog(localizedStrings.loadFromFile)
                onChangeFilePath.invoke(newFile?.absolutePath)
            }) {
                Icon(imageVector = Icons.Default.Plagiarism,
                    contentDescription = localizedStrings.fileSystem,
                    tint = colorScheme.onSecondaryContainer)
            }
        },
        colors = TextFieldDefaults.textFieldColors(backgroundColor = colorScheme.secondaryContainer,
            textColor = colorScheme.onSecondaryContainer,
            focusedIndicatorColor = colorScheme.onSecondaryContainer.copy(0.75f)))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(modifier = Modifier.padding(4.dp),
            colors = buttonColors,
            enabled = isValidPath,
            onClick = { onLoadLeftFile(InputResource(Kind.FILE, selectedFile)) },
            elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)) {
            AppImageIcon(relativePath = AppIconResource.icLoadLeftFile,
                label = localizedStrings.loadLeftFile,
                tint = buttonColors.contentColor(true).value)
            Text(localizedStrings.loadLeftFile)
        }
        Button(modifier = Modifier.padding(4.dp),
            colors = buttonColors,
            enabled = isValidPath,
            onClick = { onLoadRightFile(InputResource(Kind.FILE, selectedFile)) },
            elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)) {
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
        screen = { localizedStrings, onLoadLeft, onLoadRight ->
            FromFileScreenWrapper(localizedStrings, onLoadLeft, onLoadRight)
        })

    object FromTerminologyServer : LoadFilesTabItem(icon = Icons.Default.Fireplace,
        title = { fhirTerminologyServer },
        screen = { localizedStrings: LocalizedStrings, onLoadLeft: LoadListener, onLoadRight: LoadListener -> {} })
}