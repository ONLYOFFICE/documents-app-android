package app.editors.manager.ui.fragments.share.link

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.ShareGroup
import app.documents.core.network.share.models.SharedTo
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import app.editors.manager.ui.fragments.share.InviteUsersScreen
import app.editors.manager.viewModels.link.RoomInfoEffect
import app.editors.manager.viewModels.link.RoomInfoState
import app.editors.manager.viewModels.link.RoomInfoViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.rememberWaitingDialog
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs

class RoomInfoFragment : BaseDialogFragment() {

    companion object {

        private const val KEY_ROOM = "key_room"
        val TAG: String = RoomInfoFragment::class.java.simpleName

        fun newInstance(room: CloudFolder): RoomInfoFragment =
            RoomInfoFragment().putArgs(KEY_ROOM to room)

    }

    enum class RoomInfoScreens {
        RoomInfo, UserAccess, LinkSettings, InviteUsers
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ComponentDialog(
            requireContext(),
            if (!UiUtils.isTablet(requireContext())) R.style.FullScreenDialog else 0
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {
            ManagerTheme {
                val keyboardController = LocalSoftwareKeyboardController.current
                val portal = remember { requireContext().accountOnline?.portal }
                val room = remember { checkNotNull(arguments?.getSerializableExt<CloudFolder>(KEY_ROOM)) }
                val canEditRoom = room.security.editRoom
                val viewModel = viewModel { RoomInfoViewModel(requireContext().roomProvider, room.id) }
                val navController = rememberNavController().also {
                    it.addOnDestinationChangedListener { _, destination, _ ->
                        if (destination.route == RoomInfoScreens.RoomInfo.name) {
                            viewModel.fetchRoomInfo()
                        }
                    }
                }
                val state by viewModel.state.collectAsState()
                val waitingDialog = rememberWaitingDialog(
                    title = R.string.dialogs_wait_title,
                    onCancel = viewModel::cancelOperation
                )

                LaunchedEffect(Unit) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is RoomInfoEffect.Create -> {
                                copyLinkToClipboard(requireView(), effect.url, true)
                                waitingDialog.dismiss()
                            }
                            is RoomInfoEffect.Error -> {
                                UiUtils.getShortSnackBar(requireView())
                                    .setText(effect.message)
                                    .show()
                            }
                            RoomInfoEffect.ShowOperationDialog -> {
                                keyboardController?.hide()
                                waitingDialog.show()
                                delay(500)
                            }
                            RoomInfoEffect.CloseDialog -> waitingDialog.dismiss()
                        }
                    }
                }

