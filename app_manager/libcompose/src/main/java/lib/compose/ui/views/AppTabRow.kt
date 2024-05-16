package lib.compose.ui.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary


@Immutable
class TabRowItem(val title: String)

@Immutable
private data class TabOffset(val left: Dp = 0.dp, val width: Dp = 0.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTabRow(pagerState: PagerState, tabs: List<TabRowItem>, onTabClick: (Int) -> Unit) {
    val tabsOffset = remember { mutableStateListOf(*Array(tabs.size) { TabOffset() }) }
    val density = LocalDensity.current

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.surface,
        indicator = {
            Box {
                TabRowIndicator(tabsOffset[pagerState.currentPage])
            }
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == pagerState.currentPage,
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.colorTextSecondary,
                onClick = { onTabClick.invoke(index) },
                text = {
                    Text(
                        text = tab.title,
                        modifier = Modifier.onPlaced {
                            with(density) {
                                tabsOffset[index] = TabOffset(
                                    left = it.positionInRoot().x.toDp(),
                                    width = it.size.width.toDp()
                                )
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun BoxScope.TabRowIndicator(tabOffset: TabOffset) {
    val spec = tween<Float>(durationMillis = 250, easing = FastOutSlowInEasing)
    val density = LocalDensity.current
    val currentTabWidth = remember { Animatable(0f) }
    val indicatorOffset = remember { Animatable(0f) }

    LaunchedEffect(tabOffset) {
        with(density) {
            launch {
                currentTabWidth.animateTo(tabOffset.width.toPx(), spec)
            }
            launch {
                indicatorOffset.animateTo(tabOffset.left.toPx(), spec)
            }
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(indicatorOffset.value.toInt(), 0) }
            .width(tabOffset.width)
            .align(Alignment.BottomStart)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .requiredHeight(4.dp)
            .background(color = MaterialTheme.colors.primary)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        Column {
            val tabs = listOf(TabRowItem("People"), TabRowItem("Groups"))
            val pagerState = rememberPagerState { tabs.size }
            val coroutineScope = rememberCoroutineScope()

            AppTabRow(
                pagerState = pagerState,
                tabs = tabs,
                onTabClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(it)
                    }
                }
            )
            HorizontalPager(state = pagerState) {}
        }
    }
}