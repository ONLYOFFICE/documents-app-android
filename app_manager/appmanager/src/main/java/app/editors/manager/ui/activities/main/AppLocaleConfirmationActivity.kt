package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.fillMaxWidth
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.capitalize
import java.util.*
import lib.toolkit.base.R as Toolkit

class AppLocaleConfirmationActivity : AppCompatActivity() {

    companion object {

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun show(context: Context) {
            context.startActivity(
                Intent(context, AppLocaleConfirmationActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setContent {
                val localeHelper = appComponent.appLocaleHelper
                AppLocaleConfirmationScreen(
                    localeHelper = localeHelper,
                    isTablet = UiUtils.isTablet(this),
                    onClick = ::finish
                )
            }
        }
    }

    override fun onBackPressed() {
        // Stub
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun AppLocaleConfirmationScreen(
        isTablet: Boolean,
        localeHelper: AppLocaleHelper,
        onClick: () -> Unit = {}
    ) {
        ManagerTheme {
            Scaffold { padding ->
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(isTablet),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(isTablet)
                                .weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = Toolkit.drawable.image_onlyoffice_text),
                                contentDescription = null
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth(isTablet)
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                text = stringResource(
                                    id = R.string.settings_language_confirmation_message,
                                    locale = localeHelper.systemLocale
                                )
                            )
                            VerticalSpacer(height = 48.dp)
                            Button(
                                onClick = {
                                    localeHelper.changeLocale(null, true)
                                    onClick()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.settings_language_confirmation_accept,
                                        locale = localeHelper.systemLocale
                                    )
                                )
                            }
                            VerticalSpacer(height = 16.dp)
                            localeHelper.appLocale?.let { locale ->
                                TextButton(onClick = {
                                    localeHelper.setPrefs(true)
                                    onClick()
                                }) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.settings_language_continue_using_app_language,
                                            locale.getDisplayLanguage(locale).capitalize(locale)
                                        )
                                    )
                                }
                            }
                            VerticalSpacer(height = 24.dp)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Preview(uiMode = UI_MODE_NIGHT_YES, device = Devices.TABLET)
    @Composable
    fun PreviewTabletNightMode() {
        val context = LocalContext.current
        AppLocaleConfirmationScreen(
            isTablet = true,
            localeHelper = AppLocaleHelper(context, PreferenceTool(context))
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 360, heightDp = 640)
    @Composable
    fun PreviewPhone() {
        val context = LocalContext.current
        AppLocaleConfirmationScreen(
            isTablet = false,
            localeHelper = AppLocaleHelper(context, PreferenceTool(context))
        )
    }

    @Composable
    fun stringResource(id: Int, locale: Locale?): String {
        return with(LocalContext.current) {
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            createConfigurationContext(configuration).resources.getString(id)
        }
    }
}