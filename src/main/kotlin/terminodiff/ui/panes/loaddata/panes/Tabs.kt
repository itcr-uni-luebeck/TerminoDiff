package terminodiff.terminodiff.ui.panes.loaddata.panes

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ca.uhn.fhir.context.FhirContext
import kotlinx.coroutines.launch
import libraries.accompanist.pager.ExperimentalPagerApi
import libraries.accompanist.pager.HorizontalPager
import libraries.accompanist.pager.PagerState
import libraries.pager_indicators.pagerTabIndicatorOffset
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import terminodiff.ui.MouseOverPopup
import terminodiff.ui.panes.loaddata.panes.fromserver.FromServerScreenWrapper

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(tabs: List<LoadFilesTabItem>, pagerState: PagerState, localizedStrings: LocalizedStrings) {
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
fun TabsContent(
    tabs: List<LoadFilesTabItem>,
    pagerState: PagerState,
    localizedStrings: LocalizedStrings,
    onLoadLeft: LoadListener,
    onLoadRight: LoadListener,
    fhirContext: FhirContext,
) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen(localizedStrings, onLoadLeft, onLoadRight, fhirContext)
    }
}

@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelText: String,
    singleLine: Boolean = true,
    trailingIconVector: ImageVector? = null,
    trailingIconDescription: String? = null,
    onTrailingIconClick: (() -> Unit)? = null,
) = TextField(value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = singleLine,
    label = {
        Text(text = labelText, color = colorScheme.onSecondaryContainer.copy(0.75f))
    },
    trailingIcon = {
        trailingIconVector?.let { imageVector ->
            if (trailingIconDescription == null) throw IllegalArgumentException("a content description has to be specified if a trailing icon is provided")
            MouseOverPopup(
                text = trailingIconDescription
            ) {
                when (onTrailingIconClick) {
                    null -> Icon(imageVector = imageVector,
                        contentDescription = trailingIconDescription,
                        tint = colorScheme.onSecondaryContainer)
                    else -> IconButton(onClick = onTrailingIconClick) {
                        Icon(imageVector = imageVector,
                            contentDescription = trailingIconDescription,
                            tint = colorScheme.onSecondaryContainer)
                    }
                }
            }

        }
    },
    colors = TextFieldDefaults.textFieldColors(backgroundColor = colorScheme.secondaryContainer,
        textColor = colorScheme.onSecondaryContainer,
        focusedIndicatorColor = colorScheme.onSecondaryContainer.copy(0.75f)))


typealias LoadListener = (InputResource) -> Unit

sealed class LoadFilesTabItem(
    val icon: ImageVector,
    val title: LocalizedStrings.() -> String,
    val screen: @Composable (LocalizedStrings, LoadListener, LoadListener, FhirContext) -> Unit,
) {
    object FromFile : LoadFilesTabItem(icon = Icons.Default.Save,
        title = { fileSystem },
        screen = { localizedStrings, onLoadLeft, onLoadRight, _ ->
            FromFileScreenWrapper(localizedStrings, onLoadLeft, onLoadRight)
        })

    object FromTerminologyServer : LoadFilesTabItem(icon = Icons.Default.Fireplace,
        title = { fhirTerminologyServer },
        screen = { localizedStrings, onLoadLeft, onLoadRight, fhirContext ->
            FromServerScreenWrapper(localizedStrings, onLoadLeft, onLoadRight, fhirContext)
        })
}