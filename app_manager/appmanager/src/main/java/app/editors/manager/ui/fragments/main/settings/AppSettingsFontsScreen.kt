package app.editors.manager.ui.fragments.main.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.editors.manager.R
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.PlaceholderView
import java.io.File


@Composable
fun AppSettingsFontsScreen(fonts: List<File>, onFontClick: (File) -> Unit = {}) {
    if (fonts.isEmpty()) {
        PlaceholderView(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            title = stringResource(id = R.string.placeholder_no_fonts),
            subtitle = ""
        )
    } else {
        LazyColumn {
            items(items = fonts, key = { it.name }) { font ->
                AppListItem(
                    modifier = Modifier.animateItem(),
                    title = font.nameWithoutExtension,
                    dividerVisible = false,
                    onClick = { onFontClick.invoke(font) }
                )
            }
        }
    }
}