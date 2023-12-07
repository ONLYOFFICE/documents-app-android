package app.editors.manager.ui.fragments.share.link

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.UiUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

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
    var saveConfirmationDialogState by remember { mutableStateOf(false) }
    var deleteConfirmationDialogState by remember { mutableStateOf(false) }
    var waitingDialogState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                ExternalLinkSettingsEffect.Loading -> {
                    waitingDialogState = true
                    delay(500)
                }
                ExternalLinkSettingsEffect.Save -> {
                    if (waitingDialogState) {
                        waitingDialogState = false
                        onBackListener.invoke()
                    }
                }
                is ExternalLinkSettingsEffect.Copy -> {
                    waitingDialogState = false
                    UiUtils.getSnackBar(localView).setText("Copied").show()
                }
                is ExternalLinkSettingsEffect.Share -> {
                    waitingDialogState = false
                    UiUtils.getSnackBar(localView).setText("Share").show()
                }
                is ExternalLinkSettingsEffect.Error -> {
                    waitingDialogState = false
                    UiUtils.getSnackBar(localView).setText(it.message).show()
                }
            }
        }
    }

    if (waitingDialogState) {
        AlertDialog(
            title = { Text(text = stringResource(id = R.string.dialogs_wait_title)) },
            text = { CircularProgressIndicator() },
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { waitingDialogState = false }) {
                    Text(text = stringResource(id = R.string.operation_panel_cancel_button))
                }
            }
        )
    }

    if (saveConfirmationDialogState) {
        AlertDialog(
            title = { Text(text = "Save link") },
            text = { Text(text = "Are you sure you want to save and exit?") },
            onDismissRequest = { saveConfirmationDialogState = false },
            buttons = {
                TextButton(onClick = { saveConfirmationDialogState = false }) {
                    Text(text = stringResource(id = R.string.operation_panel_cancel_button))
                }
                TextButton(onClick = {
                    saveConfirmationDialogState = false
                    onBackListener.invoke()
                }) {
                    Text(text = "Don't save")
                }
                TextButton(onClick = {
                    saveConfirmationDialogState = false
                    viewModel.save()
                }) {
                    Text(text = stringResource(id = R.string.storage_connect_save_button))
                }
            }
        )
    }

    if (deleteConfirmationDialogState) {
        AlertDialog(
            title = { Text(text = stringResource(id = R.string.rooms_info_delete_link)) },
            text = { Text(text = stringResource(id = R.string.rooms_info_delete_link_message)) },
            onDismissRequest = { deleteConfirmationDialogState = false },
            buttons = {
                TextButton(onClick = {
                    deleteConfirmationDialogState = false
                }) {
                    Text(text = stringResource(id = R.string.operation_panel_cancel_button))
                }
                TextButton(onClick = viewModel::delete) {
                    Text(text = stringResource(id = R.string.list_context_delete))
                }
            }
        )
    }

    MainScreen(
        link = state.link,
        roomType = roomType,
        onBackListener = {
            if (state.viewStateChanged) {
                saveConfirmationDialogState = true
            } else {
                onBackListener.invoke()
            }
        },
        onShareClick = viewModel::share,
        onCopyLink = viewModel::copy,
        onDeleteLink = { deleteConfirmationDialogState = true },
        updateViewState = viewModel::updateViewState
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
    updateViewState: (ExternalLink) -> Unit
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
                var title by remember { mutableStateOf(link.sharedTo.title) }
                var password by remember { mutableStateOf(link.sharedTo.password) }
                var denyDownload by remember { mutableStateOf(link.sharedTo.denyDownload) }
                var expirationString by remember { mutableStateOf(link.sharedTo.expirationDate) }
                var expirationDate by remember { mutableStateOf(TimeUtils.parseDate(expirationString)) }

                LaunchedEffect(denyDownload) {
                    updateViewState.invoke(
                        link.copy(
                            sharedTo = link.sharedTo.copy(
                                denyDownload = denyDownload
                            )
                        )
                    )
                }

                LaunchedEffect(expirationDate) {
                    updateViewState.invoke(
                        link.copy(
                            sharedTo = link.sharedTo.copy(
                                expirationDate = TimeUtils.formatDateOrNull(expirationDate),
                            )
                        )
                    )
                }

                LaunchedEffect(title) {
                    delay(500)
                    updateViewState.invoke(link.copy(sharedTo = link.sharedTo.copy(title = title)))
                }

                LaunchedEffect(password) {
                    delay(500)
                    updateViewState.invoke(link.copy(sharedTo = link.sharedTo.copy(password = password)))
                }

                AppHeaderItem(title = R.string.rooms_info_link_name)
                BasicTextField(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    value = title,
                    onValueChange = { title = it },
                    textStyle = MaterialTheme.typography.body1
                )
                AppDivider(startIndent = 16.dp)

                AppHeaderItem(title = lib.editors.gbase.R.string.context_protection_title)
                AppSwitchItem(
                    title = R.string.rooms_info_password_access,
                    checked = password != null,
                    onCheck = { checked -> password = if (checked) "" else null }
                )
                AnimatedVisibilityVerticalFade(visible = password != null) {
                    Column {
                        Row(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var visible by remember { mutableStateOf(false) }
                            var focused by remember { mutableStateOf(false) }
                            BasicTextField(
                                modifier = Modifier
                                    .onFocusChanged { focused = it.isFocused }
                                    .weight(1f)
                                    .padding(vertical = 12.dp),
                                value = password.orEmpty(),
                                onValueChange = { password = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.body1,
                                visualTransformation = if (!visible)
                                    PasswordVisualTransformation() else
                                    VisualTransformation.None
                            ) {
                                if (password?.isEmpty() == true && !focused) {
                                    Text(
                                        text = stringResource(id = R.string.login_enterprise_password_hint),
                                        color = MaterialTheme.colors.colorTextSecondary
                                    )
                                } else {
                                    it()
                                }
                            }
                            IconButton(
                                modifier = Modifier.size(40.dp),
                                onClick = { visible = !visible }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(
                                        if (!visible) {
                                            R.drawable.drawable_ic_visibility
                                        } else {
                                            R.drawable.drawable_ic_visibility_off
                                        }
                                    ),
                                    tint = MaterialTheme.colors.primary,
                                    contentDescription = null
                                )
                            }
                        }
                        AppDivider(startIndent = 16.dp)
                    }
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
                        onCheck = { expirationString = if (it) "" else null }
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
                AnimatedContent(targetState = password, label = "passwordState") {
                    AppTextButton(
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                        enabled = !(link.sharedTo.isExpired && !linkDateChanged),
                        title = if (it != null)
                            R.string.rooms_info_copy_link_and_password else
                            R.string.rooms_info_copy_link,
                        onClick = onCopyLink
                    )
                }
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

@Preview(apiLevel = 33)
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