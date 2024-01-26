package lib.compose.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary

@Composable
fun PlaceholderView(image: Int, title: String, subtitle: String) {
    Surface(color = MaterialTheme.colors.background) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .width(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier
                        .padding(bottom = 60.dp)
                        .width(IntrinsicSize.Max),
                    imageVector = ImageVector.vectorResource(image),
                    alpha = 0.4f,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = title,
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.colorTextSecondary
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        AppScaffold {
            PlaceholderView(
                image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
                title = "Room created!",
                subtitle = "Create new folders to organize your files."
            )
        }
    }
}

@Preview
@Composable
private fun Preview2() {
    ManagerTheme {
        AppScaffold {
            PlaceholderView(
                image = lib.toolkit.base.R.drawable.placeholder_not_found,
                title = "No members found",
                subtitle = "You can add new team members manually or invite them via link"
            )
        }
    }
}