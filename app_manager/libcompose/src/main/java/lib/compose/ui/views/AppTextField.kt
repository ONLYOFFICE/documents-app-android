package lib.compose.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    errorState: MutableState<String?>? = null,
    onValueChange: ((String) -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    Column {
        TextField(
            modifier = modifier.fillMaxWidth(),
            value = state.value,
            onValueChange = { value ->
                onValueChange?.let {
                    onValueChange(value)
                } ?: run {
                    state.value = value
                    if (value.isEmpty()) errorState?.value = null
                }
            },
            singleLine = singleLine,
            isError = errorState?.value != null,
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
        Text(
            modifier = Modifier.alpha(if (errorState?.value.isNullOrEmpty()) 0f else 1f),
            text = errorState?.value.orEmpty(),
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption,
        )
    }
}

@Composable
fun AppPasswordTextField(
    modifier: Modifier = Modifier,
    state: MutableState<String>,
    label: Int,
    focusManager: FocusManager,
    onDone: (() -> Unit)? = null,
    errorState: MutableState<String?>? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

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
                    tint = if (errorState?.value != null) {
                        MaterialTheme.colors.error
                    } else {
                        LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                    }
                )
            }
        }
    )

}