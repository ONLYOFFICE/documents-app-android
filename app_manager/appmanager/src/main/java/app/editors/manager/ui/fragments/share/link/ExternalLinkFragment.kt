package app.editors.manager.ui.fragments.share.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.ShareGroup
import app.documents.core.network.share.models.SharedTo
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.link.ExternalLinkEffect
import app.editors.manager.viewModels.link.ExternalLinkState
import app.editors.manager.viewModels.link.ExternalLinkViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import lib.compose.ui.addIf
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.putArgs

class ExternalLinkFragment : BaseAppFragment() {

    companion object {

        private const val KEY_ROOM_ID = "key_room_id"
        private const val KEY_ROOM_TITLE = "key_room_type"
        private const val KEY_ROOM_TYPE = "key_room_title"

        private fun newInstance(id: String, type: Int, title: String): ExternalLinkFragment =
            ExternalLinkFragment().putArgs(
                KEY_ROOM_ID to id,
                KEY_ROOM_TYPE to type,
                KEY_ROOM_TITLE to title,
            )

        fun show(fragmentManager: FragmentManager, id: String, type: Int, title: String) {
            FragmentUtils.showFragment(
                fragmentManager = fragmentManager,
                fragment = newInstance(id, type, title),
                frameId = android.R.id.content,
                tag = TAG,
                isAdd = true
            )
        }
    }

