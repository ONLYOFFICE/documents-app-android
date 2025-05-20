package app.editors.manager.ui.fragments.main.template.settings

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.editors.manager.R
import lib.toolkit.base.R as R2
import app.editors.manager.managers.tools.BaseEvent
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.mvp.models.ui.ResultUi
import app.editors.manager.mvp.models.ui.SizeUnit
import app.editors.manager.ui.dialogs.AddRoomItem
import app.editors.manager.ui.fragments.main.template.rememberAccountContext
import app.editors.manager.ui.fragments.room.add.ChooseImageBottomView
import app.editors.manager.ui.fragments.room.add.QuotaBlock
import app.editors.manager.ui.fragments.room.add.RoomLogo
import app.editors.manager.ui.fragments.share.MemberAvatar
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.viewModels.main.TemplateSettingsViewModel
import app.editors.manager.viewModels.base.RoomSettingsLogoState
import app.editors.manager.viewModels.main.TemplateAccessSettings
import app.editors.manager.viewModels.main.TemplateSettingsEvent
import app.editors.manager.viewModels.main.TemplateSettingsMode
import app.editors.manager.viewModels.main.TemplateSettingsState
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppMultilineArrowItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextFieldListItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.ChipList
import lib.compose.ui.views.ChipsTextField
import lib.compose.ui.views.NestedColumn
import lib.compose.ui.views.PlaceholderView
import lib.toolkit.base.managers.utils.capitalize

@Composable
fun TemplateSettingsScreen(
    viewModel: TemplateSettingsViewModel,
    showSnackbar: (String) -> Unit,
    navigateToAccessSettings: () -> Unit,
    navigateToCreated: (id: String?, type: Int?, title: String?) -> Unit,
    onBack: () -> Unit
) {
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val logoState by viewModel.logoState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is BaseEvent.ShowMessage -> {
                        showSnackbar(event.msg)
                    }

                    is TemplateSettingsEvent.Created -> {
                        navigateToCreated(event.id, state.roomType, state.title)
                    }

                    TemplateSettingsEvent.Edited -> {
                        navigateToCreated(null, null, null)
                    }
                }
            }
        }
    }

    TemplateSettingsScreenContent(
        state = state,
        mode = viewModel.getMode(),
        loadingStatus = loadingStatus,
        logoState = logoState,
        onSetImage = viewModel::setLogoUri,
        onDeleteImage = viewModel::deleteLogo,
        onChangeTag = viewModel::addOrRemoveTag,
        onTitleChange = viewModel::changeTitle,
        onSetQuotaEnabled = { enabled ->
            viewModel.updateStorageQuota {
                it.copy(enabled = enabled)
            }
        },
        onSetQuotaValue = { value ->
            viewModel.updateStorageQuota {
                it.copy(value = value)
            }
        },
        onSetQuotaMeasurementUnit = { unit ->
            viewModel.updateStorageQuota {
                it.copy(unit = unit)
            }
        },
        onSave = viewModel::save,
        navigateToAccessSettings = navigateToAccessSettings,
        onBack = onBack
    )
}

@Composable
private fun TemplateSettingsScreenContent(
    state: TemplateSettingsState,
    mode: TemplateSettingsMode,
    loadingStatus: ResultUi<*>,
    logoState: RoomSettingsLogoState,
    onSetImage: (Uri?) -> Unit,
    onDeleteImage: () -> Unit,
    onChangeTag: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onSetQuotaEnabled: (Boolean) -> Unit,
    onSetQuotaValue: (Long) -> Unit,
    onSetQuotaMeasurementUnit: (SizeUnit) -> Unit,
    onSave: () -> Unit,
    navigateToAccessSettings: () -> Unit,
    onBack: () -> Unit
) {
    val keyboardController = LocalFocusManager.current
    val title = when (mode) {
        is TemplateSettingsMode.CreateTemplate -> stringResource(R.string.title_save_template)
        is TemplateSettingsMode.EditTemplate -> stringResource(R.string.title_edit_template)
        is TemplateSettingsMode.CreateRoom -> stringResource(R.string.title_create_from_template)
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = title,
                isClose = true,
                backListener = onBack,
                actions = {
                    TextButton(
                        enabled = state.canApplyChanges && !state.isSaving,
                        onClick = {
                            keyboardController.clearFocus()
                            onSave()
                        }
                    ) {
                        val textId = if (mode is TemplateSettingsMode.EditTemplate) {
                            R.string.rooms_info_save_button
                        } else {
                            R.string.dialogs_edit_accept_create
                        }
                        Text(stringResource(textId).capitalize())
                    }
                }
            )
        },
        useTablePaddings = false
    ) {
        TemplateSettingsScreenContent(
            loadingStatus = loadingStatus,
            templateState = state,
            mode = mode,
            logoState = logoState,
            onSetImage = onSetImage,
            onDeleteImage = onDeleteImage,
            onChangeTag = onChangeTag,
            onTitleChange = onTitleChange,
            onSetQuotaEnabled = onSetQuotaEnabled,
            onSetQuotaValue = onSetQuotaValue,
            onSetQuotaMeasurementUnit = onSetQuotaMeasurementUnit,
            navigateToAccessSettings = navigateToAccessSettings
        )
    }
}

