package app.editors.manager.ui.fragments.main.settings

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import app.editors.manager.viewModels.main.AppSettingsViewModel
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
fun AboutScreen(
    onShowBrowser: (Int) -> Unit,
    onDeveloperModeTrigger: () -> Unit = {},
    viewModel: AppSettingsViewModel? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.About.screen) {
        composable(Screen.About.screen) {
            val isDeveloperMode = viewModel?.settingsState?.collectAsState()?.value?.developerMode ?: false

            MainScreen(
                navController = navController,
                sdkVersion = FileUtils.readSdkVersion(context, "sdk.version"),
                onClick = onShowBrowser,
                onDeveloperModeTrigger = onDeveloperModeTrigger,
                isDeveloperMode = isDeveloperMode,
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
    onClick: (Int) -> Unit,
    onDeveloperModeTrigger: () -> Unit = {},
    isDeveloperMode: Boolean = false
) {
    val tapCount = remember { mutableIntStateOf(0) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.about_title),
        "App logs",
        "Network requests"
    )

    ManagerTheme {
        Surface(color = MaterialTheme.colors.background) {
            Column {
                if (isDeveloperMode) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title) },
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index }
                            )
                        }
                    }
                }

                when {
                    isDeveloperMode && selectedTabIndex == 1 -> LogsContent()
                    isDeveloperMode && selectedTabIndex == 2 -> NetworkContent()
                    else -> {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VerticalSpacer(height = 40.dp)
                            Image(
                                painter = painterResource(id = lib.toolkit.base.R.drawable.image_onlyoffice_text),
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    tapCount.intValue++
                                    if (tapCount.intValue >= 10) {
                                        tapCount.intValue = 0
                                        onDeveloperModeTrigger()
                                    }
                                }
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

@Composable
private fun LogsContent() {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "App Logs",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.weight(1f)
            )


//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
//            }
        }

        if (true) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Logs are not available yet.",
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {

            }
        }
    }
}

@Composable
private fun NetworkContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(List(10) { "Network request  #$it: https://api.example.com/$it" }) { request ->
            Text(text = request)
            Spacer(modifier = Modifier.height(4.dp))
            Divider()
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Previews.All
@Composable
private fun Preview() {
    MainScreen(
        navController = rememberNavController(),
        sdkVersion = "5.4.21",
        onClick = {})
}