package app.editors.manager.ui.fragments.main.settings

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.documents.core.network.common.NetworkRequest
import app.documents.core.network.common.RequestsCollector
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.managers.utils.LogCollector
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
        stringResource(id = R.string.dev_mode_system_logs),
        stringResource(id = R.string.dev_mode_network_requests)
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
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
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
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        // Load logs and reverse the list to show the most recent messages first
        logs = LogCollector.collectLogs().asReversed()
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refresh button
            IconButton(
                onClick = {
                    logs = emptyList()
                    isLoading = true
                    refreshTrigger++ // Increment to trigger LaunchedEffect
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = "Refresh"
                )
            }

            // Clear button
            IconButton(
                onClick = {
                    LogCollector.clearLogs()
                    logs = emptyList()
                }
            ) {
                Icon(
                    painter = painterResource(id = lib.editors.gbase.R.drawable.ic_clear),
                    contentDescription = "Clear"
                )
            }

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.filter_title_search)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.placeholder_empty_folder),
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray
                )
            }
        } else {
            val filteredLogs = if (searchQuery.isBlank()) {
                logs
            } else {
                logs.filter { it.contains(searchQuery, ignoreCase = true) }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredLogs) { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.body2,
                        color = when {
                            log.contains(" E ", ignoreCase = true) -> Color.Red
                            log.contains(" W ", ignoreCase = true) -> Color.Yellow
                            log.contains(" I ", ignoreCase = true) -> Color.Green
                            else -> MaterialTheme.colors.onSurface
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                    Divider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun NetworkContent() {
    var requests by remember { mutableStateOf<List<NetworkRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var expandedRequestId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        requests = RequestsCollector.getRequests()
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top toolbar with controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refresh button
            IconButton(
                onClick = {
                    requests = emptyList()
                    isLoading = true
                    refreshTrigger++
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = "Refresh"
                )
            }

            // Clear button
            IconButton(
                onClick = {
                    RequestsCollector.clearRequests()
                    requests = emptyList()
                }
            ) {
                Icon(
                    painter = painterResource(id = lib.editors.gbase.R.drawable.ic_clear),
                    contentDescription = "Clear"
                )
            }

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search requests") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Content area
        if (isLoading) {
            // Loading state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (requests.isEmpty()) {
            // Empty state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.placeholder_empty_folder),
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray
                )
            }
        } else {
            // Filter requests based on search query
            val filteredRequests = if (searchQuery.isBlank()) {
                requests
            } else {
                requests.filter {
                    it.url.contains(searchQuery, ignoreCase = true) ||
                            it.method.contains(searchQuery, ignoreCase = true) ||
                            it.responseMessage.contains(searchQuery, ignoreCase = true)
                }
            }

            // Request list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredRequests.size) { index ->
                    val request = filteredRequests[index]
                    val isExpanded = expandedRequestId == index

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                expandedRequestId = if (isExpanded) null else index
                            }
                    ) {
                        // Request header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status indicator
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        when {
                                            request.responseCode in 200..299 -> Color.Green
                                            request.responseCode in 300..399 -> Color.Blue
                                            request.responseCode in 400..499 -> Color.Yellow
                                            request.responseCode >= 500 -> Color.Red
                                            else -> Color.Gray
                                        },
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Request summary
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${request.method} ${request.responseCode}",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = request.url,
                                    style = MaterialTheme.typography.body2,
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${request.getFormattedTime()} (${request.duration} ms)",
                                    style = MaterialTheme.typography.caption
                                )
                            }

                            // Expand/collapse indicator
                            Icon(
                                imageVector = if (isExpanded)
                                    ImageVector.vectorResource(R.drawable.drawable_ic_visibility_off)
                                else
                                    ImageVector.vectorResource(R.drawable.drawable_ic_visibility),
                                contentDescription = "Expand"
                            )
                        }

                        // Expanded details
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Headers section
                            Text(
                                text = "Headers:",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            request.headers.forEach { (name, value) ->
                                Text(
                                    text = "$name: $value",
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Request body section (if available)
                            request.requestBody?.let {
                                Text(
                                    text = "Request body:",
                                    style = MaterialTheme.typography.subtitle2,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // Response section
                            Text(
                                text = "Response: ${request.responseCode} ${request.responseMessage}",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )

                            request.responseBody?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        Divider(thickness = 0.5.dp)
                    }
                }
            }
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