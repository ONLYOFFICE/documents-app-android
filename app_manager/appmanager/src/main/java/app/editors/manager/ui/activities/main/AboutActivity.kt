package app.editors.manager.ui.activities.main

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.Previews
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.FileUtils

private sealed class Screen(val screen: String) {
    data object About : Screen("about")
    data object License : Screen("license")
}

@Composable
fun AboutScreen(onShowBrowser: (Int) -> Unit, onBackClick: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.About.screen) {
        composable(Screen.About.screen) {
            MainScreen(
                navController = navController,
                sdkVersion = FileUtils.readSdkVersion(context, "sdk.version"),
                backPressed = onBackClick,
                onClick = onShowBrowser
            )
        }
        composable(Screen.License.screen) {
            LicenseScreen()
        }
    }
}

@Composable
private fun MainScreen(
    navController: NavHostController,
    sdkVersion: String,
    backPressed: () -> Unit,
    onClick: (Int) -> Unit
) {
    ManagerTheme {
        Surface(color = MaterialTheme.colors.background) {
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
                AppArrowItem(
                    title = R.string.about_terms,
                    dividerVisible = false,
                    arrowVisible = false
                ) { onClick(R.string.app_url_terms) }
                AppArrowItem(title = R.string.about_policy, dividerVisible = false, arrowVisible = false) {
                    onClick(
                        R.string.app_url_policy
                    )
                }
                AppArrowItem(
                    title = R.string.about_license,
                    dividerVisible = false,
                    arrowVisible = false
                ) { navController.navigate(Screen.License.screen) }
                AppArrowItem(
                    title = R.string.about_website,
                    dividerVisible = false,
                    arrowVisible = false
                ) { onClick(R.string.app_url_main) }
            }
        }
    }
}

@Composable
private fun LicenseScreen() {
    ManagerTheme {
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

@Previews.All
@Composable
private fun Preview() {
    MainScreen(
        navController = rememberNavController(),
        sdkVersion = "5.4.21",
        backPressed = { },
        onClick = {})
}