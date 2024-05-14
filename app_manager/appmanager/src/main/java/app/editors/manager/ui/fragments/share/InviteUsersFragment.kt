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
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.fragments.share.link.RoomAccessScreen
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.InviteByEmailViewModel
import app.editors.manager.viewModels.main.InviteUserState
import app.editors.manager.viewModels.main.InviteUserViewModel
import app.editors.manager.viewModels.main.UserListEffect
import app.editors.manager.viewModels.main.UserListViewModel
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
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs

class InviteUsersFragment : BaseDialogFragment() {

    companion object {

        private const val ROOM_ID_KEY = "room_id_key"
        private const val ROOM_TYPE_KEY = "room_type_key"

        fun newInstance(roomId: String, roomType: Int): InviteUsersFragment = InviteUsersFragment()
            .putArgs(ROOM_ID_KEY to roomId)
            .putArgs(ROOM_TYPE_KEY to roomType)
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
}

private enum class Screens {
    Main, Access, InviteByEmail, InviteByEmailAccess, UserList
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
        val viewModel = viewModel { InviteUserViewModel(roomId, roomType, roomProvider) }
        val byEmailViewModel = viewModel { InviteByEmailViewModel(roomId, roomType, roomProvider) }
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
                    ownerOrAdmin = false,
                    isRemove = false,
                    onChangeAccess = viewModel::setAccess,
                    onBack = navController::popBackStackWhenResumed
                )
            }
            composable(Screens.InviteByEmail.name) {
                InviteByEmailScreen(
                    viewModel = byEmailViewModel,
                    onBack = navController::popBackStackWhenResumed,
                    onNext = { navController.navigate(Screens.InviteByEmailAccess.name) }
                )
            }
            composable(Screens.InviteByEmailAccess.name) {
                InviteByEmailAccessScreen(
                    roomType = roomType,
                    viewModel = byEmailViewModel,
                    onBack = navController::popBackStackWhenResumed,
                    onSnackBar = onSnackBar,
                    onSuccess = {
                        navController.navigate(Screens.Main.name) {
                            popUpTo(Screens.Main.name) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Screens.UserList.name) {
                val context = LocalContext.current
                val userListViewModel = viewModel {
                    UserListViewModel(
                        roomId = roomId,
                        roomType = roomType,
                        shareService = context.shareApi,
                        roomProvider = context.roomProvider,
                        resourcesProvider = context.appComponent.resourcesProvider,
                    )
                }
                val userListState by userListViewModel.viewState.collectAsState()

                LaunchedEffect(Unit) {
                    userListViewModel.effect.collect {
                        when (it) {
                            is UserListEffect.Error -> onSnackBar.invoke(it.message)
                            UserListEffect.Success -> navController.popBackStackWhenResumed()
                        }
                    }
                }

                UserListScreen(
                    title = R.string.filter_toolbar_users_title,
                    closeable = false,
                    userListState = userListState,
                    onClick = userListViewModel::toggleSelect,
                    onSearch = userListViewModel::search,
                    onBack = navController::popBackStackWhenResumed
                ) {
                    UserListBottomContent(
                        count = userListState.selected.size,
                        access = userListState.access ?: 0,
                        accessList = RoomUtils.getAccessOptions(roomType, false),
                        onAccess = userListViewModel::setAccess,
                        onDelete = userListViewModel::onDelete,
                        onNext = userListViewModel::onInvite
                    )
                }
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