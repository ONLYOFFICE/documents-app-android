package app.editors.manager.ui.compose.base

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import lib.toolkit.base.R

@Composable
fun Spacer(size: Dp) {
    Spacer(modifier = Modifier.size(size))
}