                Surface(color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = RoomInfoScreens.RoomInfo.name) {
                        composable(route = RoomInfoScreens.RoomInfo.name) {
                            RoomInfoScreen(
                                state = state,
                                canEditRoom = canEditRoom,
                                roomType = room.roomType,
                                roomTitle = room.title,
                                portal = portal?.url.orEmpty(),
                                onBackClick = this@RoomInfoFragment::onBackPressed,
                                onAddUsers = { navController.navigate(RoomInfoScreens.InviteUsers.name) },
                                onSetUserAccess = { userId, access, ownerOrAdmin ->
                                    navController.navigate(
                                        RoomInfoScreens.UserAccess.name +
                                                "?userId=$userId" +
                                                "&access=$access" +
                                                "&ownerOrAdmin=$ownerOrAdmin"
                                    )
                                },
                                onSharedLinkCreate = {
                                    navController.navigate("${RoomInfoScreens.LinkSettings.name}?create=true")
                                },
                                onLinkClick = { link ->
                                    val json = Json.encodeToString(link.sharedTo)
                                    navController.navigate("${RoomInfoScreens.LinkSettings.name}?link=$json")
                                },
                            )
                        }
                        composable(
                            route = "${RoomInfoScreens.LinkSettings.name}?link={link}&create={create}",
                            arguments = listOf(
                                navArgument("link") {
                                    type = NavType.StringType
                                    defaultValue = Json.encodeToString(
                                        ExternalLinkSharedTo(
                                            id = "",
                                            title = "",
                                            shareLink = "",
                                            linkType = 1,
                                            password = null,
                                            denyDownload = false,
                                            isExpired = false,
                                            primary = false,
                                            requestToken = "",
                                            expirationDate = null
                                        )
                                    )
                                },
                                navArgument("create") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            ExternalLinkSettingsScreen(
                                link = backStackEntry.arguments?.getString("link")?.let(Json::decodeFromString),
                                isCreate = backStackEntry.arguments?.getBoolean("create") == true,
                                roomId = room.id,
                                roomType = room.roomType,
                                onBackListener = navController::popBackStackWhenResumed
                            )
                        }
                        composable(
                            route = "${RoomInfoScreens.UserAccess.name}?" +
                                    "userId={userId}&" +
                                    "access={access}&" +
                                    "ownerOrAdmin={ownerOrAdmin}",
                            arguments = listOf(
                                navArgument("userId") { type = NavType.StringType },
                                navArgument("access") { type = NavType.IntType },
                                navArgument("ownerOrAdmin") { type = NavType.BoolType }
                            )
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                            val roomId = room.id
                            RoomAccessScreen(
                                roomType = room.roomType,
                                currentAccess = backStackEntry.arguments?.getInt("access") ?: -1,
                                ownerOrAdmin = backStackEntry.arguments?.getBoolean("ownerOrAdmin") == true,
                                isRemove = true,
                                onBack = navController::popBackStackWhenResumed,
                                onChangeAccess = { newAccess -> viewModel.setUserAccess(roomId, userId, newAccess) }
                            )
                        }
                        composable(RoomInfoScreens.InviteUsers.name) {
                            InviteUsersScreen(
                                roomType = room.roomType,
                                roomId = room.id,
                                roomProvider = requireContext().roomProvider,
                                onSnackBar = { UiUtils.getSnackBar(requireView()).setText(it).show() },
                                onCopyLink = { link ->
                                    KeyboardUtils.setDataToClipboard(requireContext(), link)
                                    UiUtils.getSnackBar(requireView())
                                        .setText(R.string.rooms_info_copy_link_to_clipboard).show()
                                },
                                onShareLink = { link ->
                                    requireContext().openSendTextActivity(
                                        getString(R.string.toolbar_menu_main_share),
                                        link
                                    )
                                },
                                onBack = {
                                    navController.popBackStackWhenResumed()
                                    viewModel.fetchRoomInfo()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RoomInfoScreen(
        state: RoomInfoState,
        canEditRoom: Boolean,
        roomType: Int?,
        roomTitle: String?,
        portal: String?,
        onSetUserAccess: (userId: String, access: Int, ownerOrAdmin: Boolean) -> Unit,
        onAddUsers: () -> Unit,
        onBackClick: () -> Unit,
        onLinkClick: (ExternalLink) -> Unit,
        onSharedLinkCreate: () -> Unit
    ) {
        BackHandler(onBack = onBackClick)

        AppScaffold(
            useTablePaddings = false,
            topBar = {
                AppTopBar(
                    title = {
                        Column {
                            Text(text = roomTitle ?: stringResource(id = R.string.list_context_info))
                            roomType?.let { type ->
                                Text(
                                    text = stringResource(id = RoomUtils.getRoomInfo(type).title),
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    },
                    actions = {
                        TopAppBarAction(
                            icon = R.drawable.ic_add_users,
                            onClick = onAddUsers,
                            enabled = canEditRoom
                        )
                    },
                    backListener = onBackClick
                )
            }
        ) {
            if (state.isLoading) {
                LoadingPlaceholder()
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val groupedShareList = Share.groupByAccess(state.shareList)
                    if (ApiContract.RoomType.hasExternalLink(roomType)) {
                        ExternalLinkBlock(
                            sharedLinks = state.sharedLinks,
                            canEditRoom = canEditRoom,
                            onLinkClick = onLinkClick,
                            onSharedLinkCreate = onSharedLinkCreate,
                            onCopyLinkClick = { url -> copyLinkToClipboard(requireView(), url, false) }
                        )
                    }
                    ShareUsersList(
                        title = R.string.rooms_info_admin_title,
                        portal = portal,
                        groupedShareList = groupedShareList,
                        key = ShareGroup.Admin,
                        onClick = onSetUserAccess
                    )
                    ShareUsersList(
                        title = R.string.rooms_info_groups_title,
                        portal = portal,
                        groupedShareList = groupedShareList,
                        key = ShareGroup.Group,
                        onClick = onSetUserAccess
                    )
                    ShareUsersList(
                        title = R.string.rooms_info_users_title,
                        portal = portal,
                        groupedShareList = groupedShareList,
                        key = ShareGroup.User,
                        onClick = onSetUserAccess
                    )
                    ShareUsersList(
                        title = R.string.rooms_info_expected_title,
                        portal = portal,
                        groupedShareList = groupedShareList,
                        key = ShareGroup.Expected,
                        onClick = onSetUserAccess
                    )
                }
            }
        }
    }

    private fun copyLinkToClipboard(rootView: View, url: String, isCreate: Boolean) {
        KeyboardUtils.setDataToClipboard(requireContext(), url)
        UiUtils.getSnackBar(rootView).setText(
            if (!isCreate)
                R.string.rooms_info_copy_link_to_clipboard else
                R.string.rooms_info_create_link_complete
        ).show()
    }

    @Preview
    @Composable
    private fun RoomInfoScreenPreview() {
        val link = ExternalLink(
            access = 2,
            isLocked = false,
            isOwner = false,
            canEditAccess = false,
            sharedTo = ExternalLinkSharedTo(
                id = "",
                title = "Shared link",
                shareLink = "",
                linkType = 2,
                denyDownload = false,
                isExpired = false,
                primary = true,
                requestToken = "",
                password = "",
                expirationDate = ""
            )
        )

        ManagerTheme {
            RoomInfoScreen(
                roomTitle = "Room title",
                roomType = ApiContract.RoomType.CUSTOM_ROOM,
                canEditRoom = true,
                portal = "",
                state = RoomInfoState(
                    isLoading = false,
                    sharedLinks = listOf(
                        link.copy(sharedTo = link.sharedTo.copy(title = "Shared link 1", expirationDate = "123")),
                        link.copy(sharedTo = link.sharedTo.copy(title = "Shared link 2", password = "123")),
                        link.copy(sharedTo = link.sharedTo.copy(title = "Shared link 3", isExpired = true)),
                    ),
                    shareList = listOf(
                        Share(access = "1", sharedTo = SharedTo(displayName = "User 1"), isOwner = true),
                        Share(access = "5", sharedTo = SharedTo(name = "Group 2"), subjectType = 2),
                        Share(access = "9", sharedTo = SharedTo(displayName = "User 2")),
                        Share(access = "11", sharedTo = SharedTo(displayName = "User 3")),
                        Share(access = "10", sharedTo = SharedTo(displayName = "User 4")),
                        Share(access = "10", sharedTo = SharedTo(displayName = "User 4", activationStatus = 2)),
                    )
                ),
                onBackClick = {},
                onAddUsers = {},
                onSetUserAccess = { _, _, _ -> },
                onSharedLinkCreate = {},
                onLinkClick = {}
            )
        }
    }
}