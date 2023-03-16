package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.compose.ui.theme.AppManagerTheme
import app.editors.manager.managers.utils.fillMaxWidth
import app.editors.manager.ui.compose.base.Spacer
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.UiUtils

class AppLocaleConfirmationActivity : AppCompatActivity() {

    companion object {

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun show(context: Context) {
            context.startActivity(Intent(context, AppLocaleConfirmationActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setContent {
                AppLocaleConfirmationScreen(isTablet = UiUtils.isTablet(this), { }) {

                }
            }
        }
    }

    @Composable
    fun AppLocaleConfirmationScreen(isTablet: Boolean, onClickContinue: () -> Unit, onClickSkip: () -> Unit) {
        AppManagerTheme {
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
                                painter = painterResource(id = R.drawable.image_onlyoffice_text),
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
                                text = "Приложение теперь на русском языке, потому что вы изменили \n" +
                                        "язык всей системы"
                            )
                            Spacer(size = 48.dp)
                            Button(
                                onClick = onClickSkip, modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(text = "Хорошо")
                            }
                            Spacer(size = 16.dp)
                            TextButton(onClick = onClickContinue) {
                                Text(text = "Continue using English")
                            }
                            Spacer(size = 24.dp)
                        }
                    }
                }
            }
        }
    }

    @Preview(uiMode = UI_MODE_NIGHT_YES, device = Devices.TABLET)
    @Composable
    fun PreviewTabletNightMode() {
        AppLocaleConfirmationScreen(isTablet = true, onClickContinue = { }) {

        }
    }

    @Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 360, heightDp = 640)
    @Composable
    fun PreviewPhone() {
        AppLocaleConfirmationScreen(isTablet = false, onClickContinue = { }) {

        }
    }
}