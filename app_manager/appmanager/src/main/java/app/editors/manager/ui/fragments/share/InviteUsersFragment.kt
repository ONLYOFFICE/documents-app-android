package app.editors.manager.ui.fragments.share

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.fragments.share.link.RoomAccessScreen
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.InviteUserState
import app.editors.manager.viewModels.main.InviteUserViewModel
import app.editors.manager.viewModels.main.RoomInviteAccessViewModel
import app.editors.manager.viewModels.main.RoomUserListViewModel
import app.editors.manager.viewModels.main.UserListMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextPrimary
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getJsonString
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs
import java.net.URLEncoder

class InviteUsersFragment : ComposeDialogFragment() {

    companion object {

        private const val ROOM_ID_KEY = "room_id_key"
        private const val ROOM_TYPE_KEY = "room_type_key"

        fun newInstance(roomId: String, roomType: Int): InviteUsersFragment = InviteUsersFragment()
            .putArgs(ROOM_ID_KEY to roomId)
            .putArgs(ROOM_TYPE_KEY to roomType)
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            InviteUsersScreen(
                roomType = remember { arguments?.getInt(ROOM_TYPE_KEY) ?: -1 },
                roomId = remember(arguments?.getString(ROOM_ID_KEY)::orEmpty),
                roomProvider = requireContext().roomProvider,
                onCopyLink = { link ->
                    KeyboardUtils.setDataToClipboard(requireContext(), link)
                    UiUtils.getSnackBar(requireView()).setText(R.string.rooms_info_copy_link_to_clipboard).show()
                },
                onShareLink = { link ->
                    requireContext().openSendTextActivity(
                        getString(R.string.toolbar_menu_main_share),
                        link
                    )
                },
                onSnackBar = { UiUtils.getSnackBar(requireView()).setText(it).show() },
                onBack = ::dismiss
            )
        }
    }
}

private enum class Screens {
    Main, Access, InviteByEmail, InviteAccess, UserList
}

