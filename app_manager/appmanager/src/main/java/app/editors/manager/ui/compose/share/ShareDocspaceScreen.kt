package app.editors.manager.ui.compose.share

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.ScaffoldState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.fragments.share.link.Route
import app.editors.manager.ui.fragments.share.link.SharedLinkItem
import app.editors.manager.ui.fragments.share.link.SharedLinkSettingsScreen
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
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.utils.KeyboardUtils
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun ShareDocSpaceScreen(
    viewModel: ShareSettingsViewModel,
    fileExtension: String?, // null for folders
    useTabletPadding: Boolean,
    onSendLink: (String) -> Unit,
    onClose: () -> Unit,
) {
    val navController = rememberNavController()

    navController.addOnDestinationChangedListener { _, destination, _ ->
        if (destination.route == Route.SettingsScreen.name) {
            viewModel.fetchLinks()
        }
    }

    BackHandler(onBack = onClose)

    NavHost(
        modifier = Modifier.background(MaterialTheme.colors.background),
        navController = navController,
        startDestination = Route.SettingsScreen.name
    ) {
        composable(Route.SettingsScreen.name) {
            MainScreen(
                viewModel = viewModel,
                useTabletPaddings = useTabletPadding,
                isFolder = fileExtension == null,
                onShare = onSendLink,
                onBack = onClose,
                onLinkClick = { link ->
                    val json = URLEncoder.encode(Json.encodeToString(link), Charsets.UTF_8.toString())
                    navController.navigate(
                        "${Route.LinkSettingsScreen.name}?" +
                                "link=$json&" +
                                "expired=${link.sharedTo.expirationDate}"
                    )
                }
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
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    SharedLinkSettingsViewModel(
                        externalLink = Json.decodeFromString<ExternalLink>(json),
                        expired = it.arguments?.getString("expired"),
                        roomProvider = viewModel.roomProvider,
                        itemId = viewModel.itemId,
                        isFolder = fileExtension == null
                    )
                },
                fileExtension = fileExtension,
                useTabletPadding = useTabletPadding,
                onBack = navController::popBackStack,
            )
        }
    }
}


@Composable
private fun MainScreen(
    viewModel: ShareSettingsViewModel,
    isFolder: Boolean,
    useTabletPaddings: Boolean,
    onShare: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var isCreateLoading by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ShareSettingsEffect.Copy -> {
                    KeyboardUtils.setDataToClipboard(context, effect.link)
                    scaffoldState.snackbarHostState
                        .showSnackbar(context.getString(R.string.rooms_info_create_link_complete))
                }

                is ShareSettingsEffect.Error -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        effect.code?.let { code ->
                            context.getString(R.string.errors_client_error) + code
                        } ?: context.getString(R.string.errors_unknown_error)
                    )
                }

                is ShareSettingsEffect.OnCreate -> {
                    isCreateLoading = effect.loading
                }
            }
        }
    }

    ShareSettingsScreen(
        scaffoldState = scaffoldState,
        state = state,
        isFolder = isFolder,
        onBack = onBack,
        useTabletPaddings = useTabletPaddings,
        isCreateLoading = isCreateLoading,
        onCreate = viewModel::create,
        onShareClick = onShare,
        onLinkClick = onLinkClick
    )
}

@Composable
private fun ShareSettingsScreen(
    scaffoldState: ScaffoldState,
    isCreateLoading: Boolean,
    isFolder: Boolean,
    state: ShareSettingsState,
    useTabletPaddings: Boolean,
    onCreate: () -> Unit,
    onShareClick: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(
        scaffoldState = scaffoldState,
        useTablePaddings = useTabletPaddings,
        topBar = {
            AppTopBar(
                title = R.string.share_title_main,
                backListener = onBack,
                actions = {
                    TopAppBarAction(
                        icon = R.drawable.ic_add_users,
                        onClick = {}
                    )
                }
            )
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
                                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_default_add),
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
                                modifier = Modifier.animateItem(),
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
                        AppDescriptionItem(
                            modifier = Modifier.padding(top = 8.dp),
                            text = if (!isFolder) {
                                R.string.rooms_share_shared_desc
                            } else {
                                R.string.rooms_share_shared_folder_desc
                            }
                        )
                    }
                }
            }
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
            id = "1",
            title = "",
            shareLink = "",
            linkType = 2,
            internal = true,
            denyDownload = false,
            isExpired = false,
            primary = true,
            requestToken = "",
            password = "",
            expirationDate = null
        )
    )

    ManagerTheme {
        ShareSettingsScreen(
            scaffoldState = rememberScaffoldState(),
            state = ShareSettingsState.Success(
                listOf(
                    link.copy(access = 1),
                    link.copy(sharedTo = link.sharedTo.copy(expirationDate = null, id = "2")),
                    link.copy(sharedTo = link.sharedTo.copy(isExpired = true, internal = false, id = "3"))
                )
            ),
            isFolder = true,
            useTabletPaddings = false,
            isCreateLoading = false,
            onBack = {},
            onCreate = {},
            onShareClick = {},
            onLinkClick = {}
        )
    }
}