    enum class RoomInfoScreens {
        RoomInfo, UserAccess
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {
            ManagerTheme {
                val scaffoldState = rememberScaffoldState()
                val navController = rememberNavController()
                val viewModel = viewModel { ExternalLinkViewModel(requireContext().roomProvider) }
                val state by viewModel.state.collectAsState()
                val roomId = remember { arguments?.getString(KEY_ROOM_ID) }
                val roomType = remember { arguments?.getInt(KEY_ROOM_TYPE) }
                val roomTitle = remember { arguments?.getString(KEY_ROOM_TITLE) }

                LaunchedEffect(Unit) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is ExternalLinkEffect.Error -> {
                                UiUtils.getShortSnackBar(requireView())
                                    .setText(effect.message)
                                    .show()
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.fetchRoomInfo(roomId.orEmpty())
                }

                BackHandler {
                    parentFragmentManager.popBackStack()
                }

                Surface(color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = RoomInfoScreens.RoomInfo.name) {
                        composable(route = RoomInfoScreens.RoomInfo.name) {
                            RoomInfoScreen(
                                scaffoldState = scaffoldState,
                                state = state,
                                roomType = roomType,
                                roomTitle = roomTitle,
                                onBackClick = parentFragmentManager::popBackStack,
                                onSetUserAccess = { userId, access ->
                                    navController.navigate(
                                        RoomInfoScreens.UserAccess.name +
                                                "?=userId=$userId" +
                                                "&access=$access"
                                    )
                                }
                            )
                        }
                        composable(
                            route = "${RoomInfoScreens.UserAccess.name}?=userId={userId}&access={access}",
                            arguments = listOf(
                                navArgument("userId") { type = NavType.StringType },
                                navArgument("access") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            UserAccessScreen(
                                scaffoldState = scaffoldState,
                                viewModel = viewModel,
                                navController = navController,
                                roomId = roomId.orEmpty(),
                                userId = backStackEntry.arguments?.getString("userId").orEmpty(),
                                currentAccess = backStackEntry.arguments?.getInt("access")
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RoomInfoScreen(
        scaffoldState: ScaffoldState,
        state: ExternalLinkState,
        roomType: Int?,
        roomTitle: String?,
        onSetUserAccess: (userId: String, access: Int) -> Unit,
        onBackClick: () -> Unit
    ) {
        AppScaffold(
            scaffoldState = scaffoldState,
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
                        TopAppBarAction(icon = R.drawable.ic_add_users) {

                        }
                    },
                    backListener = onBackClick
                )
            }
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val groupedShareList = Share.groupByAccess(state.shareList)
                if (ApiContract.RoomType.hasExternalLink(roomType)) {
                    ExternalLinkBlock(state.generalLink, state.additionalLinks)
                }
                ShareUsersList(
                    title = R.string.rooms_info_admin_title,
                    shareList = groupedShareList[ShareGroup.Admin],
                    onClick = onSetUserAccess
                )
                ShareUsersList(
                    title = R.string.rooms_info_users_title,
                    shareList = groupedShareList[ShareGroup.User],
                    onClick = onSetUserAccess
                )
                ShareUsersList(
                    title = R.string.rooms_info_expected_title,
                    shareList = groupedShareList[ShareGroup.Expected],
                    onClick = onSetUserAccess
                )
            }
        }
    }

    @Composable
    private fun ExternalLinkBlock(generalLink: ExternalLink?, additionalLinks: List<ExternalLink>) {
        AppDescriptionItem(
            modifier = Modifier.padding(top = 8.dp),
            text = R.string.rooms_info_access_desc
        )
        if (generalLink != null) {
            AppHeaderItem(title = R.string.rooms_info_general_link)
            ExternalLinkItem(
                linkTitle = generalLink.sharedTo.title,
                accessCode = generalLink.accessCode,
                hasPassword = !generalLink.sharedTo.password.isNullOrEmpty(),
                expiring = true
            )
        }
        AppHeaderItem(title = stringResource(id = R.string.rooms_info_additional_links, 0, 5))
        additionalLinks.forEach { link ->
            ExternalLinkItem(
                linkTitle = link.sharedTo.title,
                accessCode = link.accessCode,
                hasPassword = !link.sharedTo.password.isNullOrEmpty(),
                expiring = !link.expirationDate.isNullOrEmpty()
            )
        }
        AppTextButton(
            modifier = Modifier.padding(start = 8.dp),
            title = R.string.rooms_info_create_link
        ) {}
    }

    @Composable
    fun ShareUsersList(title: Int, shareList: List<Share>?, onClick: (String, Int) -> Unit) {
        val context = LocalContext.current
        var visible by remember { mutableStateOf(true) }
        val portal = remember { context.accountOnline?.portal }

        if (!shareList.isNullOrEmpty()) {
            Column {
                Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.Bottom),
                        text = stringResource(id = title, shareList.size),
                        style = MaterialTheme.typography.subtitle2,
                        color = MaterialTheme.colors.colorTextSecondary
                    )
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { visible = !visible }
                    ) {
                        Icon(
                            modifier = Modifier.rotate(if (visible) 0f else 180f),
                            imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_arrow_down),
                            contentDescription = null,
                            tint = MaterialTheme.colors.colorTextTertiary
                        )
                    }
                }
                AnimatedVisibilityVerticalFade(visible = visible) {
                    Column {
                        shareList.forEach { share ->
                            ShareUserItem(share = share, portal = portal) {
                                onClick.invoke(share.sharedTo.id, share.intAccess)
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun ShareUserItem(share: Share, portal: String?, onClick: () -> Unit) {
        Column(modifier = Modifier.addIf(share.canEditAccess) { clickable(onClick = onClick) }) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlideImage(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(CircleShape)
                        .size(40.dp)
                        .background(MaterialTheme.colors.colorTextTertiary),
                    model = "${ApiContract.SCHEME_HTTPS}$portal${share.sharedTo.avatarMedium}",
                    loading = placeholder(R.drawable.ic_account_placeholder),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.body1,
                    text = share.sharedTo.displayName
                )
                Text(
                    text = stringResource(id = RoomUtils.getAccessTitleOrOwner(share)),
                    style = MaterialTheme.typography.body2,
                    color = if (share.canEditAccess)
                        MaterialTheme.colors.colorTextSecondary else
                        MaterialTheme.colors.colorTextTertiary
                )
                if (share.canEditAccess) {
                    Icon(
                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = MaterialTheme.colors.colorTextTertiary
                    )
                }
            }
            AppDivider(startIndent = 16.dp + 40.dp + 16.dp)
        }
    }

    @Preview
    @Composable
    private fun ExternalLinkScreenPreview() {
        val link = ExternalLink(
            accessCode = 2,
            isLocked = false,
            isOwner = false,
            canEditAccess = false,
            denyDownload = false,
            sharedTo = ExternalLinkSharedTo(
                id = "",
                title = "",
                shareLink = "",
                linkType = 2,
                denyDownload = false,
                isExpired = false,
                primary = true,
                requestToken = "",
                password = ""
            ),
            expirationDate = ""
        )

        ManagerTheme {
            RoomInfoScreen(
                scaffoldState = rememberScaffoldState(),
                roomTitle = "Room title",
                roomType = 1,
                state = ExternalLinkState(
                    generalLink = link,
                    additionalLinks = listOf(link, link.copy(accessCode = 1)),
                    shareList = listOf(
                        Share(access = "1", sharedTo = SharedTo(displayName = "User 1"), isOwner = true),
                        Share(access = "9", sharedTo = SharedTo(displayName = "User 2")),
                        Share(access = "11", sharedTo = SharedTo(displayName = "User 3")),
                        Share(access = "10", sharedTo = SharedTo(displayName = "User 4")),
                        Share(access = "10", sharedTo = SharedTo(displayName = "User 4", activationStatus = 2)),
                    )
                ),
                onBackClick = {},
                onSetUserAccess = { _, _ -> }
            )
        }
    }
}