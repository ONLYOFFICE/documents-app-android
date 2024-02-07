package lib.compose.ui.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextPrimary
import lib.compose.ui.theme.colorTextSecondary
import lib.toolkit.base.R

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    state: MutableState<String>,
    hint: String = "",
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardOptions: KeyboardOptions? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    label: Int? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    focusManager: FocusManager? = null,
    errorState: MutableState<String?>? = null,
    onValueChange: ((String) -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    val errorAnimation = remember { Animatable(0f) }

    LaunchedEffect(errorState?.value) {
        errorAnimation.animateTo(
            targetValue = if (errorState?.value != null) 1f else 0f,
            animationSpec = tween()
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.value,
            onValueChange = { value ->
                onValueChange?.let {
                    onValueChange(value)
                } ?: run {
                    state.value = value
                    errorState?.value = null
                }
            },
            singleLine = singleLine,
            isError = errorState?.value != null,
            label = { label?.let { Text(stringResource(id = label)) } },
            placeholder = { Text(text = hint) },
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            visualTransformation = visualTransformation,
            keyboardActions = KeyboardActions(
                onDone = { onDone?.invoke() },
                onNext = { if (state.value.isNotEmpty()) focusManager?.moveFocus(FocusDirection.Down) }
            ),
            keyboardOptions = keyboardOptions ?: KeyboardOptions(
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
            modifier = Modifier
                .alpha(alpha = errorAnimation.value)
                .padding(top = 4.dp, bottom = 8.dp),
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

@Composable
fun AppTextFieldListItem(state: MutableState<String?>, hint: String = "", isPassword: Boolean = false) {
    Column {
        Row(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.item_one_line_height))
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
                value = state.value.orEmpty(),
                onValueChange = { state.value = it },
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colors.primary),
                textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.colorTextPrimary),
                visualTransformation = if (!visible && isPassword)
                    PasswordVisualTransformation() else
                    VisualTransformation.None
            ) { textField ->
                if (state.value?.isEmpty() == true && !focused) {
                    Text(
                        text = hint,
                        color = MaterialTheme.colors.colorTextSecondary
                    )
                } else {
                    textField()
                }
            }
            if (isPassword) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { visible = !visible }
                ) {
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
        }
        AppDivider(startIndent = 16.dp)
    }
}

class SuffixTransformation(private val suffix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {

        val result = text + AnnotatedString(suffix)

        val textWithSuffixMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (text.isEmpty()) return 0
                return if (offset >= text.length) text.length else offset
            }
        }

        return TransformedText(result, textWithSuffixMapping)
    }
}

@Preview
@Composable
private fun AppTextFieldPreview() {
    ManagerTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            val state = remember { mutableStateOf("") }
            val errorState = remember { mutableStateOf<String?>(null) }
            Column {
                AppTextField(
                    state = state,
                    errorState = errorState,
                    label = R.string.app_title,
                    hint = stringResource(id = R.string.text_hint_required)
                )
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        if (errorState.value == null) {
                            errorState.value = "Some error"
                        } else {
                            errorState.value = null
                        }
                    }
                ) {
                    Text(text = "Toggle error")
                }
            }
        }
    }
}
