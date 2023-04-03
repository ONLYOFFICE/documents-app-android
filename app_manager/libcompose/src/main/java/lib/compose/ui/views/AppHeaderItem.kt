package lib.compose.ui.views

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.colorGrey

@Composable
fun AppHeaderItem(@StringRes title: Int) {
    AppHeaderItem(title = stringResource(id = title))
}

@Composable
fun AppHeaderItem(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.colorGrey,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 4.dp)
    )
}