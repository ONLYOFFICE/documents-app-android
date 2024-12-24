package app.editors.manager.ui.fragments.share.link

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.contracts.ApiContract.RoomType.FILL_FORMS_ROOM
import app.documents.core.network.common.contracts.ApiContract.RoomType.PUBLIC_ROOM
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.viewModels.link.ExternalLinkSettingsEffect
import app.editors.manager.viewModels.link.ExternalLinkSettingsViewModel
import kotlinx.coroutines.delay
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppPasswordTextField
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTextFieldListItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.DropdownMenuButton
import lib.compose.ui.views.DropdownMenuItem
import lib.toolkit.base.managers.utils.UiUtils

@Composable
fun ExternalLinkSettingsScreen(
    link: ExternalLinkSharedTo?,
    access: Int,
    isCreate: Boolean,
    roomType: Int?,
    roomId: String?,
    onBackListener: () -> Unit,
) {
    if (link == null) return
    val context = LocalContext.current
    val localView = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel = viewModel {
        ExternalLinkSettingsViewModel(
            access = access,
            inputLink = link,
            roomId = roomId,
            roomProvider = context.roomProvider
        )
    }
    val state by viewModel.state.collectAsState()
    val isRevoke = link.primary && roomType in arrayOf(PUBLIC_ROOM, FILL_FORMS_ROOM)
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
        access = state.access,
        loading = state.loading,
        passwordErrorState = passwordErrorState,
        isCreate = isCreate,
        isRevoke = isRevoke,
        onSetAccess = viewModel::setAccess,
        onBackListener = onBackListener,
        onDoneClick = if (isCreate) viewModel::createLink else viewModel::save,
        onDeleteOrRevokeLink = {
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
                acceptListener = viewModel::deleteOrRevoke
            )
        },
        updateViewState = viewModel::updateViewState
    )
}

@Composable
private fun MainScreen(
    link: ExternalLinkSharedTo,
    loading: Boolean,
    access: Int,
    passwordErrorState: MutableState<String?>,
    isCreate: Boolean,
    isRevoke: Boolean,
    onSetAccess: (Int) -> Unit,
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
                                enabled = link.title.isNotEmpty(),
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
                val password = remember { mutableStateOf(link.password.orEmpty()) }
                val passwordEnabled = remember { mutableStateOf(!link.password.isNullOrEmpty()) }
                var denyDownload by remember { mutableStateOf(link.denyDownload) }
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(denyDownload) {
                    updateViewState { copy(denyDownload = denyDownload) }
                }

                LaunchedEffect(title.value) {
                    delay(500)
                    updateViewState { copy(title = title.value) }
                }

                LaunchedEffect(password.value) {
                    if (password.value.isEmpty()) return@LaunchedEffect
                    delay(500)
                    updateViewState { copy(password = password.value) }
                }

                AppHeaderItem(title = R.string.rooms_info_link_name)
                AppTextFieldListItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    state = title,
                    hint = stringResource(id = lib.toolkit.base.R.string.text_hint_required)
                )
                AppHeaderItem(title = R.string.rooms_share_general_header)
                AppListItem(
                    title = stringResource(R.string.share_access_room_type),
                    endContent = {
                        val dropdownMenuShow = remember { mutableStateOf(false) }
                        DropdownMenuButton(
                            state = dropdownMenuShow,
                            icon = ImageVector.vectorResource(ManagerUiUtils.getAccessIcon(access)),
                            onDismiss = { dropdownMenuShow.value = false },
                            items = {
                                RoomUtils.getLinkAccessOptions().forEach { accessCode ->
                                    DropdownMenuItem(
                                        title = stringResource(RoomUtils.getAccessTitle(accessCode)),
                                        selected = access == accessCode,
                                        startIcon = ManagerUiUtils.getAccessIcon(accessCode),
                                        onClick = {
                                            dropdownMenuShow.value = false
                                            onSetAccess(accessCode)
                                        }
                                    )
                                }
                            }
                        ) {
                            dropdownMenuShow.value = true
                        }
                    }
                )
                if (!link.primary) {
                    LinkLifeTimeListItem(
                        expirationDate = link.expirationDate,
                        onSetLifeTime = {
                            updateViewState { copy(expirationDate = it.getFormattedDateTime()) }
                        }
                    )
                }

                AppHeaderItem(title = R.string.context_protection_title)
                AppSwitchItem(
                    title = R.string.rooms_info_password_access,
                    checked = passwordEnabled.value,
                    onCheck = { checked ->
                        passwordEnabled.value = checked;
                        updateViewState { copy(password = "".takeIf { checked }) }
                    }
                )
                AnimatedVisibilityVerticalFade(visible = passwordEnabled.value) {

                    LaunchedEffect(passwordEnabled.value) {
                        if (passwordEnabled.value) {
                            delay(500)
                            focusRequester.requestFocus()
                        }
                    }

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
                AppSwitchItem(
                    title = R.string.rooms_info_file_rectrict,
                    checked = denyDownload,
                    singleLine = false,
                    onCheck = { denyDownload = it }
                )
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 8.dp),
                    text = R.string.rooms_info_file_rectrict_desc
                )
                if (!isCreate) {
                    AppTextButton(
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                        title = if (isRevoke)
                            R.string.rooms_info_revoke_link else
                            R.string.rooms_info_delete_link,
                        textColor = MaterialTheme.colors.error,
                        onClick = onDeleteOrRevokeLink
                    )
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val link = ExternalLinkSharedTo(
        id = "",
        title = "Shared link",
        shareLink = "",
        linkType = 2,
        denyDownload = false,
        isExpired = false,
        primary = true,
        requestToken = "",
        password = null,
        expirationDate = null
    )

    MainScreen(
        link = link,
        loading = true,
        passwordErrorState = remember { mutableStateOf(null) },
        access = ApiContract.ShareCode.EDITOR,
        isCreate = false,
        isRevoke = true,
        onBackListener = {},
        onSetAccess = {},
        onDoneClick = {},
        onDeleteOrRevokeLink = {}) {}
}