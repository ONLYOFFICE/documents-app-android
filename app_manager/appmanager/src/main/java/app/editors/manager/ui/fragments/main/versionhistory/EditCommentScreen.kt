package app.editors.manager.ui.fragments.main.versionhistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.editors.manager.R
import app.editors.manager.viewModels.main.BaseEvent
import app.editors.manager.viewModels.main.EditCommentEvent
import app.editors.manager.viewModels.main.VersionEditCommentViewModel
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.TopAppBarAction

@Composable
fun EditCommentScreen(
    onSuccess: (Int) -> Unit,
    onCancel: () -> Unit
) {
    val viewModel: VersionEditCommentViewModel = viewModel(factory = VersionEditCommentViewModel.Factory)
    val scaffoldState = rememberScaffoldState()
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel.events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is BaseEvent.ShowMessage -> {
                        scaffoldState.snackbarHostState.showSnackbar(event.msg)
                    }
                    is EditCommentEvent.UpdateHistory -> {
                        onSuccess(R.string.edit_comment_success)
                    }
                }
            }
        }
    }

    AppScaffold(
        topBar = {
            Column {
                AppTopBar(
                    title = stringResource(R.string.version_edit_comment_title),
                    backListener = onCancel,
                    isClose = true,
                    actions = {
                        TopAppBarAction(
                            icon = R.drawable.drawable_ic_done,
                            enabled = !state.isLoading && state.comment.isNotBlank(),
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.saveComment()
                            }
                        )
                    }
                )
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        scaffoldState = scaffoldState,
        useTablePaddings = false
    ) {
        EditCommentScreenContent(
            comment = state.comment,
            onValueChange = viewModel::onCommentChange
        )
    }
}

@Composable
fun EditCommentScreenContent(
    comment: String,
    onValueChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val textFieldValue = remember {
        mutableStateOf(
            TextFieldValue(
                text = comment,
                selection = TextRange(comment.length)
            )
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        TextField(
            value = textFieldValue.value,
            onValueChange = {
                textFieldValue.value = it
                onValueChange(it.text)
            },
            label = { Text(stringResource(R.string.edit_comment_label)) },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                disabledTextColor = Color.Transparent,
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colors.primary,
                unfocusedIndicatorColor = Color.Gray,
                disabledIndicatorColor = Color.Gray,
                focusedLabelColor = MaterialTheme.colors.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}