package app.editors.manager.ui.fragments.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import app.editors.manager.R
import app.editors.manager.ui.fragments.room.add.AddRoomFragment.Companion.TAG
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppButton
import lib.compose.ui.views.AppScaffold

private data class Feature(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int
)

class WhatsNewDialog : ComposeDialogFragment() {

    companion object {

        fun show(fragmentManager: FragmentManager) {
            fragmentManager
                .beginTransaction()
                .add(WhatsNewDialog(), TAG)
                .commitAllowingStateLoss()
        }
    }

    private val newFeatures = listOf(
        Feature(
            title = R.string.whats_new_1,
            description = R.string.whats_new_1_desc,
            icon = R.drawable.ic_new_1
        ),
        Feature(
            title = R.string.whats_new_2,
            description = R.string.whats_new_2_desc,
            icon = R.drawable.ic_new_2
        ),
        Feature(
            title = R.string.whats_new_3,
            description = R.string.whats_new_3_desc,
            icon = R.drawable.ic_new_3
        ),
        Feature(
            title = R.string.whats_new_4,
            description = R.string.whats_new_4_desc,
            icon = R.drawable.ic_new_4
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
private fun WhatsNewScreen(features: List<Feature>, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.whats_new_title),
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 48.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    features.forEach { feature ->
                        FeatureItem(feature = feature)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AppButton(
                    title = stringResource(R.string.whats_new_next),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(feature: Feature) {
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

@Preview(locale = "zh")
@Composable
private fun WhatsNewScreenPreview() {
    ManagerTheme {
       AppScaffold {
           WhatsNewScreen(
               features = listOf(
                   Feature(
                       title = R.string.whats_new_1,
                       description = R.string.whats_new_1_desc,
                       icon = R.drawable.ic_new_1
                   ),
                   Feature(
                       title = R.string.whats_new_2,
                       description = R.string.whats_new_2_desc,
                       icon = R.drawable.ic_new_2
                   ),
                   Feature(
                       title = R.string.whats_new_3,
                       description = R.string.whats_new_3_desc,
                       icon = R.drawable.ic_new_3
                   ),
                   Feature(
                       title = R.string.whats_new_4,
                       description = R.string.whats_new_4_desc,
                       icon = R.drawable.ic_new_4
                   )
               ),
               onDismiss = {}
           )
       }
    }
}