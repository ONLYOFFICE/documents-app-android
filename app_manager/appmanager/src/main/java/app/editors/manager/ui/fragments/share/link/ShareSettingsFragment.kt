package app.editors.manager.ui.fragments.share.link

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.viewModels.link.ShareSettingsEffect
import app.editors.manager.viewModels.link.ShareSettingsState
import app.editors.manager.viewModels.link.ShareSettingsViewModel
import app.editors.manager.viewModels.link.SharedLinkSettingsViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppCircularProgress
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Route(val name: String) {

    data object SettingsScreen : Route("settings")
    data object LinkSettingsScreen : Route("link_settings")
    data object AccessScreen : Route("access")
    data object LifeTimeScreen : Route("life_time")
}

class ShareSettingsFragment : ComposeDialogFragment() {

    companion object {

        private const val KEY_FILE_ID = "KEY_FILE_ID"
        private val TAG = ShareSettingsFragment::class.simpleName

        private fun newInstance(): ShareSettingsFragment = ShareSettingsFragment()

        fun show(activity: FragmentActivity, fileId: String?) {
            newInstance()
                .putArgs(KEY_FILE_ID to fileId)
                .show(activity.supportFragmentManager, TAG)
        }
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val snackBar = remember { UiUtils.getSnackBar(requireView()) }
            val fileId = remember { arguments?.getString(KEY_FILE_ID).orEmpty() }
            val viewModel = viewModel { ShareSettingsViewModel(requireContext().roomProvider, fileId) }
            val navController = rememberNavController()

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.route == Route.SettingsScreen.name) {
                    viewModel.fetchLinks()
                }
            }

            BackHandler {
                dismiss()
            }

            NavHost(navController = navController, startDestination = Route.SettingsScreen.name) {
                composable(Route.SettingsScreen.name) {
                    MainScreen(
                        viewModel = viewModel,
                        onSnackBar = { text -> snackBar.setText(text).show() },
                        onShare = { link ->
                            requireContext().openSendTextActivity(
                                getString(R.string.toolbar_menu_main_share),
                                link
                            )
                        },
                        onLinkClick = { link ->
                            val json = URLEncoder.encode(Json.encodeToString(link), Charsets.UTF_8.toString())
                            navController.navigate(
                                "${Route.LinkSettingsScreen.name}?" +
                                        "link=$json&" +
                                        "expired=${link.sharedTo.expirationDate}"
                            )
                        },
                        onBack = ::dismiss
                    )
                }
                composable(
                    route = "${Route.LinkSettingsScreen.name}?link={link}&expired={expired}",
                    arguments = listOf(
                        navArgument("link") {
                            type = NavType.StringType
                        },
                        navArgument("expired") {
                            type = NavType.StringType
                            nullable = true
                        }
                    )
                ) {
                    val json = URLDecoder.decode(it.arguments?.getString("link"), Charsets.UTF_8.toString())
                    SharedLinkSettingsScreen(
                        viewModel = viewModel {
                            SharedLinkSettingsViewModel(
                                externalLink = Json.decodeFromString<ExternalLink>(json),
                                expired = it.arguments?.getString("expired"),
                                roomProvider = requireContext().roomProvider,
                                fileId = fileId
                            )
                        },
                        onBack = navController::popBackStack,
                        onSnackBar = { text -> snackBar.setText(text).show() }
                    )
                }
            }
        }
    }

}

@Composable
private fun MainScreen(
    viewModel: ShareSettingsViewModel,
    onSnackBar: (String) -> Unit,
    onShare: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isCreateLoading by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ShareSettingsEffect.Copy -> {
                    KeyboardUtils.setDataToClipboard(context, effect.link)
                    onSnackBar(context.getString(R.string.rooms_info_create_link_complete))
                }
                is ShareSettingsEffect.Error -> {
                    onSnackBar(
                        effect.code?.let { code ->
                            context.getString(R.string.errors_client_error) + code
                        } ?: context.getString(R.string.errors_unknown_error)
                    )
                }
                is ShareSettingsEffect.OnCreate -> {
                    isCreateLoading = effect.loading
                }
                else -> Unit
            }
        }
    }

    ShareSettingsScreen(
        state = state,
        onBack = onBack,
        isCreateLoading = isCreateLoading,
        onCreate = viewModel::create,
        onShareClick = onShare,
        onLinkClick = onLinkClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShareSettingsScreen(
    isCreateLoading: Boolean,
    state: ShareSettingsState,
    onCreate: () -> Unit,
    onShareClick: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AppTopBar(title = R.string.share_title_main, backListener = onBack)
        }
    ) {
        when (state) {
            is ShareSettingsState.Loading -> LoadingPlaceholder()
            is ShareSettingsState.Success -> {
                LazyColumn {
                    item {
                        Row {
                            AppHeaderItem(
                                modifier = Modifier.weight(1f),
                                title = stringResource(id = R.string.rooms_share_shared_links)
                            )
                            if (state.links.isNotEmpty()) {
                                IconButton(onClick = onCreate) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_action_button_docs_add),
                                        tint = MaterialTheme.colors.primary,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                    if (state.links.isNotEmpty()) {
                        items(state.links, key = { it.sharedTo.id }) { link ->
                            SharedLinkItem(
                                modifier = Modifier.animateItemPlacement(),
                                access = link.access,
                                internal = link.sharedTo.internal == true,
                                expirationDate = link.sharedTo.expirationDate,
                                isExpired = link.sharedTo.isExpired,
                                onShareClick = { onShareClick.invoke(link.sharedTo.shareLink) },
                                onClick = { onLinkClick(link) }
                            )
                        }
                    } else if (!isCreateLoading) {
                        item {
                            AppTextButton(
                                modifier = Modifier.padding(start = 8.dp),
                                title = R.string.rooms_info_create_link,
                                onClick = onCreate
                            )
                        }
                    }
                    if (isCreateLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_onehalf_line_height))
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AppCircularProgress()
                            }
                        }
                    }
                    item {
                        AppDescriptionItem(text = R.string.rooms_share_shared_desc)
                    }
                }
            }
            else -> Unit
        }
    }
}

@Preview
@Composable
private fun ShareSettingsScreenPreview() {
    val link = ExternalLink(
        access = 2,
        isLocked = false,
        isOwner = false,
        canEditAccess = false,
        sharedTo = ExternalLinkSharedTo(
            id = "",
            title = "",
            shareLink = "",
            linkType = 2,
            internal = true,
            denyDownload = false,
            isExpired = false,
            primary = true,
            requestToken = "",
            password = "",
            expirationDate = "2024-4-05T22:00:00.0000000+03:00"
        )
    )

    ManagerTheme {
        ShareSettingsScreen(
            state = ShareSettingsState.Success(
                listOf(
                    link.copy(access = 1),
                    link.copy(sharedTo = link.sharedTo.copy(expirationDate = null)),
                    link.copy(sharedTo = link.sharedTo.copy(isExpired = true, internal = false))
                )
            ),
            isCreateLoading = false,
            onBack = {},
            onCreate = {},
            onShareClick = {},
            onLinkClick = {}
        )
    }
}