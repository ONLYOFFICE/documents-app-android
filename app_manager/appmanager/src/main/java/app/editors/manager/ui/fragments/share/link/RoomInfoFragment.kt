package app.editors.manager.ui.fragments.share.link

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.AccessTarget
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.ShareEntity
import app.documents.core.network.share.models.SharedTo
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.titleWithCount
import app.editors.manager.ui.fragments.share.InviteUsersScreen
import app.editors.manager.viewModels.link.ChangeUserAccessViewModel
import app.editors.manager.viewModels.link.RoomInfoEffect
import app.editors.manager.viewModels.link.RoomInfoState
import app.editors.manager.viewModels.link.RoomInfoViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs

class RoomInfoFragment : ComposeDialogFragment() {

    companion object {

        private const val KEY_ROOM = "key_room"
        val TAG: String = RoomInfoFragment::class.java.simpleName

        fun newInstance(room: CloudFolder): RoomInfoFragment =
            RoomInfoFragment().putArgs(KEY_ROOM to room)

    }

    private sealed interface Screens {
        @Serializable
        data object RoomInfo : Screens

        @Serializable
        data class ChangeUserAccess(
            val id: String,
            val access: Int,
            val removable: Boolean,
            val isOwnerOrAdmin: Boolean
        ) : Screens

        @Serializable
        data class ChangeGroupAccess(
            val id: String,
            val access: Int,
        ) : Screens

        @Serializable
        data class LinkSettings(
            val link: String? // null if create
        ) : Screens

        @Serializable
        data object InviteUsers : Screens
    }

    private val room: CloudFolder by lazy {
        arguments?.getSerializableExt(KEY_ROOM) ?: CloudFolder()
    }

