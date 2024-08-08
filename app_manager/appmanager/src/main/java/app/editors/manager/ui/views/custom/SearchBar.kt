package app.editors.manager.ui.views.custom

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import lib.compose.ui.views.AppTextField


@Composable
fun SearchAppBar(
    onTextChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val searchValueState = remember { mutableStateOf("") }

    BackHandler(onBack = onClose::invoke)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        AppTextField(
            state = searchValueState,
            onValueChange = {
                searchValueState.value = it
                onTextChange.invoke(it)
            },
            focusManager = focusManager,
            label = R.string.share_title_search,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onDone = { focusManager.clearFocus(true) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = android.R.string.search_go),
                    modifier = Modifier.alpha(ContentAlpha.medium)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (searchValueState.value.isNotEmpty()) {
                            searchValueState.value = ""
                        } else {
                            onClose()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = android.R.string.cancel),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        )
    }
}