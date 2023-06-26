package lib.compose.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import lib.toolkit.base.R

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    state: MutableState<String>,
    hint: String = "",
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    label: Int? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    focusManager: FocusManager? = null,
    errorState: MutableState<Boolean>? = null,
    onValueChange: ((String) -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        value = state.value,
        onValueChange = { value ->
            onValueChange?.let {
                onValueChange(value)
            } ?: run {
                state.value = value
                if (value.isEmpty()) errorState?.value = false
            }
        },
        singleLine = singleLine,
        isError = errorState?.value == true,
        label = { label?.let { Text(stringResource(id = label)) } },
        placeholder = { Text(text = hint) },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() },
            onNext = { if (state.value.isNotEmpty()) focusManager?.moveFocus(FocusDirection.Down) }
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = onDone?.let { ImeAction.Done } ?: ImeAction.Next,
            keyboardType = keyboardType
        ),
        colors = TextFieldDefaults.textFieldColors(
            textColor = MaterialTheme.colors.onSurface,
            disabledTextColor = Color.Transparent,
            backgroundColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colors.primary,
            unfocusedIndicatorColor = Color.Gray,
            disabledIndicatorColor = Color.Gray,
            focusedLabelColor = MaterialTheme.colors.primary
        ),
    )
}

@Composable
fun AppPasswordTextField(
    modifier: Modifier = Modifier,
    state: MutableState<String>,
    label: Int,
    focusManager: FocusManager,
    onDone: (() -> Unit)? = null,
    errorState: MutableState<Boolean> = mutableStateOf(false),
    errorMessage: String = "",
    backgroundColor: Color = MaterialTheme.colors.background,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(backgroundColor)
            .padding(bottom = 16.dp)
    ) {
        AppTextField(
            modifier = modifier,
            state = state,
            label = label,
            focusManager = focusManager,
            onDone = onDone,
            errorState = errorState,
            keyboardType = KeyboardType.Password,
            hint = stringResource(id = R.string.text_hint_required),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        painter = if (passwordVisible) {
                            painterResource(id = R.drawable.drawable_ic_visibility)
                        } else {
                            painterResource(id = R.drawable.drawable_ic_visibility_off)
                        },
                        contentDescription = null,
                        tint = if (errorState.value) {
                            MaterialTheme.colors.error
                        } else {
                            LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                        }
                    )
                }
            }
        )
        AnimatedVisibility(errorState.value && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 32.dp)
            )
        }
    }

}