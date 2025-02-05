package lib.compose.ui.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary

@Immutable
data class ChipList(
    val list: List<String> = emptyList()
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipsTextField(
    modifier: Modifier = Modifier,
    label: String,
    chips: ChipList,
    onChipAdd: (String) -> Unit,
    onChipDelete: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    var typing by remember { mutableStateOf(false) }
    var typeValue by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    BackHandler(typing) {
        if (typing) {
            typing = false
            if (typeValue.isNotEmpty()) {
                onChipAdd(typeValue)
            }
        }
    }

    LaunchedEffect(typing) {
        if (typing) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .widthIn(min = 48.dp)
            .heightIn(min = 56.dp)
            .height(IntrinsicSize.Min)
            .pointerInput(Unit) {
                detectTapGestures {
                    focused = false
                    typing = false
                    typeValue = ""
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.list.forEach { text ->
                    key(text) {
                        AppSelectableChip(
                            selected = true,
                            onClick = { onChipDelete.invoke(text) }
                        ) {
                            Text(text = text, color = MaterialTheme.colors.onPrimary)
                        }
                    }
                }
                if (typing) {
                    AppSelectableChip(selected = false) {
                        BasicTextField(
                            modifier = Modifier
                                .widthIn(8.dp)
                                .width(IntrinsicSize.Min)
                                .focusRequester(focusRequester)
                                .onFocusChanged { state ->
                                    if (focused && !state.isFocused) {
                                        typing = false
                                        typeValue = ""
                                    }
                                    focused = state.isFocused
                                },
                            value = typeValue,
                            singleLine = true,
                            onValueChange = { typeValue = it; error = false },
                            cursorBrush = SolidColor(MaterialTheme.colors.colorTextSecondary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (chips.list.any { it == typeValue }) {
                                        error = true
                                        return@KeyboardActions
                                    }

                                    if (typeValue.isNotEmpty()) {
                                        onChipAdd.invoke(typeValue)
                                        typeValue = ""
                                    }
                                }
                            ),
                            textStyle = MaterialTheme.typography.body2
                                .copy(
                                    color = if (error) {
                                        MaterialTheme.colors.error
                                    } else {
                                        MaterialTheme.colors.onSurface
                                    }
                                )
                        )
                    }
                } else {
                    AppSelectableChip(
                        selected = false,
                        onClick = { typing = true },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_add_small),
                                contentDescription = null
                            )
                        }
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
        AppDivider()
    }
}

@Preview
@Composable
private fun Preview() {
    val list = remember {
        mutableStateListOf(
            "one",
            "two",
            "threethreethreethree"
        )
    }
    ManagerTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
                .border(0.5.dp, Color.LightGray.copy(.5f))
        ) {
            ChipsTextField(
                label = "add tag",
                chips = ChipList(list),
                onChipDelete = { list.remove(it) },
                onChipAdd = { list.add(it) },
            )
        }
    }
}

@Preview
@Composable
private fun Preview2() {
    val list = remember {
        mutableStateListOf("one")
    }
    ManagerTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
                .border(0.5.dp, Color.LightGray.copy(.5f))
        ) {
            ChipsTextField(
                label = "add tag",
                chips = ChipList(list),
                onChipDelete = { list.remove(it) },
                onChipAdd = { list.add(it) },
            )
        }
    }
}