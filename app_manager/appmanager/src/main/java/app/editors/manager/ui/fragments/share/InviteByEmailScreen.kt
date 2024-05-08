package app.editors.manager.ui.fragments.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.viewModels.main.InviteByEmailViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectableChip
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.utils.StringUtils


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InviteByEmailScreen(viewModel: InviteByEmailViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.state.collectAsState()

    AppScaffold(topBar = {
        AppTopBar(
            title = R.string.invite_by_email,
            backListener = onBack,
            actions = {
                TopAppBarAction(
                    icon = lib.toolkit.base.R.drawable.ic_done,
                    enabled = state.emails.isNotEmpty(),
                    onClick = onNext
                )
            }
        )
    }) {
        NestedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            val textState = remember { mutableStateOf("") }
            val errorState = remember { mutableStateOf<String?>(null) }
            val context = LocalContext.current

            AppTextField(
                state = textState,
                errorState = errorState,
                keyboardType = KeyboardType.Email,
                onValueChange = {
                    errorState.value = null
                    if (!it.contains("[, ;]".toRegex())) {
                        textState.value = it
                    } else {
                        if (StringUtils.isEmailValid(textState.value)) {
                            viewModel.add(textState.value)
                            textState.value = ""
                        } else {
                            errorState.value = context.getString(R.string.errors_email_syntax_error)
                        }
                    }
                },
                onDone = {
                    if (StringUtils.isEmailValid(textState.value)) {
                        viewModel.add(textState.value)
                        textState.value = ""
                    } else {
                        errorState.value = context.getString(R.string.errors_email_syntax_error)
                    }
                }
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.emails.forEach {
                    AppSelectableChip(onClick = { viewModel.remove(it.key) }, selected = true) {
                        Text(text = it.key)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun InviteByEmailScreenPreview() {
    ManagerTheme {
        //        InviteByEmailScreen({}, {})
    }
}
