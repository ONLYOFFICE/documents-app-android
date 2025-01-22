package lib.compose.ui.views

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary

@Composable
fun PlaceholderView(
    image: Int,
    title: String,
    subtitle: String,
    button: @Composable () -> Unit = {}
) {
    Surface(color = MaterialTheme.colors.background) {
        NestedColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier
                    .padding(top = 32.dp),
                contentScale = ContentScale.None,
                alignment = Alignment.BottomCenter,
                imageVector = ImageVector.vectorResource(image),
                contentDescription = null
            )
            Column(
                modifier = Modifier.padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = title,
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle + subtitle,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.colorTextSecondary
                )
                button()
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, device = "spec:parent=pixel_5,orientation=landscape")
@Preview(
    uiMode = UI_MODE_NIGHT_YES,
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
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

@Preview(uiMode = UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview2() {
    ManagerTheme {
        AppScaffold {
            PlaceholderView(
                image = lib.toolkit.base.R.drawable.placeholder_payment_required,
                title = "Business subscription expired",
                subtitle = "Your current tariff plan \"Business\" expired. Please renew your subscription in the account settings to be able to use your DocSpace. If you have any questions, please contact support"
            ) {
                AppButton(title = "Renew Business plan") {
                    
                }
            }
        }
    }
}