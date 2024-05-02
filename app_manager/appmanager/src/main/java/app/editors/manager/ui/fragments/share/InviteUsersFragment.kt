package app.editors.manager.ui.fragments.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.NestedColumn

class InviteUsersFragment : BaseDialogFragment() {


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
                InviteUsersScreen()
            }
        }
    }
}

@Composable
private fun InviteUsersScreen() {
    AppScaffold {
        NestedColumn {
            AppSwitchItem(title = R.string.share_clipboard_external_link_label, checked = true) {

            }
            AppArrowItem(
                title = R.string.rooms_share_access_rights,
                option = stringResource(id = RoomUtils.getAccessTitle(2))
            )
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp)
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp),
                    value = remember { mutableStateOf("https://docspace.onlyoffice.com/f12ddso3o4pdc") }.value,
                    readOnly = true,
                    onValueChange = {},
                    textStyle = MaterialTheme.typography.body1,
                    singleLine = true
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_list_context_external_link),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = null
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_list_context_share),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = null
                    )
                }
            }
            AppHeaderItem(title = R.string.invite_add_manually)
            AppArrowItem(title = R.string.invite_by_email)
            AppArrowItem(title = R.string.invite_choose_from_list)
        }
    }
}

@Preview
@Composable
private fun InviteUsersScreenPreview() {
    ManagerTheme {
        InviteUsersScreen()
    }
}