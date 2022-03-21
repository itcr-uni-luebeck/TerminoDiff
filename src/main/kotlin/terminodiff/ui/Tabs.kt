package terminodiff.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.uhn.fhir.context.FhirContext
import kotlinx.coroutines.launch
import libraries.accompanist.pager.ExperimentalPagerApi
import libraries.accompanist.pager.HorizontalPager
import libraries.accompanist.pager.PagerState
import libraries.accompanist.pager_indicators.pagerTabIndicatorOffset
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource

@OptIn(ExperimentalPagerApi::class)
@Composable
fun <T : TabItem.ScreenData> Tabs(tabs: List<TabItem<T>>, pagerState: PagerState, localizedStrings: LocalizedStrings) {
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
                    Icon(tabItem.spec.icon, contentDescription = null)
                },
                text = {
                    Text(tabItem.spec.title.invoke(localizedStrings))
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
fun <T : TabItem.ScreenData> TabsContent(
    tabs: List<TabItem<T>>,
    pagerState: PagerState,
    localizedStrings: LocalizedStrings,
    fhirContext: FhirContext,
    provideData: () -> T,
) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        Column(Modifier.padding(8.dp).fillMaxSize()) {
            tabs[page].spec.screen(localizedStrings, fhirContext, provideData.invoke())
        }
    }
}

typealias LoadListener = (InputResource) -> Unit

abstract class TabItem<T : TabItem.ScreenData>(
    val spec: TabItemSpec<T>
) {

    data class TabItemSpec<T : ScreenData>(
        val icon: ImageVector,
        val title: LocalizedStrings.() -> String,
        val screen: @Composable (LocalizedStrings, FhirContext, T) -> Unit,
    )

    interface ScreenData
}