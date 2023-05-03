package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.Previews
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.FileUtils

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
                        backPressed = ::finish,
                        onClick = { url -> showUrlInBrowser(getString(url)) }
                    )
                }
                composable(Screen.License.screen) {
                    LicenseScreen(backListener = navController::popBackStack)
                }
            }
        }
    }

}

@Composable
private fun AboutScreen(
    navController: NavHostController,
    sdkVersion: String,
    backPressed: () -> Unit,
    onClick: (Int) -> Unit
) {
    ManagerTheme {
        AppScaffold(topBar = {
            AppTopBar(title = R.string.about_title, backListener = backPressed)
        }) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerticalSpacer(height = 40.dp)
                Image(
                    painter = painterResource(id = lib.toolkit.base.R.drawable.image_onlyoffice_text),
                    contentDescription = null
                )
                VerticalSpacer(height = 16.dp)
                Text(
                    text = stringResource(
                        id = R.string.about_app_version,
                        formatArgs = arrayOf(
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE.toString(),
                            sdkVersion
                        )
                    ),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                VerticalSpacer(height = 48.dp)
                AppArrowItem(title = R.string.about_terms, dividerVisible = false) { onClick(R.string.app_url_terms) }
                AppArrowItem(title = R.string.about_policy, dividerVisible = false) { onClick(R.string.app_url_policy) }
                AppArrowItem(
                    title = R.string.about_license,
                    dividerVisible = false
                ) { navController.navigate(Screen.License.screen) }
                AppArrowItem(title = R.string.about_website, dividerVisible = false) { onClick(R.string.app_url_main) }
            }
        }
    }
}

@Composable
private fun LicenseScreen(backListener: () -> Unit) {
    ManagerTheme {
        AppScaffold(topBar = {
            AppTopBar(title = R.string.about_license, backListener = backListener)
        }) {
            AndroidView(factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    loadUrl(context.getString(R.string.app_licenses_path))
                }
            })
        }
    }
}

@Previews.All
@Composable
fun PreviewTablet() {
    AboutScreen(
        navController = rememberNavController(),
        sdkVersion = "5.4.21",
        backPressed = { },
        onClick = {})
}