    private val shareData: ShareData by lazy {
        ShareData.from(room, room.roomType, room.denyDownload)
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val portal = remember { requireContext().accountOnline?.portal }
            val canEditRoom = room.security?.editRoom == true
            val viewModel = viewModel { RoomInfoViewModel(requireContext().roomProvider, room.id) }
            val navController = rememberNavController().also {
                it.addOnDestinationChangedListener { _, destination, _ ->
                    if (destination.hasRoute(Screens.RoomInfo::class)) {
                        viewModel.fetchRoomInfo()
                    }
                }
            }
            val state by viewModel.state.collectAsState()

            BackHandler {
                dismiss()
            }

            LaunchedEffect(Unit) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is RoomInfoEffect.Create -> {
                            copyLinkToClipboard(requireView(), effect.url)
                        }

                        is RoomInfoEffect.Error -> {
                            UiUtils.getShortSnackBar(requireView())
                                .setText(effect.message)
                                .show()
                        }
                    }
                }
            }

            Surface(color = MaterialTheme.colors.background) {
                NavHost(
                    navController = navController,
                    startDestination = Screens.RoomInfo
                ) {
                    composable<Screens.RoomInfo> {
                        RoomInfoScreen(
                            state = state,
                            canEditRoom = canEditRoom,
                            roomType = room.roomType,
                            roomTitle = room.title,
                            portal = portal?.url.orEmpty(),
                            onBackClick = ::dismiss,
                            onAddUsers = { navController.navigate(Screens.InviteUsers) },
                            onChangeMemberAccess = { share ->
                                if ((share as? Share)?.isGroup == true) {
                                    navController.navigate(
                                        Screens.ChangeGroupAccess(
                                            id = share.sharedTo.id,
                                            access = share.access.code
                                        )
                                    )
                                } else {
                                    navController.navigate(
                                        Screens.ChangeUserAccess(
                                            id = share.sharedTo.id,
                                            access = share.access.code,
                                            removable = true,
                                            isOwnerOrAdmin = share.isOwnerOrAdmin
                                        )
                                    )
                                }
                            },
                            onSharedLinkCreate = {
                                navController.navigate(Screens.LinkSettings(null))
                            },
                            onLinkClick = { link ->
                                val json = Json.encodeToString(link)
                                navController.navigate(Screens.LinkSettings(json))
                            },
                        )
                    }
                    composable<Screens.LinkSettings> { backStackEntry ->
                        val json = backStackEntry.toRoute<Screens.LinkSettings>().link
                        ExternalLinkSettingsScreen(
                            link = json?.let { Json.decodeFromString<ExternalLink>(it) },
                            shareData = shareData,
                            onBackListener = navController::popBackStackWhenResumed
                        )
                    }
                    composable<Screens.ChangeUserAccess> { backStackEntry ->
                        val data = backStackEntry.toRoute<Screens.ChangeUserAccess>()
                        ChangeUserAccessScreen(
                            accessList = shareData.getAccessList(AccessTarget.User, data.removable),
                            portal = remember { requireContext().accountOnline?.portalUrl.orEmpty() },
                            onBack = navController::popBackStackWhenResumed,
                            onChangeAccess = { newAccess ->
                                viewModel.setUserAccess(
                                    room.id,
                                    data.id,
                                    newAccess
                                )
                            },
                            currentAccess = Access.get(data.access)
                        )
                    }
                    composable<Screens.ChangeGroupAccess> { backStackEntry ->
                        val data = backStackEntry.toRoute<Screens.ChangeGroupAccess>()
                        val lifecycleOwner = LocalLifecycleOwner.current

                        val changeUserAccessViewModel = viewModel {
                            ChangeUserAccessViewModel(
                                roomProvider = requireContext().roomProvider,
                                roomId = room.id,
                                groupId = data.id
                            )
                        }

                        val groupUsers = changeUserAccessViewModel.users.collectAsState()

                        LaunchedEffect(Unit) {
                            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                changeUserAccessViewModel.fetchUsers()
                            }
                        }

                        ChangeUserAccessScreen(
                            users = groupUsers.value,
                            portal = remember { requireContext().accountOnline?.portalUrl.orEmpty() },
                            onBack = navController::popBackStackWhenResumed,
                            currentAccess = Access.get(data.access),
                            accessList = shareData.getAccessList(AccessTarget.Group, true),
                            onChangeAccess = { newAccess ->
                                viewModel.setUserAccess(room.id, data.id, newAccess)
                            },
                            onUserClick = { share ->
                                navController.navigate(
                                    Screens.ChangeUserAccess(
                                        id = share.sharedTo.id,
                                        access = share.access.code,
                                        removable = false,
                                        isOwnerOrAdmin = share.isOwnerOrAdmin
                                    )
                                )
                            }
                        )
                    }

                    composable<Screens.InviteUsers> {
                        InviteUsersScreen(
                            roomType = room.roomType,
                            roomId = room.id,
                            roomProvider = requireContext().roomProvider,
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

    @Composable
    private fun RoomInfoScreen(
        state: RoomInfoState,
        canEditRoom: Boolean,
        roomType: Int?,
        roomTitle: String?,
        portal: String?,
        onChangeMemberAccess: (ShareEntity) -> Unit,
        onAddUsers: () -> Unit,
        onBackClick: () -> Unit,
        onLinkClick: (ExternalLink) -> Unit,
        onSharedLinkCreate: () -> Unit
    ) {
        BackHandler(onBack = onBackClick)

        AppScaffold(
            useTablePaddings = false,
            topBar = {
                Column {
                    AppTopBar(
                        title = {
                            Column {
                                Text(
                                    text = roomTitle
                                        ?: stringResource(id = R.string.list_context_info)
                                )
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
                    AnimatedVisibilityVerticalFade(visible = state.requestLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        ) {
            if (state.isLoading) {
                LoadingPlaceholder()
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val groupedShareList = state.shareList.groupBy(Share::roomAccessType)
                    if (ApiContract.RoomType.hasExternalLink(roomType)) {
                        ExternalLinkBlock(
                            sharedLinks = state.sharedLinks,
                            roomType = roomType,
                            canEditRoom = canEditRoom,
                            onLinkClick = onLinkClick,
                            onSharedLinkCreate = onSharedLinkCreate
                        )
                    }
                    groupedShareList.forEach { (shareType, shareList) ->
                        ShareUsersList(
                            isRoom = true,
                            portal = portal,
                            shareList = shareList,
                            title = shareType.titleWithCount,
                            onClick = { share -> onChangeMemberAccess(share) }
                        )
                    }
                }
            }
        }
    }

    private fun copyLinkToClipboard(rootView: View, url: String) {
        KeyboardUtils.setDataToClipboard(requireContext(), url)
        UiUtils.getSnackBar(rootView)
            .setText(R.string.rooms_info_create_link_complete)
            .show()
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
                roomType = ApiContract.RoomType.FILL_FORMS_ROOM,
                canEditRoom = true,
                portal = "",
                state = RoomInfoState(
                    isLoading = false,
                    sharedLinks = listOf(
                        link.copy(
                            sharedTo = link.sharedTo.copy(
                                title = "Shared link 1",
                                expirationDate = "123"
                            )
                        ),
                        link.copy(
                            sharedTo = link.sharedTo.copy(
                                title = "Shared link 2",
                                password = "123"
                            )
                        ),
                        link.copy(
                            sharedTo = link.sharedTo.copy(
                                title = "Shared link 3",
                                isExpired = true
                            )
                        ),
                    ),
                    shareList = listOf(
                        Share(
                            _access = Access.Comment.code,
                            sharedTo = SharedTo(displayName = "User 1"),
                            isOwner = true
                        ),
                        Share(
                            _access = Access.Read.code,
                            sharedTo = SharedTo(name = "Group 2"),
                            subjectType = 2
                        ),
                        Share(
                            _access = Access.Read.code,
                            sharedTo = SharedTo(displayName = "User 2")
                        ),
                        Share(
                            _access = Access.Editor.code,
                            sharedTo = SharedTo(displayName = "User 3")
                        ),
                        Share(
                            _access = Access.Editor.code,
                            sharedTo = SharedTo(displayName = "User 4")
                        ),
                        Share(
                            _access = Access.Editor.code,
                            sharedTo = SharedTo(displayName = "User 4", activationStatus = 2)
                        ),
                    )
                ),
                onBackClick = {},
                onAddUsers = {},
                onChangeMemberAccess = { },
                onSharedLinkCreate = {},
                onLinkClick = {},
            )
        }
    }
}