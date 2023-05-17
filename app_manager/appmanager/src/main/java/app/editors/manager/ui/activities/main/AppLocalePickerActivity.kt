package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.app.appComponent
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.Previews
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.capitalize
import java.util.*

class AppLocalePickerActivity : AppCompatActivity() {

    companion object {

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun show(context: Context) {
            context.startActivity(Intent(context, AppLocalePickerActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setContent {
                with(appComponent.appLocaleHelper) {
                    AppLocalePickerScreen(
                        locales = locales,
                        current = currentLocale,
                        onBackListener = onBackPressedDispatcher::onBackPressed,
                        onChangeLocale = ::changeLocale
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    private fun AppLocalePickerScreen(
        locales: List<Locale>,
        current: Locale,
        onChangeLocale: (Locale?, Boolean) -> Unit,
        onBackListener: () -> Unit
    ) {
        ManagerTheme {
            AppScaffold(topBar = {
                AppTopBar(
                    title = R.string.settings_language,
                    backListener = onBackListener
                )
            }) {
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
    fun AppLocalePickerScreenPreview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AppLocalePickerScreen(
                locales = listOf(Locale.ENGLISH, Locale.FRANCE, Locale.ITALY),
                current = Locale.ENGLISH,
                onChangeLocale = { _, _ -> },
                onBackListener = {},
            )
        }
    }
}