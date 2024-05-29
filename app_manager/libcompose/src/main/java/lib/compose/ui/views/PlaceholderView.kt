package lib.compose.ui.views

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
        NestedColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .weight(.5f)
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    imageVector = ImageVector.vectorResource(image),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier.weight(.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

@Preview(uiMode = UI_MODE_NIGHT_YES)
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
                image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
                title = "No docs here yet",
                subtitle = "All deleted files are moved to \'Trash\'. Restore files deleted by mistake or delete them permanently. Files in \'Trash\' are automatically deleted after 30 days. Please note, that the files deleted from the \'Trash\' cannot be restored any longer"
            )
        }
    }
}