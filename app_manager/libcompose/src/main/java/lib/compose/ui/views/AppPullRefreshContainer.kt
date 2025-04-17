package lib.compose.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppPullRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshThreshold: Dp = PullRefreshDefaults.RefreshThreshold,
    refreshingOffset: Dp = PullRefreshDefaults.RefreshingOffset,
    indicatorBackgroundColor: Color = MaterialTheme.colors.surface,
    indicatorContentColor: Color = contentColorFor(indicatorBackgroundColor),
    indicatorScale: Boolean = false,
    content: @Composable () -> Unit
){
    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh,
        refreshThreshold = refreshThreshold,
        refreshingOffset = refreshingOffset
    )

    Box(modifier.pullRefresh(refreshState)){
        content()
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            backgroundColor = indicatorBackgroundColor,
            contentColor = indicatorContentColor,
            scale = indicatorScale,
            modifier = Modifier.align(Alignment. TopCenter)
        )
    }
}