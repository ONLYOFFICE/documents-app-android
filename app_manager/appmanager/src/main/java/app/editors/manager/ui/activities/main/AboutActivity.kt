package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.compose.ui.theme.AppManagerTheme
import app.editors.manager.compose.ui.theme.colorAppBar
import app.editors.manager.ui.activities.base.BaseAppActivity
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.UiUtils

private sealed class AboutClickedItem {
    object Terms : AboutClickedItem()
    object Policy : AboutClickedItem()
    object License : AboutClickedItem()
    object Web : AboutClickedItem()
}

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
                        sdkVersion = FileUtils.readSdkVersion(this@AboutActivity, "sdk.version"),
                        isTablet = UiUtils.isTablet(this@AboutActivity),
                        backPressed = { onBackPressed() },
                        itemClick = { itemClick ->
                            when (itemClick) {
                                is AboutClickedItem.Terms -> showUrlInBrowser(getString(R.string.app_url_terms))
                                is AboutClickedItem.Policy -> showUrlInBrowser(getString(R.string.app_url_policy))
                                is AboutClickedItem.License -> navController.navigate(Screen.License.screen)
                                is AboutClickedItem.Web -> showUrlInBrowser(getString(R.string.app_url_main))
                            }
                        }
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
    sdkVersion: String,
    isTablet: Boolean?,
    itemClick: (item: AboutClickedItem) -> Unit,
    backPressed: () -> Unit,
) {
    val scrollState = rememberScrollState()

    AppManagerTheme {
        Scaffold(topBar = {
            AppBar(title = R.string.about_title, icon = R.drawable.ic_toolbar_close) {
                backPressed()
            }
        }) {
            Surface(color = MaterialTheme.colors.background) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 48.dp)
                        .verticalScroll(state = scrollState, enabled = true)
                ) {
                    Image(painter = painterResource(id = lib.toolkit.base.R.drawable.image_onlyoffice_text), contentDescription = null)
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
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.padding(top = 48.dp))
                    AboutItem(
                        title = stringResource(id = R.string.about_terms),
                        icon = R.drawable.ic_open_in_new,
                        isTablet = isTablet
                    ) {
                        itemClick(AboutClickedItem.Terms)
                    }
                    AboutItem(
                        title = stringResource(id = R.string.about_policy),
                        icon = R.drawable.ic_open_in_new,
                        isTablet = isTablet
                    ) {
                        itemClick(AboutClickedItem.Policy)
                    }
                    AboutItem(
                        title = stringResource(id = R.string.about_license),
                        icon = R.drawable.ic_open_in_new,
                        isTablet = isTablet
                    ) {
                        itemClick(AboutClickedItem.License)
                    }
                    AboutItem(
                        title = stringResource(id = R.string.about_website),
                        icon = R.drawable.ic_open_in_new,
                        isTablet = isTablet
                    ) {
                        itemClick(AboutClickedItem.Web)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutItem(title: String, @DrawableRes icon: Int, isTablet: Boolean? = null, click: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .height(48.dp)
                .fillMaxWidth(if (isTablet == true) 0.7f else 1f)
                .background(color = MaterialTheme.colors.surface)
                .clickable { click() }
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Image(
                painter = painterResource(id = icon),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
                modifier = Modifier.align(Alignment.CenterEnd)
            )

        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth(if (isTablet == true) 0.7f else 1f)
                .height(1.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .padding(start = 0.dp, end = 0.dp)
                .background(color = colorResource(id = lib.toolkit.base.R.color.colorOutline))
        )
    }
}

@Composable
private fun LicenseScreen(backListener: () -> Unit) {
    AppManagerTheme {
        Scaffold(topBar = {
            AppBar(title = R.string.about_license, icon = R.drawable.ic_toolbar_back) {
                backListener()
            }
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