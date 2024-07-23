package app.editors.manager.ui.fragments.main

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import app.editors.manager.ui.fragments.share.InviteUsersFragment
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppButton
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.UiUtils

class PersonalPortalMigrationFragment : BaseDialogFragment() {

    companion object {

        fun newInstance(): InviteUsersFragment = InviteUsersFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UiUtils.isTablet(requireContext())) {
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialog
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setContent {
            ManagerTheme {
                MigrationScreen()
            }
        }
    }
}

@Composable
private fun MigrationScreen() {
    AppScaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = lib.toolkit.base.R.drawable.ic_close),
                            contentDescription = null
                        )
                    }
                },
                title = {}
            )
        }
    ) {
        NestedColumn(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.padding(bottom = 24.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.image_personal_to_docspace),
                contentDescription = null
            )
            Row(
                Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 24.dp)
            ) {
                Image(
                    modifier = Modifier.size(26.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_storage_onlyoffice),
                    contentDescription = null
                )
                Text(
                    text = "ONLYOFFICE Personal \nis wrapping up",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6
                )
            }
            Text(
                modifier = Modifier.padding(bottom = 32.dp),
                text = "ONLYOFFICE Personal will be discontinued on September 1st, 2024. \n" +
                        "We recommend you move to the free ONLYOFFICE DocSpace Cloud. ",
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.colorTextSecondary,
                textAlign = TextAlign.Center
            )
            AppButton(title = "Create a free account") {

            }
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
private fun MigrationScreenPreview() {
    ManagerTheme {
        MigrationScreen()
    }
}

