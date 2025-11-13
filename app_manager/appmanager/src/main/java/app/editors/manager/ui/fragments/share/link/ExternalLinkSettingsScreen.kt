package app.editors.manager.ui.fragments.share.link

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract.RoomType.FILL_FORMS_ROOM
import app.documents.core.network.common.contracts.ApiContract.RoomType.PUBLIC_ROOM
import app.documents.core.network.common.contracts.ApiContract.RoomType.VIRTUAL_ROOM
import app.documents.core.network.manager.models.explorer.AccessTarget
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.managers.utils.toUi
import app.editors.manager.mvp.models.ui.AccessUI
import app.editors.manager.viewModels.link.ExternalLinkSettingsEffect
import app.editors.manager.viewModels.link.ExternalLinkSettingsViewModel
import kotlinx.coroutines.delay
import lib.compose.ui.TouchDisable
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppPasswordTextField
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTextFieldListItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.DropdownMenuButton
import lib.compose.ui.views.DropdownMenuItem
import lib.toolkit.base.managers.utils.UiUtils

@Composable
fun ExternalLinkSettingsScreen(
    link: ExternalLink?,
    shareData: ShareData,
    onBackListener: () -> Unit,
) {
    val linkSharedTo = link?.sharedTo?.copy(
        denyDownload = shareData.denyDownload || link.sharedTo.denyDownload
    ) ?: ExternalLinkSharedTo(
        id = "",
        title = "",
        shareLink = "",
        linkType = 1,
        password = null,
        denyDownload = shareData.denyDownload,
        isExpired = false,
        primary = false,
        internal = false,
        requestToken = "",
        expirationDate = null
    )
    val accessOptions = remember { shareData.getAccessList(AccessTarget.ExternalLink) }
    val context = LocalContext.current
    val localView = LocalView.current
    val viewModel = viewModel {
        ExternalLinkSettingsViewModel(
            access = link?.access?.let(Access::get) ?: accessOptions.last().access,
            inputLink = linkSharedTo,
            shareData = shareData,
            roomProvider = context.roomProvider
        )
    }
    val state by viewModel.state.collectAsState()
    val isRevoke =
        linkSharedTo.primary && shareData.roomType in arrayOf(PUBLIC_ROOM, FILL_FORMS_ROOM)
    val passwordErrorState = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ExternalLinkSettingsEffect.Delete -> {
                    onBackListener.invoke()
                    UiUtils.getSnackBar(localView)
                        .setText(
                            if (isRevoke)
                                R.string.rooms_info_revoke_link_complete else
                                R.string.rooms_info_delete_link_complete
                        )
                        .show()
                }

                is ExternalLinkSettingsEffect.Save -> {
                    onBackListener.invoke()
                }

                is ExternalLinkSettingsEffect.Error -> {
                    UiUtils.getSnackBar(localView).setText(effect.message).show()
                }

                ExternalLinkSettingsEffect.PasswordForbiddenSymbols -> {
                    passwordErrorState.value =
                        context.getString(R.string.rooms_info_password_allowed_symbols)
                }

                ExternalLinkSettingsEffect.PasswordLength -> {
                    passwordErrorState.value =
                        context.getString(R.string.rooms_info_password_minimum_length)
                }
            }
        }
    }

    MainScreen(
        link = state.link,
        accessOptions = accessOptions,
        access = state.access,
        loading = state.loading,
        shareData = shareData,
        passwordErrorState = passwordErrorState,
        isCreate = link == null,
        isRevoke = isRevoke,
        onSetAccess = viewModel::setAccess,
        onBackListener = onBackListener,
        onDoneClick = if (link == null) viewModel::createRoomLink else viewModel::save,
        onDeleteOrRevokeLink = {
            if (shareData.isRoom) {
                UiUtils.showQuestionDialog(
                    context = context,
                    title = if (isRevoke) {
                        context.getString(R.string.rooms_info_revoke_link)
                    } else {
                        context.getString(R.string.rooms_info_delete_link)
                    },
                    description = if (isRevoke) {
                        context.getString(R.string.rooms_info_revoke_link_desc)
                    } else {
                        context.getString(R.string.rooms_info_delete_link_desc)
                    },
                    acceptTitle = if (isRevoke) {
                        context.getString(R.string.rooms_info_revoke)
                    } else {
                        context.getString(R.string.list_context_delete)
                    },
                    acceptErrorTint = true,
                    acceptListener = viewModel::deleteOrRevoke
                )
            } else {
                viewModel.deleteOrRevoke()
            }

        },
        updateViewState = viewModel::updateViewState
    )
}