@Composable
private fun TemplateSettingsScreenContent(
    loadingStatus: ResultUi<*>,
    templateState: TemplateSettingsState,
    mode: TemplateSettingsMode,
    logoState: RoomSettingsLogoState,
    onSetImage: (Uri?) -> Unit,
    onDeleteImage: () -> Unit,
    onChangeTag: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onSetQuotaEnabled: (Boolean) -> Unit,
    onSetQuotaValue: (Long) -> Unit,
    onSetQuotaMeasurementUnit: (SizeUnit) -> Unit,
    navigateToAccessSettings: () -> Unit
) {
    Crossfade(targetState = loadingStatus) { state ->
        when (state) {
            ResultUi.Loading -> LoadingPlaceholder()
            is ResultUi.Success -> TemplateSettingsScreenContent(
                state = templateState,
                mode = mode,
                logoState = logoState,
                onSetImage = onSetImage,
                onDeleteImage = onDeleteImage,
                onChangeTag = onChangeTag,
                onTitleChange = onTitleChange,
                onSetQuotaEnabled = onSetQuotaEnabled,
                onSetQuotaValue = onSetQuotaValue,
                onSetQuotaMeasurementUnit = onSetQuotaMeasurementUnit,
                navigateToAccessSettings = navigateToAccessSettings
            )

            is ResultUi.Error -> {
                PlaceholderView(
                    image = null,
                    title = stringResource(R.string.placeholder_connection),
                    subtitle = stringResource(R.string.placeholder_connection_desc)
                )
            }
        }
    }
}

@Composable
private fun TemplateSettingsScreenContent(
    state: TemplateSettingsState,
    mode: TemplateSettingsMode,
    logoState: RoomSettingsLogoState,
    onSetImage: (Uri?) -> Unit,
    onDeleteImage: () -> Unit,
    onChangeTag: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onSetQuotaEnabled: (Boolean) -> Unit,
    onSetQuotaValue: (Long) -> Unit,
    onSetQuotaMeasurementUnit: (SizeUnit) -> Unit,
    navigateToAccessSettings: () -> Unit
) {
    val keyboardController = LocalFocusManager.current
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetContent = {
            ChooseImageBottomView(
                onDelete = {
                    coroutineScope.launch { modalBottomSheetState.hide() }
                    onDeleteImage()
                }.takeIf { logoState.logoUri != null || logoState.logoWebUrl?.isNotEmpty() == true },
                onSuccess = {
                    coroutineScope.launch { modalBottomSheetState.hide() }
                    onSetImage(it)
                }
            )
        },
        sheetState = modalBottomSheetState,
        scrimColor = if (!isSystemInDarkTheme()) {
            ModalBottomSheetDefaults.scrimColor
        } else {
            MaterialTheme.colors.background.copy(alpha = 0.60f)
        }
    ) {
        if (state.isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        NestedColumn {
            AddRoomItem(
                roomType = state.roomType,
                clickable = false,
                isTemplate = true
            ) {}
            Row(
                modifier = Modifier
                    .height(72.dp)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoomLogo(
                    logoState = logoState,
                    keyboardController = keyboardController,
                    selectPhoto = { coroutineScope.launch { modalBottomSheetState.show() } },
                    shape = RoundedCornerShape(8.dp)
                )
                AppTextFieldListItem(
                    modifier = Modifier.height(56.dp),
                    value = state.title,
                    hint = stringResource(id = R.string.template_name_hint),
                    contentPadding = PaddingValues(end = 16.dp),
                    onValueChange = onTitleChange,
                    focusManager = keyboardController,
                    fillMaxWidth = true
                )
            }

            ChipsTextField(
                modifier = Modifier.padding(start = 16.dp),
                label = stringResource(id = R.string.room_add_tag_hint),
                chips = ChipList(state.tags),
                contentPadding = PaddingValues(end = 16.dp),
                onChipAdd = onChangeTag,
                onChipDelete = onChangeTag
            )
            AppDescriptionItem(
                text = stringResource(R.string.setting_tags_desc),
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
            if (mode !is TemplateSettingsMode.CreateRoom) {
                AccessBlock(
                    settings = state.accessSettings,
                    currentUser = mode.user,
                    navigateToAccessSettings = navigateToAccessSettings
                )
            }

            AnimatedVisibilityVerticalFade(visible = state.quota.visible) {
                QuotaBlock(
                    quota = state.quota,
                    onSetEnabled = onSetQuotaEnabled,
                    onSetValue = onSetQuotaValue,
                    onSetMeasurementUnit = onSetQuotaMeasurementUnit
                )
            }
        }
    }
}

@Composable
private fun AccessBlock(
    settings: TemplateAccessSettings,
    currentUser: User,
    navigateToAccessSettings: () -> Unit
) {
    Column {
        if (settings.public) {
            AppMultilineArrowItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .background(
                                color = colorResource(R2.color.colorIconBackground),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = null,
                            tint = MaterialTheme.colors.colorTextSecondary
                        )
                    }
                },
                title = stringResource(R.string.setting_access_public),
                description = stringResource(R.string.setting_access_public_desc),
                onClick = navigateToAccessSettings
            )
        } else {
            MembersRow(
                currentUser = currentUser,
                users = settings.selectedUsers,
                groups = settings.selectedGroups,
                navigateToAccessSettings = navigateToAccessSettings
            )
        }
        AppDescriptionItem(
            text = stringResource(R.string.setting_access_desc),
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun MembersRow(
    currentUser: User,
    users: List<User>,
    groups: List<Group>,
    navigateToAccessSettings: () -> Unit
) {
    val onlyUsers = users.isNotEmpty() && groups.isEmpty()
    val onlyGroups = groups.isNotEmpty() && users.isEmpty()
    val hasBoth = users.isNotEmpty() && groups.isNotEmpty()
    val hasOnlyMe = !(onlyUsers || onlyGroups || hasBoth)

    val usersText = pluralStringResource(
        R.plurals.access_members_users_title, users.size, users.size
    )
    val groupsText = pluralStringResource(
        R.plurals.access_members_groups_title, groups.size, groups.size
    )

    val title = when {
        hasOnlyMe -> currentUser.displayName
        onlyUsers -> stringResource(R.string.access_members_me_and_title, usersText)
        onlyGroups -> stringResource(R.string.access_members_me_and_title, groupsText)
        else -> stringResource(R.string.access_members_me_users_groups_title, usersText, groupsText)
    }

    val membersList: List<Member> = buildList {
        add(currentUser)
        when {
            onlyUsers -> addAll(users.take(2))
            onlyGroups -> addAll(groups.take(2))
            hasBoth -> {
                add(users.first())
                add(groups.first())
            }
        }
    }

    MembersRow(
        membersList = membersList,
        title = title,
        hasOnlyMe = hasOnlyMe,
        onClick = navigateToAccessSettings
    )
}

@Composable
private fun MembersRow(
    membersList: List<Member>,
    title: String,
    hasOnlyMe: Boolean,
    onClick: () -> Unit
) {
    val xOffset = 24.dp
    val avatarSize = 40.dp
    val avatarsRowWidth = avatarSize + xOffset * (membersList.size - 1)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MultiAvatarRow(
                membersList = membersList,
                xOffset = xOffset,
                avatarSize = avatarSize,
                avatarsRowWidth = avatarsRowWidth
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemberTitle(
                    name = title,
                    isCurrentUser = hasOnlyMe,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R2.drawable.ic_arrow_right
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colors.colorTextTertiary
                )
            }
        }
        AppDivider(startIndent = 16.dp + avatarsRowWidth + 16.dp)
    }
}

