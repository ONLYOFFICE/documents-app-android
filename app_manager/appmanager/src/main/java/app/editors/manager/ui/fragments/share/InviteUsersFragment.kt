package app.editors.manager.ui.fragments.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.viewModels.main.InviteUserState
import app.editors.manager.viewModels.main.InviteUserViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.putArgs

class InviteUsersFragment : BaseDialogFragment() {

    companion object {

        private const val ROOM_ID_KEY = "room_id_key"

        fun newInstance(roomId: String): InviteUsersFragment = InviteUsersFragment()
            .putArgs(ROOM_ID_KEY to roomId)
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
                val roomId = remember(arguments?.getString(ROOM_ID_KEY)::orEmpty)
                val viewModel = viewModel { InviteUserViewModel(roomId, requireContext().roomProvider) }
                val state by viewModel.state.collectAsState()

                InviteUsersScreen(state, viewModel::setInviteLinkEnabled)
            }
        }
    }
}

@Composable
private fun InviteUsersScreen(state: InviteUserState, onLinkEnable: (Boolean) -> Unit) {
    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AppTopBar(title = R.string.share_invite_user)
        }
    ) {
        if (state.screenLoading) {
            LoadingPlaceholder()
        } else {
            NestedColumn {
                if (state.requestLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    VerticalSpacer(height = 4.dp)
                }

                AppSwitchItem(
                    title = R.string.share_clipboard_external_link_label,
                    checked = state.externalLink != null,
                    onCheck = onLinkEnable
                )
                if (state.externalLink != null) {
                    AppArrowItem(
                        title = R.string.rooms_share_access_rights,
                        option = stringResource(id = RoomUtils.getAccessTitle(state.externalLink.access))
                    )
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    ) {
                        BasicTextField(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .padding(end = 8.dp),
                            value = state.externalLink.sharedTo.shareLink,
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
                }
                AppHeaderItem(title = R.string.invite_add_manually)
                AppArrowItem(title = R.string.invite_by_email)
                AppArrowItem(title = R.string.invite_choose_from_list)
            }
        }
    }
}

@Preview
@Composable
private fun InviteUsersScreenPreview() {
    ManagerTheme {
        InviteUsersScreen(
            InviteUserState(
                screenLoading = false,
                requestLoading = true,
                externalLink = ExternalLink(
                    access = 4,
                    sharedTo = ExternalLinkSharedTo("", "", "https://...", 0, null, null, false, false, false, "", null)
                )
            ), {}
        )
    }
}