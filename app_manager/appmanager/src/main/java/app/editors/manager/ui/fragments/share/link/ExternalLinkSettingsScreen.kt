package app.editors.manager.ui.fragments.share.link

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.viewModels.link.ExternalLinkSettingsEffect
import app.editors.manager.viewModels.link.ExternalLinkSettingsViewModel
import kotlinx.coroutines.delay
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTextFieldListItem
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.openSendTextActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: back handler for ask to save
@Composable
fun ExternalLinkSettingsScreen(
    link: ExternalLink?,
    roomType: Int?,
    roomId: String?,
    onBackListener: () -> Unit
) {
    if (link == null) return
    val context = LocalContext.current
    val localView = LocalView.current
    val viewModel = viewModel { ExternalLinkSettingsViewModel(link, roomId, context.roomProvider) }
    val state by viewModel.state.collectAsState()
    val waitingDialog = remember {
        UiUtils.getWaitingDialog(
            context = context,
            isCircle = true,
            title = context.getString(R.string.dialogs_wait_title),
            cancelListener = viewModel::cancelJob
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ExternalLinkSettingsEffect.Loading -> {
                    waitingDialog.show()
                    delay(500)
                }
                ExternalLinkSettingsEffect.Save -> {
                    if (waitingDialog.isShowing) {
                        waitingDialog.dismiss()
                        onBackListener.invoke()
                    }
                }
                is ExternalLinkSettingsEffect.Copy -> {
                    waitingDialog.dismiss()
                    KeyboardUtils.setDataToClipboard(context, effect.url)
                    UiUtils.getSnackBar(localView).setText(R.string.rooms_info_copy_link_to_clipboard).show()
                }
                is ExternalLinkSettingsEffect.Share -> {
                    waitingDialog.dismiss()
                    context.openSendTextActivity(context.getString(R.string.toolbar_menu_main_share), effect.url)
                }
                is ExternalLinkSettingsEffect.Error -> {
                    waitingDialog.dismiss()
                    UiUtils.getSnackBar(localView).setText(effect.message).show()
                }
            }
        }
    }

    MainScreen(
        link = state.link,
        roomType = roomType,
        onBackListener = {
            if (state.viewStateChanged) {
                UiUtils.showQuestionDialog(
                    context = context,
                    title = context.getString(R.string.rooms_info_save_link_title),
                    description = context.getString(R.string.rooms_info_save_link_question),
                    acceptTitle = context.getString(R.string.rooms_info_save_button),
                    acceptListener = viewModel::save,
                    neutralTitle = context.getString(R.string.rooms_info_dont_save_button),
                    neutralListener = onBackListener
                )
            } else {
                onBackListener.invoke()
            }
        },
        onShareClick = viewModel::share,
        onCopyLink = viewModel::copy,
        onDeleteLink = {
            UiUtils.showQuestionDialog(
                context = context,
                title = context.getString(R.string.rooms_info_delete_link),
                description = context.getString(R.string.rooms_info_delete_link_message),
                acceptTitle = context.getString(R.string.list_context_delete),
                acceptListener = viewModel::delete
            )
        },
        updateViewState = { updated ->
            viewModel.updateViewState {
                copy(sharedTo = updated.invoke(sharedTo))
            }
        }
    )
}

