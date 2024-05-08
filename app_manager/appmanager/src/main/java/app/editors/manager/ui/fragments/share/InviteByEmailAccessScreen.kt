package app.editors.manager.ui.fragments.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.views.custom.AccessDropdownMenu
import app.editors.manager.viewModels.main.InviteByEmailEffect
import app.editors.manager.viewModels.main.InviteByEmailViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.compose.ui.views.TopAppBarAction
import lib.compose.ui.views.VerticalSpacer
import retrofit2.HttpException

@Composable
fun InviteByEmailAccessScreen(
    viewModel: InviteByEmailViewModel,
    roomType: Int,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onSnackBar: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val accessList = remember { RoomUtils.getAccessOptions(roomType, true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InviteByEmailEffect.Success -> onSuccess.invoke()
                is InviteByEmailEffect.Error -> {
                    val text = when (val exception = effect.exception) {
                        is HttpException -> {
                            context.getString(R.string.errors_client_error) + exception.code()
                        }
                        else -> context.getString(R.string.errors_unknown_error)
                    }
                    onSnackBar(text)
                }
                else -> Unit
            }
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = R.string.rooms_share_access_rights,
                backListener = onBack,
                actions = {
                    TopAppBarAction(
                        icon = lib.toolkit.base.R.drawable.ic_done,
                        onClick = viewModel::save
                    )
                }
            )
        }
    ) {
        NestedColumn {
            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                VerticalSpacer(height = 4.dp)
            }
            AppHeaderItem(title = R.string.invite_new_members)
            state.emails.forEach { (email, access) ->
                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.padding(end = 16.dp),
                        painter = painterResource(R.drawable.ic_account_placeholder),
                        contentDescription = null
                    )
                    Column {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var dropdown by remember { mutableStateOf(false) }
                            Text(modifier = Modifier.weight(1f), text = email)
                            IconButton(onClick = { dropdown = true }) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(ManagerUiUtils.getAccessIcon(access)),
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.colorTextSecondary
                                    )
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_drawer_menu_header_arrow),
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.colorTextSecondary
                                    )
                                    AccessDropdownMenu(
                                        onDismissRequest = { dropdown = false },
                                        expanded = dropdown,
                                        accessList = accessList,
                                        onClick = { newAccess ->
                                            dropdown = false
                                            viewModel.setAccess(email, newAccess)
                                        }
                                    )
                                }
                            }
                        }
                        AppDivider()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun InviteAccessScreenPreview() {
    ManagerTheme {
        //        InviteAccessScreen(2)
    }
}