@Composable
fun InviteUsersScreen(
    roomType: Int,
    roomId: String,
    roomProvider: RoomProvider,
    onCopyLink: (String) -> Unit,
    onShareLink: (String) -> Unit,
    onSnackBar: (String) -> Unit,
    onBack: () -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        val context = LocalContext.current
        val viewModel = viewModel { InviteUserViewModel(roomId, roomType, roomProvider) }
        val navController = rememberNavController()
        val state by viewModel.state.collectAsState()

        NavHost(navController = navController, startDestination = Screens.Main.name) {
            composable(Screens.Main.name) {
                MainScreen(
                    state = state,
                    onLinkEnable = viewModel::setInviteLinkEnabled,
                    onAccessClick = { navController.navigate(Screens.Access.name) },
                    onCopyLink = { onCopyLink.invoke(state.externalLink?.sharedTo?.shareLink.orEmpty()) },
                    onShareLink = { onShareLink.invoke(state.externalLink?.sharedTo?.shareLink.orEmpty()) },
                    onInviteByEmailClick = { navController.navigate(Screens.InviteByEmail.name) },
                    onChooseFromListClick = { navController.navigate(Screens.UserList.name) },
                    onBack = onBack
                )
            }
            composable(Screens.Access.name) {
                RoomAccessScreen(
                    roomType = roomType,
                    currentAccess = state.externalLink?.access ?: -1,
                    portal = remember { context.accountOnline?.portalUrl.orEmpty() },
                    ownerOrAdmin = false,
                    isRemove = false,
                    onChangeAccess = viewModel::setAccess,
                    onBack = navController::popBackStackWhenResumed
                )
            }
            composable(Screens.InviteByEmail.name) {
                InviteByEmailScreen(
                    onBack = navController::popBackStackWhenResumed,
                    onNext = {
                        navController.navigate(
                            Screens.InviteAccess.name +
                                    "?emails=${Json.encodeToString<List<String>>(it)}&" +
                                    "users=null&" +
                                    "groups=null&"
                        )
                    }
                )
            }
            composable(Screens.UserList.name) {
                val userListViewModel = viewModel {
                    RoomUserListViewModel(
                        mode = UserListMode.Invite,
                        roomId = roomId,
                        roomType = roomType,
                        shareService = context.shareApi,
                        roomProvider = context.roomProvider,
                        resourcesProvider = context.appComponent.resourcesProvider,
                    )
                }
                UserListScreen(
                    title = R.string.filter_toolbar_users_title,
                    closeable = false,
                    disableInvited = true,
                    withGroups = roomType != ApiContract.RoomType.PUBLIC_ROOM,
                    viewModel = userListViewModel,
                    onClick = userListViewModel::toggleSelect,
                    onBack = navController::popBackStackWhenResumed,
                    onSnackBar = onSnackBar
                ) { size, access ->
                    UserListBottomContent(
                        nextButtonTitle = lib.toolkit.base.R.string.common_next,
                        count = size,
                        access = access,
                        accessList = RoomUtils.getAccessOptions(roomType, false),
                        onAccess = userListViewModel::setAccess,
                        onDelete = userListViewModel::onDelete
                    ) {
                        val users = Json.encodeToString(userListViewModel.getSelectedUsers().ifEmpty { null })
                        val groups = Json.encodeToString(userListViewModel.getSelectedGroups().ifEmpty { null })
                        navController.navigate(
                            Screens.InviteAccess.name +
                                    "?emails=null&" +
                                    "users=${URLEncoder.encode(users, Charsets.UTF_8.toString())}&" +
                                    "groups=${URLEncoder.encode(groups, Charsets.UTF_8.toString())}&" +
                                    "access=$access"
                        )
                    }
                }
            }
            composable(
                route = "${Screens.InviteAccess.name}?" +
                        "emails={emails}&" +
                        "users={users}&" +
                        "groups={groups}&" +
                        "access={access}",
                arguments = listOf(
                    navArgument("emails") { type = NavType.StringType; nullable = true },
                    navArgument("users") { type = NavType.StringType; nullable = true },
                    navArgument("groups") { type = NavType.StringType; nullable = true },
                    navArgument("access") { type = NavType.IntType; defaultValue = 2 }
                )
            ) {
                val inviteAccessViewModel = viewModel {
                    RoomInviteAccessViewModel(
                        roomId = roomId,
                        roomProvider = roomProvider,
                        access = it.arguments?.getInt("access") ?: 2,
                        users = it.arguments?.getJsonString<List<User>>("users", true).orEmpty(),
                        groups = it.arguments?.getJsonString<List<Group>>("groups", true).orEmpty(),
                        emails = it.arguments?.getJsonString<List<String>>("emails").orEmpty(),
                    )
                }
                InviteAccessScreen(
                    accessList = remember { RoomUtils.getAccessOptions(roomType, false) },
                    viewModel = inviteAccessViewModel,
                    onBack = navController::popBackStackWhenResumed,
                    onSnackBar = onSnackBar,
                    onSuccess = {
                        onSnackBar.invoke(context.getString(R.string.invite_link_send_success))
                        navController.navigate(Screens.Main.name) {
                            popUpTo(Screens.Main.name) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MainScreen(
    state: InviteUserState,
    onLinkEnable: (Boolean) -> Unit,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
    onAccessClick: () -> Unit,
    onInviteByEmailClick: () -> Unit,
    onChooseFromListClick: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AppTopBar(title = R.string.share_invite_user, backListener = onBack)
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
                        option = stringResource(id = RoomUtils.getAccessTitle(state.externalLink.access)),
                        onClick = onAccessClick
                    )
                    Row(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
                        BasicTextField(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .padding(end = 8.dp),
                            value = state.externalLink.sharedTo.shareLink,
                            readOnly = true,
                            onValueChange = {},
                            textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.colorTextPrimary),
                            singleLine = true
                        )
                        IconButton(onClick = onCopyLink) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_list_context_external_link),
                                tint = MaterialTheme.colors.primary,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = onShareLink) {
                            Icon(
                                imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_list_context_share),
                                tint = MaterialTheme.colors.primary,
                                contentDescription = null
                            )
                        }
                    }
                }
                AppHeaderItem(title = R.string.invite_add_manually)
                AppArrowItem(title = R.string.invite_by_email, onClick = onInviteByEmailClick)
                AppArrowItem(title = R.string.invite_choose_from_list, onClick = onChooseFromListClick)
            }
        }
    }
}

@Preview
@Composable
private fun InviteUsersScreenPreview() {
    ManagerTheme {
        MainScreen(
            InviteUserState(
                screenLoading = false,
                requestLoading = true,
                externalLink = ExternalLink(
                    access = 4,
                    sharedTo = ExternalLinkSharedTo("", "", "https://...", 0, null, null, false, false, false, "", null)
                )
            ), {}, {}, {}, {}, {}, {}, {}
        )
    }
}