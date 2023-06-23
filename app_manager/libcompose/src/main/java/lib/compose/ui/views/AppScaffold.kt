package lib.compose.ui.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import lib.compose.ui.addIf
import lib.compose.ui.theme.LocalUseTabletPadding

@Composable
fun AppScaffold(modifier: Modifier = Modifier, topBar: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
    Scaffold(modifier = modifier, topBar = topBar) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .addIf(LocalUseTabletPadding.current) {
                    val tabletPadding = dimensionResource(id = lib.toolkit.base.R.dimen.screen_left_right_padding)
                    padding(horizontal = tabletPadding)
                },
            color = MaterialTheme.colors.background,
            content = content
        )
    }
}