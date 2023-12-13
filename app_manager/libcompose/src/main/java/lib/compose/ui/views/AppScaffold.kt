package lib.compose.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import lib.compose.ui.addIf
import lib.compose.ui.theme.LocalUseTabletPadding

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    useTablePaddings: Boolean = true,
    topBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState }) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .addIf(LocalUseTabletPadding.current && useTablePaddings) {
                    val tabletPadding = dimensionResource(id = lib.toolkit.base.R.dimen.screen_left_right_padding)
                    padding(horizontal = tabletPadding)
                },
            color = MaterialTheme.colors.background,
        ) {
            Box(contentAlignment = Alignment.TopCenter, content = content)
        }
    }
}