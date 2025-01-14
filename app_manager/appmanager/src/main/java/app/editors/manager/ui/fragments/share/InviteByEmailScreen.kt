package app.editors.manager.ui.fragments.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.ui.views.custom.UserListBottomContent
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectableChip
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.StringUtils


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InviteByEmailScreen(onBack: () -> Unit, onNext: (List<String>) -> Unit) {
    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AppTopBar(
                title = R.string.invite_by_email,
                backListener = onBack
            )
        }) {
        Column {
            val emails = remember { mutableStateListOf<String>() }
            NestedColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                val textState = remember { mutableStateOf("") }
                val errorState = remember { mutableStateOf<String?>(null) }
                val context = LocalContext.current

                AppTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    state = textState,
                    label = R.string.login_enterprise_email_hint,
                    errorState = errorState,
                    keyboardType = KeyboardType.Email,
                    onValueChange = {
                        errorState.value = null
                        if (!it.contains("[, ;]".toRegex())) {
                            textState.value = it
                        } else {
                            if (StringUtils.isEmailValid(textState.value)) {
                                emails.add(textState.value)
                                textState.value = ""
                            } else {
                                errorState.value = context.getString(R.string.errors_email_syntax_error)
                            }
                        }
                    },
                    onDone = {
                        if (StringUtils.isEmailValid(textState.value)) {
                            emails.add(textState.value)
                            textState.value = ""
                        } else {
                            errorState.value = context.getString(R.string.errors_email_syntax_error)
                        }
                    }
                )
                FlowRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    emails.forEach {
                        AppSelectableChip(
                            onClick = { emails.remove(it) },
                            selected = true,
                            content = { Text(text = it) }
                        )
                    }
                }
            }
            UserListBottomContent(
                nextButtonTitle = lib.toolkit.base.R.string.common_next,
                count = emails.size,
                access = null,
                onDelete = emails::clear
            ) { onNext.invoke(emails) }
        }
    }
}

@Preview
@Composable
private fun InviteByEmailScreenPreview() {
    ManagerTheme {
        InviteByEmailScreen({}, {})
    }
}
