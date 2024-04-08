package app.editors.manager.ui.fragments.share.link

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.viewModels.link.SharedLinkSettingsEffect
import app.editors.manager.viewModels.link.SharedLinkSettingsViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.VerticalSpacer
import lib.editors.gbase.ui.fragments.base.NestedColumn
import lib.toolkit.base.managers.utils.TimeUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SharedLinkSettingsScreen(viewModel: SharedLinkSettingsViewModel, onSnackBar: (String) -> Unit, onBack: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val state = viewModel.state.collectAsState()
    val loading = viewModel.loading.collectAsState()

    LaunchedEffect(viewModel) {
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
            SharedLinkSettingsScreen(
                loading = loading,
                state = state,
                onSetInternal = viewModel::setInternal,
                onDeleteLink = viewModel::delete,
                onRegenerateLink = viewModel::regenerate,
                onBack = onBack,
                onAccessClick = { navController.navigate(Route.AccessScreen.name) }
            )
        }
        composable(Route.AccessScreen.name) {
            ShareAccessScreen(
                currentAccess = state.value.access,
                onBack = navController::popBackStack,
                onSetUserAccess = viewModel::setAccess
            )
        }
    }
}

@Composable
private fun SharedLinkSettingsScreen(
    loading: State<Boolean>,
    state: State<ExternalLink>,
    onAccessClick: () -> Unit,
    onSetInternal: (Boolean) -> Unit,
    onDeleteLink: () -> Unit,
    onRegenerateLink: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = R.string.rooms_share_shared_link_title,
                backListener = onBack
            )
        }
    ) {
        NestedColumn {
            val context = LocalContext.current

            if (loading.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                VerticalSpacer(height = 4.dp)
            }

            AppHeaderItem(title = R.string.rooms_share_general_header)
            AppArrowItem(
                title = R.string.rooms_share_access_rights,
                optionImage = ManagerUiUtils.getAccessIcon(state.value.access),
                enabled = !state.value.sharedTo.isExpired,
                onClick = onAccessClick
            )
            AppHeaderItem(title = R.string.rooms_info_time_limit_title)
            AppArrowItem(
                title = stringResource(id = R.string.rooms_info_valid_through),
                enabled = !state.value.sharedTo.isExpired,
                option = TimeUtils.parseDate(state.value.sharedTo.expirationDate)?.let {
                    SimpleDateFormat
                        .getDateTimeInstance(
                            DateFormat.LONG,
                            DateFormat.SHORT,
                            TimeUtils.getCurrentLocale(context) ?: Locale.getDefault()
                        )
                        .format(it)
                } ?: "-",
                onClick = onAccessClick
            )
            AppHeaderItem(title = R.string.filter_title_type)
            AppSelectItem(
                title = R.string.rooms_share_shared_to_docsspace_users,
                selected = state.value.sharedTo.internal == true,
                enabled = !state.value.sharedTo.isExpired,
                onClick = { onSetInternal(true) }
            )
            AppSelectItem(
                title = R.string.rooms_share_shared_to_anyone,
                selected = state.value.sharedTo.internal == false,
                enabled = !state.value.sharedTo.isExpired,
                onClick = { onSetInternal(false) }
            )

            if (state.value.sharedTo.isExpired) {
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 8.dp),
                    text = R.string.rooms_info_link_expired_full,
                    color = MaterialTheme.colors.error
                )
                AppTextButton(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
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

@Preview(locale = "ru")
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
        SharedLinkSettingsScreen(
            loading = remember { mutableStateOf(false) },
            state = remember { mutableStateOf(link) },
            onSetInternal = {},
            onBack = {},
            onRegenerateLink = {},
            onDeleteLink = {},
            onAccessClick = {}
        )
    }
}