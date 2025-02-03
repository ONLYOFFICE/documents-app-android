package app.editors.manager.ui.fragments.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import app.editors.manager.R
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.fragments.room.add.AddRoomFragment.Companion.TAG
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppButton
import lib.compose.ui.views.AppScaffold

data class Feature(@StringRes val title: Int, @StringRes val description: Int, @DrawableRes val icon: Int)

class WhatsNewDialog : ComposeDialogFragment() {

    companion object {
        fun show(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(WhatsNewDialog(), TAG)
            transaction.commitAllowingStateLoss()
        }
    }

    private val newFeatures = listOf(
        Feature(
            title = R.string.whats_new_1,
            description = R.string.whats_new_1_desc,
            icon = R.drawable.ic_new_onlyoffice
        ),
        Feature(
            title = R.string.whats_new_2,
            description = R.string.whats_new_2_desc,
            icon = R.drawable.ic_new_tabbed
        ),
        Feature(
            title = R.string.whats_new_3,
            description = R.string.whats_new_3_desc,
            icon = R.drawable.ic_new_review
        ),
    )

    @Composable
    override fun Content() {
        ManagerTheme {
            AppScaffold(useTablePaddings = false) {
                WhatsNewScreen(newFeatures) {
                    dismiss()
                }
            }
        }
    }

}

@Composable
fun WhatsNewScreen(features: List<Feature>, onDismiss: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 48.dp, bottom = 24.dp, start = 30.dp, end = 30.dp)) {

        Text(text = stringResource(R.string.whats_new_title), style = MaterialTheme.typography.h5, modifier = Modifier
            .align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(36.dp))
        LazyColumn {
            items(features) { feature ->
                FeatureItem(feature = feature)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        AppButton(title = stringResource(R.string.whats_new_next), onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun FeatureItem(feature: Feature) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Image(painter = painterResource(id = feature.icon), contentDescription = null, modifier = Modifier.align(Alignment.CenterVertically))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = stringResource(feature.title), style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(feature.description), style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.colorTextSecondary))
        }
    }
}

@Preview
@Composable
private fun WhatsNewScreenPreview() {
    ManagerTheme {
       AppScaffold {
           WhatsNewScreen(
               features = listOf(
                   Feature(
                       title = lib.toolkit.base.R.string.app_title,
                       description = lib.toolkit.base.R.string.app_title,
                       icon = R.drawable.ic_vdr_room
                   ),
                   Feature(
                       title = lib.toolkit.base.R.string.app_title,
                       description = lib.toolkit.base.R.string.app_title,
                       icon = R.drawable.ic_collaboration_room
                   ),
                   Feature(
                       title = lib.toolkit.base.R.string.app_title,
                       description = lib.toolkit.base.R.string.app_title,
                       icon = R.drawable.ic_public_room
                   ),
                   Feature(
                       title = lib.toolkit.base.R.string.app_title,
                       description = lib.toolkit.base.R.string.app_title,
                       icon = R.drawable.ic_custom_room
                   )
               ),
               onDismiss = {}
           )
       }
    }
}