@Composable
private fun MainScreen(
    link: ExternalLinkSharedTo,
    loading: Boolean,
    shareData: ShareData,
    accessOptions: List<AccessUI>,
    access: Access,
    passwordErrorState: MutableState<String?>,
    isCreate: Boolean,
    isRevoke: Boolean,
    onSetAccess: (Access) -> Unit,
    onBackListener: () -> Unit,
    onDoneClick: () -> Unit,
    onDeleteOrRevokeLink: () -> Unit,
    updateViewState: (ExternalLinkSharedTo.() -> ExternalLinkSharedTo) -> Unit,
) {
    ManagerTheme {

        BackHandler(onBack = onBackListener)

        AppScaffold(
            useTablePaddings = false,
            topBar = {
                Column {
                    AppTopBar(
                        title = if (isCreate)
                            R.string.rooms_info_create_link_title else
                            R.string.rooms_info_edit_link,
                        backListener = onBackListener,
                        actions = {
                            AppTextButton(
                                enabled = link.title.isNotEmpty() && !loading,
                                title = lib.toolkit.base.R.string.common_done,
                                onClick = onDoneClick
                            )
                        }
                    )
                    AnimatedVisibilityVerticalFade(visible = loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val title = remember { mutableStateOf(link.title) }

                TouchDisable(disableTouch = link.isExpired) {
                    Column {
                        AppHeaderItem(title = R.string.rooms_info_link_name)
                        AppTextFieldListItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            state = title,
                            hint = stringResource(id = lib.toolkit.base.R.string.text_hint_required),
                            fillMaxWidth = true,
                            onValueChange = { updateViewState { copy(title = title.value) } }
                        )

                        AppHeaderItem(title = R.string.rooms_share_general_header)
                        AppListItem(
                            title = stringResource(R.string.share_access_room_type),
                            endContent = {

                                if (shareData.roomType == FILL_FORMS_ROOM) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(Access.FormFiller.toUi().icon),
                                        contentDescription = null
                                    )
                                } else {
                                    val dropdownMenuShow = remember { mutableStateOf(false) }
                                    DropdownMenuButton(
                                        state = dropdownMenuShow,
                                        icon = ImageVector.vectorResource(access.toUi().icon),
                                        onDismiss = { dropdownMenuShow.value = false },
                                        items = {
                                            accessOptions.forEach { accessOption ->
                                                DropdownMenuItem(
                                                    title = stringResource(accessOption.title),
                                                    selected = access == accessOption.access,
                                                    startIcon = accessOption.icon,
                                                    onClick = {
                                                        dropdownMenuShow.value = false
                                                        onSetAccess(accessOption.access)
                                                    }
                                                )
                                            }
                                        }
                                    ) {
                                        dropdownMenuShow.value = true
                                    }
                                }
                            }
                        )

                        if (!link.primary || shareData.roomType !in listOf(
                                FILL_FORMS_ROOM,
                                PUBLIC_ROOM
                            )
                        ) {
                            LinkLifeTimeListItem(
                                expirationDate = link.expirationDate,
                                onSetLifeTime = {
                                    updateViewState { copy(expirationDate = it.getFormattedDateTime()) }
                                }
                            )
                        }

                        if (shareData.roomType !in listOf(VIRTUAL_ROOM, PUBLIC_ROOM)) {
                            AppHeaderItem(title = R.string.filter_title_type)
                            AppSelectItem(
                                title = R.string.rooms_share_shared_to_anyone,
                                selected = link.internal == false,
                                onClick = { updateViewState { copy(internal = false) } }
                            )
                            AppSelectItem(
                                title = R.string.rooms_share_shared_to_docsspace_users,
                                selected = link.internal == true,
                                onClick = { updateViewState { copy(internal = true) } }
                            )
                        }

                        ProtectionSection(
                            link = link,
                            shareData = shareData,
                            passwordErrorState = passwordErrorState,
                            updateViewState = updateViewState
                        )
                    }
                }

                if (!isCreate) {
                    AppTextButton(
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                        title = if (isRevoke)
                            R.string.rooms_info_revoke_link else
                            R.string.rooms_info_delete_link,
                        textColor = MaterialTheme.colors.error,
                        onClick = onDeleteOrRevokeLink,
                        enabled = !loading
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtectionSection(
    link: ExternalLinkSharedTo,
    shareData: ShareData,
    passwordErrorState: MutableState<String?>,
    updateViewState: (ExternalLinkSharedTo.() -> ExternalLinkSharedTo) -> Unit,
) {
    var showWarningDialog by remember { mutableStateOf(false) }
    val password = remember { mutableStateOf(link.password.orEmpty()) }
    val passwordEnabled = remember { mutableStateOf(!link.password.isNullOrEmpty()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(password.value) {
        if (password.value.isEmpty()) return@LaunchedEffect
        delay(500)
        updateViewState { copy(password = password.value) }
    }
    LaunchedEffect(passwordEnabled.value) {
        if (passwordEnabled.value && !link.isExpired) {
            delay(500)
            focusRequester.requestFocus()
        }
    }

    AppHeaderItem(title = R.string.context_protection_title)
    AppSwitchItem(
        title = R.string.rooms_info_password_access,
        checked = passwordEnabled.value,
        onCheck = { checked ->
            passwordEnabled.value = checked
            updateViewState { copy(password = "".takeIf { checked }) }
        }
    )
    AnimatedVisibilityVerticalFade(visible = passwordEnabled.value) {
        AppPasswordTextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            label = null,
            state = password,
            focusManager = LocalFocusManager.current,
            errorState = passwordErrorState
        )
    }

    if (shareData.roomType != FILL_FORMS_ROOM) {
        AppSwitchItem(
            title = R.string.rooms_info_file_rectrict,
            checked = link.denyDownload,
            singleLine = false,
            onCheck = {
                if (shareData.denyDownload) {
                    showWarningDialog = true
                } else {
                    updateViewState { copy(denyDownload = it) }
                }
            }
        )
        AppDescriptionItem(
            modifier = Modifier.padding(top = 8.dp),
            text = when {
                shareData.isRoom -> R.string.rooms_info_file_rectrict_desc
                shareData.isFolder -> R.string.share_link_folder_restrict_desc
                else -> R.string.share_link_file_restrict_desc
            }
        )
    }

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialogs_warning_title),
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.share_link_file_restrict_warning),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.colorTextSecondary
                )
            },
            confirmButton = {
                AppTextButton(title = R.string.share_link_file_restrict_warning_ok) {
                    showWarningDialog = false
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val link = ExternalLinkSharedTo(
        id = "",
        title = "Shared link",
        internal = true,
        shareLink = "",
        linkType = 2,
        denyDownload = false,
        isExpired = true,
        primary = false,
        requestToken = "",
        password = null,
        expirationDate = null
    )

    val shareTypeRoom = ShareData(
        itemId = "",
        roomType = PUBLIC_ROOM,
        isRoom = true
    )

    MainScreen(
        accessOptions = shareTypeRoom.getAccessList(AccessTarget.ExternalLink),
        link = link,
        shareData = shareTypeRoom,
        loading = true,
        passwordErrorState = remember { mutableStateOf(null) },
        access = Access.Editor,
        isCreate = false,
        isRevoke = true,
        onBackListener = {},
        onSetAccess = {},
        onDoneClick = {},
        onDeleteOrRevokeLink = {}) {}
}