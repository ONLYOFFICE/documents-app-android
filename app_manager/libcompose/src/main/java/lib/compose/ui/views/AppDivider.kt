package lib.compose.ui.views

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lib.toolkit.base.R

@Composable
fun AppDivider(modifier: Modifier = Modifier, startIndent: Dp = 0.dp) {
    Divider(modifier = modifier, color = colorResource(id = R.color.colorOutline), startIndent = startIndent)
}