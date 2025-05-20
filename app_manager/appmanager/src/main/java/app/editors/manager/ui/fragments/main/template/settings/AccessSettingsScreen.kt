package app.editors.manager.ui.fragments.main.template.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.editors.manager.R
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.viewModels.main.TemplateAccessSettings
import app.editors.manager.viewModels.main.TemplateAccessSettingsState
import app.editors.manager.viewModels.main.TemplateUserListViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.TopAppBarAction

@Composable
fun AccessSettingsScreen(
    viewModel: TemplateUserListViewModel,
    goToList: () -> Unit,
    onSave: (TemplateAccessSettings) -> Unit,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    AccessSettingsScreenContent(
        settings = settings,
        goToList = goToList,
        currentUser = viewModel.getCurrentUser(),
        onDeleteMember = viewModel::deleteMember,
        switchPublic = viewModel::switchPublic,
        onSave = onSave,
        onBack = onBack
    )
}

@Composable
private fun AccessSettingsScreenContent(
    settings: TemplateAccessSettingsState,
    currentUser: User,
    switchPublic: (Boolean) -> Unit,
    goToList: () -> Unit,
    onSave: (TemplateAccessSettings) -> Unit,
    onDeleteMember: (String) -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_access_settings),
                backListener = onBack,
                actions = {
                    TopAppBarAction(
                        icon = R.drawable.drawable_ic_done,
                        enabled = !(settings.loading || settings.requestLoading),
                        onClick = { onSave(settings.confirmedSettings) }
                    )
                }
            )
        },
        useTablePaddings = false
    ) {
        if (settings.requestLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (settings.loading) {
            LoadingPlaceholder()
        } else {
            Column {
                AppSwitchItem(
                    title = stringResource(R.string.setting_access_public),
                    checked = settings.confirmedSettings.public,
                    onCheck = switchPublic
                )
                AppDescriptionItem(
                    text = stringResource(R.string.setting_access_public_desc),
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                if (!settings.confirmedSettings.public) {
                    AppHeaderItem(title = stringResource(R.string.setting_access_members_title))
                    AppArrowItem(
                        title = stringResource(R.string.invite_choose_from_list),
                        arrowVisible = true,
                        onClick = goToList
                    )
                    AppDescriptionItem(
                        text = stringResource(R.string.setting_access_members_title_desc),
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                    AppHeaderItem(title = stringResource(R.string.setting_access_list_title))
                    SelectedMembersList(
                        currentUser = currentUser,
                        users = settings.confirmedSettings.selectedUsers,
                        groups = settings.confirmedSettings.selectedGroups,
                        onDelete = onDeleteMember
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccessSettingsScreenPreview() {
    ManagerTheme {
        AccessSettingsScreenContent(
            settings = TemplateAccessSettingsState(
                loading = false,
                confirmedSettings = TemplateAccessSettings(
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
                        )
                    ),
                )
            ),
            goToList = {},

            currentUser = User(
                id = "0u",
                displayName = "Anokhin Sergey"
            ),
            onDeleteMember = {},
            switchPublic = {},
            onSave = {},
            onBack = {}
        )
    }
}