@Composable
private fun MultiAvatarRow(
    membersList: List<Member>,
    xOffset: Dp,
    avatarSize: Dp,
    avatarsRowWidth: Dp,
    modifier: Modifier = Modifier
) {
    val accountContext = rememberAccountContext()

    Box(modifier.width(avatarsRowWidth)) {
        membersList.reversed().forEachIndexed { index, member ->
            Box(
                modifier = Modifier
                    .offset(x = xOffset * (membersList.size - index - 1))
                    .clip(CircleShape)
                    .size(avatarSize)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.colorTextTertiary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (member) {
                    is User -> {
                        MemberAvatar(
                            name = member.displayName,
                            avatar = GlideUtils.getCorrectLoad(
                                member.avatarMedium,
                                accountContext.token,
                                accountContext.portal.urlWithScheme
                            )
                        )
                    }

                    is Group -> {
                        MemberAvatar(
                            name = member.name,
                            avatar = null
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTemplateScreenContentPreview() {
    ManagerTheme {
        TemplateSettingsScreenContent(
            state = TemplateSettingsState(
                title = "Template",
                roomType = 2,
                tags = listOf("tag1"),
                accessSettings = TemplateAccessSettings(
                    public = false,
                    selectedUsers = listOf(
                        User(
                            id = "1u",
                            displayName = "Anokhin Tollan"
                        )
                    ),
                    selectedGroups = listOf(
                        Group(
                            id = "1g",
                            name = "Programming Department"
                        ),
                        Group(
                            id = "2g",
                            name = "Programming Department"
                        ),
                        Group(
                            id = "3g",
                            name = "Programming Department"
                        ),
                        Group(
                            id = "4g",
                            name = "Programming Department"
                        ),
                    ),
                )
            ),
            loadingStatus = ResultUi.Success(Unit),
            logoState = RoomSettingsLogoState(),
            onSetImage = {},
            onDeleteImage = {},
            onChangeTag = {},
            onTitleChange = {},
            onSetQuotaEnabled = {},
            onSetQuotaValue = {},
            onSetQuotaMeasurementUnit = {},
            onSave = {},
            navigateToAccessSettings = {},
            onBack = {},
            mode = TemplateSettingsMode.CreateTemplate(User(displayName = "Sergey"), "")
        )
    }
}