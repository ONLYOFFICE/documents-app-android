package app.editors.manager.ui.compose.locale

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.Previews
import lib.toolkit.base.managers.utils.capitalize
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppLocalePickerScreen(
    locales: List<Locale>,
    current: Locale,
    onChangeLocale: (Locale?, Boolean) -> Unit,
    onBackListener: () -> Unit
) {
    ManagerTheme {
        Surface(color = MaterialTheme.colors.background) {
            LazyColumn {
                items(locales) { locale ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onChangeLocale.invoke(locale, false)
                                onBackListener()
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = locale.getDisplayName(locale).capitalize(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        if (current.language == locale.language) {
                            Icon(
                                painter = painterResource(id = R.drawable.drawable_ic_done),
                                contentDescription = null,
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Previews.All
@Composable
private fun AppLocalePickerScreenPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AppLocalePickerScreen(
            locales = listOf(Locale.ENGLISH, Locale.FRANCE, Locale.ITALY),
            current = Locale.ENGLISH,
            onChangeLocale = { _, _ -> },
            onBackListener = {},
        )
    }
}