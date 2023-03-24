package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.compose.ui.theme.AppManagerTheme
import app.editors.manager.compose.ui.theme.colorAppBar
import app.editors.manager.managers.utils.fillMaxWidth
import app.editors.manager.ui.activities.base.BaseAppActivity
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.UiUtils

enum class Screen(val screen: String) {
    About("about"), License("license")
}

class AboutActivity : BaseAppActivity() {

    companion object {
        val TAG: String = AboutActivity::class.java.simpleName

        fun show(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.About.screen) {
                composable(Screen.About.screen) {
                    AboutScreen(
                        navController = navController,
                        sdkVersion = FileUtils.readSdkVersion(this@AboutActivity, "sdk.version"),
                        isTablet = UiUtils.isTablet(this@AboutActivity),
                        backPressed = onBackPressedDispatcher::onBackPressed,
                        onClick = { url -> showUrlInBrowser(getString(url)) }
                    )
                }
                composable(Screen.License.screen) {
                    LicenseScreen {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

}

@Composable
private fun AppBar(@StringRes title: Int, @DrawableRes icon: Int, onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Close",
                    tint = MaterialTheme.colors.primary,
                )
            }
        },
        backgroundColor = MaterialTheme.colors.colorAppBar
    )
}

@Composable
private fun AboutScreen(
    navController: NavHostController,
    sdkVersion: String,
    isTablet: Boolean,
    backPressed: () -> Unit,
    onClick: (Int) -> Unit
) {
    AppManagerTheme {
        Scaffold(topBar = {
            AppBar(title = R.string.about_title, icon = R.drawable.ic_toolbar_close, onClick = backPressed)
        }) { padding ->
            Surface(
                color = MaterialTheme.colors.background,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxHeight()
                        .fillMaxWidth(isTablet),
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Image(
                        painter = painterResource(id = lib.toolkit.base.R.drawable.image_onlyoffice_text),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        text = stringResource(
                            id = R.string.about_app_version,
                            formatArgs = arrayOf(
                                BuildConfig.VERSION_NAME,
                                BuildConfig.VERSION_CODE.toString(),
                                sdkVersion
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    AboutItem(title = R.string.about_terms, isTablet = isTablet) {
                        onClick(R.string.app_url_terms)
                    }
                    AboutItem(title = R.string.about_policy, isTablet = isTablet) {
                        onClick(R.string.app_url_policy)
                    }
                    AboutItem(title = R.string.about_license, isTablet = isTablet) {
                        navController.navigate(Screen.License.screen)
                    }
                    AboutItem(title = R.string.about_website, isTablet = isTablet) {
                        onClick(R.string.app_url_main)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutItem(@StringRes title: Int, isTablet: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth(isTablet)
        .clickable(onClick = onClick)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(47.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = stringResource(id = title))
            Icon(
                painter = painterResource(id = R.drawable.ic_open_in_new),
                contentDescription = null,
                tint = colorResource(id = lib.toolkit.base.R.color.colorGrey)
            )
        }
        Divider()
    }
}

@Composable
private fun LicenseScreen(backListener: () -> Unit) {
    AppManagerTheme {
        Scaffold(topBar = {
            AppBar(title = R.string.about_license, icon = R.drawable.ic_toolbar_back) {
                backListener()
            }
        }) { padding ->
            AndroidView(factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    loadUrl(context.getString(R.string.app_licenses_path))
                }
            }, modifier = Modifier.padding(padding))
        }
    }
}

@Preview(device = Devices.TABLET)
@Composable
fun PreviewTablet() {
    AboutScreen(
        navController = rememberNavController(),
        sdkVersion = "5.4.21",
        isTablet = true,
        backPressed = { },
        onClick = {})
}

@Preview
@Composable
fun PreviewPhone() {
    AboutScreen(
        navController = rememberNavController(),
        sdkVersion = "5.4.21",
        isTablet = false,
        backPressed = { },
        onClick = {})
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewPhoneDarkMode() {
    AboutScreen(
        navController = rememberNavController(),
        sdkVersion = "5.4.21",
        isTablet = false,
        backPressed = { },
        onClick = {})
}