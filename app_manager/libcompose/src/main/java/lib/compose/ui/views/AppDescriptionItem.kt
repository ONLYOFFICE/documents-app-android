package lib.compose.ui.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.colorTextSecondary

@Composable
fun AppDescriptionItem(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colors.colorTextSecondary
) {
    Text(
        style = MaterialTheme.typography.body2,
        color = color,
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
fun AppDescriptionItem(
    modifier: Modifier = Modifier,
    text: Int,
    color: Color = MaterialTheme.colors.colorTextSecondary,
    vararg formatArgs: Any
) {
    AppDescriptionItem(
        modifier = modifier,
        text = stringResource(id = text, formatArgs = formatArgs),
        color = color
    )
}