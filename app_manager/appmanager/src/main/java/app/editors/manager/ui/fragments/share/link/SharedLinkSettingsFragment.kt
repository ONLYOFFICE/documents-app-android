package app.editors.manager.ui.fragments.share.link

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.documents.core.model.cloud.Access
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.toUi
import app.editors.manager.viewModels.link.SharedLinkSettingsEffect
import app.editors.manager.viewModels.link.SharedLinkSettingsViewModel
import lib.compose.ui.TouchDisable
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.DropdownMenuButton
import lib.compose.ui.views.DropdownMenuItem
import lib.compose.ui.views.NestedColumn
import lib.compose.ui.views.VerticalSpacer

@Composable
fun SharedLinkSettingsScreen(
    viewModel: SharedLinkSettingsViewModel,
    useTabletPadding: Boolean = false,
    onSnackBar: (String) -> Unit,
    onBack: () -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val state = viewModel.state.collectAsState()
    val loading = viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SharedLinkSettingsEffect.Close -> onBack()
                SharedLinkSettingsEffect.Delete -> {
                    onSnackBar.invoke(context.getString(R.string.rooms_info_delete_link_complete))
                    onBack.invoke()
                }

                is SharedLinkSettingsEffect.Error -> onSnackBar(
                    effect.code?.let { code ->
                        context.getString(R.string.errors_client_error) + code
                    } ?: context.getString(R.string.errors_unknown_error)
                )
            }
        }
    }

    NavHost(navController = navController, startDestination = Route.LinkSettingsScreen.name) {
        composable(Route.LinkSettingsScreen.name) {
            MainScreen(
                loading = loading,
                state = state,
                useTabletPadding = useTabletPadding,
                onSetInternal = viewModel::setInternal,
                onDeleteLink = viewModel::delete,
                onRegenerateLink = viewModel::regenerate,
                onBack = onBack,
                onSetLifeTime = viewModel::setLifeTime,
                onSetAccess = viewModel::setAccess
            )
        }
    }
}

@Composable
private fun MainScreen(
    loading: State<Boolean>,
    state: State<ExternalLink>,
    useTabletPadding: Boolean = false,
    onSetInternal: (Boolean) -> Unit,
    onDeleteLink: () -> Unit,
    onRegenerateLink: () -> Unit,
    onSetAccess: (Int) -> Unit,
    onSetLifeTime: (SharedLinkLifeTime) -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(
        useTablePaddings = useTabletPadding,
        topBar = {
            AppTopBar(
                title = R.string.rooms_share_shared_link_title,
                backListener = onBack
            )
        }
    ) {
        TouchDisable(disableTouch = loading.value) {
            NestedColumn {
                val context = LocalContext.current
                val accessDropDownState = remember { mutableStateOf(false) }

                if (loading.value) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    VerticalSpacer(height = 4.dp)
                }

                TouchDisable(disableTouch = state.value.sharedTo.isExpired) {
                    Column {
                        AppHeaderItem(title = R.string.rooms_share_general_header)
                        AppListItem(
                            title = stringResource(R.string.rooms_share_access_rights),
                            endContent = {
                                DropdownMenuButton(
                                    state = accessDropDownState,
                                    icon = ImageVector.vectorResource(Access.get(state.value.access).toUi().icon),
                                    items = {
                                        RoomUtils.getLinkAccessOptions().forEach { access ->
                                            val accessUi = access.toUi()
                                            DropdownMenuItem(
                                                title = stringResource(accessUi.title),
                                                selected = access.code == state.value.access,
                                                startIcon = accessUi.icon,
                                                onClick = {
                                                    onSetAccess(access.code)
                                                    accessDropDownState.value = false
                                                }
                                            )
                                        }
                                    },
                                    onDismiss = { accessDropDownState.value = false }
                                ) {
                                    accessDropDownState.value = true
                                }
                            }
                        )

                        AppHeaderItem(title = R.string.rooms_info_time_limit_title)
                        LinkLifeTimeListItem(
                            expirationDate = state.value.sharedTo.expirationDate,
                            onSetLifeTime = onSetLifeTime
                        )

                        AppHeaderItem(title = R.string.filter_title_type)
                        AppSelectItem(
                            title = R.string.rooms_share_shared_to_docsspace_users,
                            selected = state.value.sharedTo.internal == true,
                            onClick = { onSetInternal(true) }
                        )
                        AppSelectItem(
                            title = R.string.rooms_share_shared_to_anyone,
                            selected = state.value.sharedTo.internal == false,
                            onClick = { onSetInternal(false) }
                        )
                    }

                }
                if (state.value.sharedTo.isExpired) {
                    AppDescriptionItem(
                        modifier = Modifier.padding(top = 8.dp),
                        text = R.string.rooms_info_link_expired_full,
                        color = MaterialTheme.colors.error
                    )

                }
                VerticalSpacer(16.dp)
                if (state.value.sharedTo.isExpired) {
                    AppTextButton(
                        modifier = Modifier.padding(start = 8.dp),
                        title = R.string.rooms_share_regenerate_link,
                        onClick = onRegenerateLink
                    )
                }
                AppTextButton(
                    modifier = Modifier.padding(start = 8.dp),
                    title = R.string.rooms_info_delete_link,
                    textColor = MaterialTheme.colors.error,
                    onClick = onDeleteLink
                )
            }
        }
    }
}

@Preview(locale = "ru")
@Preview()
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
            isExpired = true,
            primary = true,
            requestToken = "",
            password = "",
            expirationDate = "2024-12-10T22:00:00.0000000+03:00"
        )
    )

    ManagerTheme {
        MainScreen(
            useTabletPadding = false,
            loading = remember { mutableStateOf(false) },
            state = remember { mutableStateOf(link) },
            onSetInternal = {},
            onBack = {},
            onRegenerateLink = {},
            onDeleteLink = {},
            onSetAccess = {},
            onSetLifeTime = {}
        )
    }
}