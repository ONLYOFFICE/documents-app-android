package lib.compose.ui.views

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.colorTextSecondary

@Composable
fun AppHeaderItem(@StringRes title: Int, startIndent: Dp = 16.dp, modifier: Modifier = Modifier) {
    AppHeaderItem(modifier = modifier, title = stringResource(id = title), startIndent = startIndent)
}

@Composable
fun AppHeaderItem(title: String, startIndent: Dp = 16.dp, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.colorTextSecondary,
        modifier = modifier
            .fillMaxWidth()
            .padding(startIndent, 16.dp, 16.dp, 4.dp)
    )
}