@Composable
private fun MainScreen(
    link: ExternalLink,
    roomType: Int?,
    onBackListener: () -> Unit,
    onShareClick: () -> Unit,
    onCopyLink: () -> Unit,
    onDeleteLink: () -> Unit,
    updateViewState: (ExternalLinkSharedTo.() -> ExternalLinkSharedTo) -> Unit
) {
    ManagerTheme {
        var linkDateChanged by remember { mutableStateOf(false) }
        AppScaffold(
            topBar = {
                AppTopBar(
                    title = if (link.sharedTo.primary)
                        R.string.rooms_info_general_link else
                        R.string.rooms_info_additional_link,
                    backListener = onBackListener,
                    actions = {
                        AppTextButton(
                            title = R.string.toolbar_menu_main_share,
                            enabled = !(link.sharedTo.isExpired && !linkDateChanged),
                            onClick = onShareClick
                        )
                    }
                )
            }
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val context = LocalContext.current
                val title = remember { mutableStateOf<String?>(link.sharedTo.title) }
                val password = remember { mutableStateOf(link.sharedTo.password) }
                var denyDownload by remember { mutableStateOf(link.sharedTo.denyDownload) }
                var expirationString by remember { mutableStateOf(link.sharedTo.expirationDate) }
                var expirationDate by remember { mutableStateOf(TimeUtils.parseDate(expirationString)) }

                LaunchedEffect(denyDownload) {
                    updateViewState { copy(denyDownload = denyDownload) }
                }

                LaunchedEffect(expirationDate) {
                    updateViewState { copy(expirationDate = expirationDate?.let(TimeUtils.DEFAULT_FORMAT::format)) }
                }

                LaunchedEffect(title.value) {
                    delay(500)
                    updateViewState { copy(title = title.value.orEmpty()) }
                }

                LaunchedEffect(password.value) {
                    delay(500)
                    updateViewState { copy(password = password.value) }
                }

                AppHeaderItem(title = R.string.rooms_info_link_name)
                AppTextFieldListItem(state = title)
                AppHeaderItem(title = lib.editors.gbase.R.string.context_protection_title)
                AppSwitchItem(
                    title = R.string.rooms_info_password_access,
                    checked = password.value != null,
                    onCheck = { checked -> password.value = if (checked) "" else null }
                )
                AnimatedVisibilityVerticalFade(visible = password.value != null) {
                    AppTextFieldListItem(
                        state = password,
                        hint = stringResource(id = R.string.login_enterprise_password_hint),
                        isPassword = true
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
                if (!link.sharedTo.primary) {
                    AppHeaderItem(title = R.string.rooms_info_time_limit_title)
                    AppSwitchItem(
                        title = R.string.rooms_info_time_limit,
                        checked = expirationString != null,
                        onCheck = { checked ->
                            expirationString = if (checked) {
                                ""
                            } else {
                                updateViewState { copy(expirationDate = null) }
                                null
                            }
                        }
                    )
                    AnimatedVisibilityVerticalFade(visible = expirationString != null) {
                        if (expirationDate != null || expirationString == "") {
                            Column {
                                AppArrowItem(
                                    title = R.string.rooms_info_valid_through,
                                    optionTint = if (link.sharedTo.isExpired && !linkDateChanged)
                                        MaterialTheme.colors.error else
                                        MaterialTheme.colors.colorTextTertiary,
                                    option = expirationDate?.let { date ->
                                        SimpleDateFormat
                                            .getDateTimeInstance(
                                                DateFormat.LONG,
                                                DateFormat.SHORT,
                                                TimeUtils.getCurrentLocale(context) ?: Locale.getDefault()
                                            )
                                            .format(date)
                                    }
                                ) {
                                    TimeUtils.showDateTimePickerDialog(context, expirationDate) { date ->
                                        expirationDate = date
                                        linkDateChanged = true
                                    }
                                }
                                if (link.sharedTo.isExpired && !linkDateChanged) {
                                    AppDescriptionItem(
                                        modifier = Modifier.padding(top = 8.dp),
                                        text = R.string.rooms_info_link_expired_full,
                                        color = MaterialTheme.colors.error
                                    )
                                }
                            }
                        }
                    }
                }
                AppTextButton(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                    enabled = !(link.sharedTo.isExpired && !linkDateChanged),
                    title = R.string.rooms_info_copy_link,
                    onClick = onCopyLink
                )
                AppTextButton(
                    modifier = Modifier.padding(start = 8.dp),
                    title = if (link.sharedTo.primary && roomType == ApiContract.RoomType.PUBLIC_ROOM)
                        R.string.rooms_info_revoke_link else
                        R.string.rooms_info_delete_link,
                    textColor = MaterialTheme.colors.error,
                    onClick = onDeleteLink
                )
            }
        }
    }
}

@Preview(apiLevel = 33, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val link = ExternalLink(
        access = 2,
        isLocked = false,
        isOwner = false,
        canEditAccess = false,
        denyDownload = false,
        sharedTo = ExternalLinkSharedTo(
            id = "",
            title = "Shared link",
            shareLink = "",
            linkType = 2,
            denyDownload = false,
            isExpired = true,
            primary = true,
            requestToken = "",
            password = null,
            expirationDate = "2023-12-06T14:00:00.0000000+03:00"
        )
    )

    MainScreen(link = link, ApiContract.RoomType.CUSTOM_ROOM, {}, {}, {}, {}) {}
}