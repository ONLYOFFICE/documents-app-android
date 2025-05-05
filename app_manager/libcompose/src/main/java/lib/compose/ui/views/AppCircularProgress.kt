package lib.compose.ui.views

import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorButtonBackground
import lib.compose.ui.theme.colorTextTertiary

@Composable
fun AppCircularProgress() {
    CircularProgressIndicator(
        modifier = Modifier.width(36.dp),
        color = MaterialTheme.colors.colorTextTertiary,
        backgroundColor = MaterialTheme.colors.colorButtonBackground,
        strokeCap = StrokeCap.Round,
        strokeWidth = 3.dp
    )
}

@Preview
@Composable
fun AppCircularProgressPreview() {
    ManagerTheme {
        AppCircularProgress()
    }
}