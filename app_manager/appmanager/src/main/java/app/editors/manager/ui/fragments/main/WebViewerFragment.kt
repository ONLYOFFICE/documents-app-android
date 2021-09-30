package app.editors.manager.ui.fragments.main

import android.Manifest
import android.accounts.Account
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.Uri
import android.net.http.SslError
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import app.documents.core.account.AccountDao
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.webview.KeyboardWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.*
import lib.toolkit.base.managers.utils.FileUtils
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class WebViewerFragment : BaseAppFragment(), OnRefreshListener {

    companion object {
        val TAG: String = WebViewerFragment::class.java.simpleName

        private const val REQUEST_DOWNLOAD = 100
        private const val MESSAGE_LOST = 1
        private const val MESSAGE_AVAILABLE = 2
        private const val TAG_FILE = "TAG_FILE"
        private const val TAG_DOWNLOAD_URL = "TAG_DOWNLOAD_URL"
        private const val TAG_WEB_VIEW = "TAG_WEB_VIEW"
        private const val TAG_PAGE_LOAD = "TAG_PAGE_LOAD"
        private const val TAG_ERROR = "TAG_ERROR"
        private const val TAG_DOCUMENT_READY = "TAG_DOCUMENT_READY"
        private const val TAG_HARDBACK_MODE = "TAG_HARDBACK_MODE"
        private const val TAG_CONNECTION_LOST = "TAG_CONNECTION_LOST"
        private const val PATTERN_BACK_1 = ".*/files/#.*"
        private const val PATTERN_BACK_3 = ".*/Files/#.*"
        private const val PATTERN_BACK_2 = ".*projects.*\\d+#\\d+"
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"
        private const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"

        const val INTERFACE = "Android"
        const val KEY_EVENT = "event"
        const val KEY_DATA = "data"
        const val KEY_TYPE = "type"
        const val EVENT_COMMON = "message"
        const val EVENT_APP_READY = "onAppReady"
        const val EVENT_DOCUMENT_READY = "onDocumentReady"
        const val EVENT_INTERNAL = "onInternalMessage"
        const val EVENT_REQUEST_EDIT = "onRequestEditRights"
        const val TYPE_EVENT_MODE = "listenHardBack"
        const val TYPE_EVENT_EXIT = "hardBack"

        /*
     * Scripts for execute
     * */
        const val SCRIPT_TEST = "(function() { alert('Hello world!'); })();"

        const val SCRIPT_COMMAND_BACK =
            "window.ASC.Files.Editor.docEditor.serviceCommand('$TYPE_EVENT_EXIT');"
        const val SCRIPT_CALLBACK_EVENTS_JSON = "window.addEventListener('" +
                EVENT_COMMON + "', function(event){ " +
                "if (event) {" +
                INTERFACE + ".events(JSON.stringify(event.data));" +
                "}" +
                "}, {once: false});"
        const val SCRIPT_CALLBACK_EVENTS_PARSED = "window.addEventListener('" +
                EVENT_COMMON + "', function(event){ " +
                "if (event && window.JSON) {" +
                "   try {" +
                "       var msg = window.JSON.parse(event.data);" +
                "       if (msg.event == '" + EVENT_INTERNAL + "') {" +
                "           if (msg.data.type == '" + TYPE_EVENT_MODE + "') {" +
                INTERFACE + ".isHardBackOn(Boolean(true))" +
                "           } else if (msg.data.type == '" + TYPE_EVENT_EXIT + "') {" +
                INTERFACE + ".onHardBack(msg.data.data)" +
                "           }" +
                "       }" +
                "   } catch(e) {}" +
                "}}, {once: false});"

        private var valueCallback: ValueCallback<Array<Uri>>? = null

        @JvmStatic
        fun newInstance(file: CloudFile?): WebViewerFragment {
            return WebViewerFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(TAG_FILE, file)
                }
            }
        }
    }

    @Inject
    lateinit var accountDao: AccountDao

    @Inject
    lateinit var networkSettings: NetworkSettings

    init {
        App.getApp().appComponent.inject(this)
    }

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var webView: KeyboardWebView
    private lateinit var progressBar: ProgressBar

    private var cloudFile: CloudFile? = null
    private var uri: Uri? = null
    private var downloadUrl: String? = null
    private var isPageLoad = false
    private var errorCode = WebViewClient.ERROR_UNKNOWN
    private var webViewEventsInterface: WebViewEventsInterface? = null
    private var isDocumentReady = false
    private var isHardBackOn = false
    private var isDesktopMode = false
    private var isLostConnection = false
    private var connectivityManager: ConnectivityManager? = null

    private val readPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            imagePick.launch(null)
        } else {
            setValueCallback(arrayOf())
        }
    }

    private val downloadFile = registerForActivityResult(CreateDocument()) { data: Uri? ->
        downloadFile(data ?: Uri.EMPTY)
    }

    private val imagePick = registerForActivityResult(ImagePick()) { data: Uri? ->
        data?.let {
            setValueCallback(arrayOf(data))
        } ?: setValueCallback(arrayOf())


    }

    private val token = runBlocking(Dispatchers.Default) {
        accountDao.getAccountOnline()?.let { account ->
            return@runBlocking AccountUtils.getToken(
                App.getApp().applicationContext,
                Account(account.getAccountName(), App.getApp().getString(lib.toolkit.base.R.string.account_type))
            )
        } ?: run {
            throw Exception("No account")
        }
    }

    private val headers: Map<String, String> by lazy {
        mapOf(
            ApiContract.HEADER_AUTHORIZATION to "Bearer $token",
            ApiContract.HEADER_ACCEPT to ApiContract.VALUE_ACCEPT
        )
    }

    private val connectivityHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_LOST -> post {
                    isLostConnection = true
                    webView.stopLoading()
                }
                MESSAGE_AVAILABLE -> post {
                    if (isLostConnection) {
                        isLostConnection = false
                        isDocumentReady = false
                        isHardBackOn = false
                        loadWebView(uri.toString())
                    }
                }
            }
        }
    }

    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val message = Message()
            message.what = MESSAGE_AVAILABLE
            connectivityHandler.handleMessage(message)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            val message = Message()
            message.what = MESSAGE_LOST
            connectivityHandler.handleMessage(message)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentMode = UiUtils.checkDeXEnabled(resources.configuration)
        if (currentMode != isDesktopMode) {
            isDesktopMode = currentMode
            val intent = Intent()
            intent.putExtra(TAG_FILE, cloudFile)
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_viewer_web, container, false)?.apply {
            swipeRefresh = findViewById(R.id.web_viewer_layout)
            webView = findViewById(R.id.web_viewer_webview)
            progressBar = findViewById(R.id.web_viewer_progress)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TAG_PAGE_LOAD, isPageLoad)
        outState.putBoolean(TAG_DOCUMENT_READY, isDocumentReady)
        outState.putBoolean(TAG_HARDBACK_MODE, isHardBackOn)
        outState.putInt(TAG_ERROR, errorCode)
        outState.putBoolean(TAG_CONNECTION_LOST, isLostConnection)

        // Save WebView state
        val bundle = Bundle()
        webView.saveState(bundle)
        outState.putBundle(TAG_WEB_VIEW, bundle)


        // Save download url
        if (downloadUrl != null) {
            outState.putString(TAG_DOWNLOAD_URL, downloadUrl)
        }
    }

    override fun onBackPressed(): Boolean {
        if (isHardBackOn) {
            addJsExitCommand()
            return true
        }
        if (webView.canGoBack() && (isDesktopMode || UiUtils.isHuaweiDesktopMode(
                resources.configuration
            ))
        ) {
            webView.goBack()
            return true
        }
        requireActivity().finish()
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.removeJavascriptInterface(INTERFACE)
        webView.setDownloadListener(null)
        webView.webChromeClient = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && connectivityManager != null) {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        }
    }

    override fun onRefresh() {
        swipeRefresh.isRefreshing = false
        loadWebView(uri.toString())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init(savedInstanceState: Bundle?) {
        UiUtils.setColorFilter(requireContext(), progressBar.indeterminateDrawable, lib.toolkit.base.R.color.colorSecondary)
        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        isDesktopMode = UiUtils.checkDeXEnabled(resources.configuration)
        isPageLoad = false
        swipeRefresh.setOnRefreshListener(this)
        webViewEventsInterface = WebViewEventsInterface()
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.setAppCacheEnabled(false)
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewCallbacks()
        webView.setDownloadListener(WebViewDownload())
        webView.webChromeClient = WebViewChromeClient()
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
        webView.addJavascriptInterface(webViewEventsInterface!!, INTERFACE)
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

        if (isDesktopMode || UiUtils.isHuaweiDesktopMode(resources.configuration)) {
            webView.settings.userAgentString = DESKTOP_USER_AGENT
        }
        NetworkUtils.clearCookies(requireContext())
        getArgs()
        setStatusBarColor()
        restoreStates(savedInstanceState)
    }

    private fun getArgs() {
        val bundle = arguments
        cloudFile = bundle!!.getSerializable(TAG_FILE) as CloudFile?
        if (cloudFile?.isReadOnly == true) {
            uri = Uri.parse(cloudFile?.webUrl)
            val im = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(webView.windowToken, 0)
            if (uri?.getQueryParameter(ApiContract.Parameters.ARG_ACTION) == null) {
                uri = uri?.buildUpon()
                    ?.appendQueryParameter(ApiContract.Parameters.ARG_ACTION, ApiContract.Parameters.VAL_ACTION_VIEW)
                    ?.build()
            }
        } else {
            uri = Uri.parse(cloudFile?.webUrl)
        }
    }

    private fun restoreStates(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_WEB_VIEW)) {
                val bundle = savedInstanceState.getBundle(TAG_WEB_VIEW)
                progressBar.visibility = View.VISIBLE
                webView.restoreState(bundle!!)
            }
            if (savedInstanceState.containsKey(TAG_PAGE_LOAD)) {
                isPageLoad = savedInstanceState.getBoolean(TAG_PAGE_LOAD)
            }
            if (savedInstanceState.containsKey(TAG_DOWNLOAD_URL)) {
                downloadUrl = savedInstanceState.getString(TAG_DOWNLOAD_URL)
            }
            if (savedInstanceState.containsKey(TAG_ERROR)) {
                errorCode = savedInstanceState.getInt(TAG_ERROR)
            }
            if (savedInstanceState.containsKey(TAG_DOCUMENT_READY)) {
                isDocumentReady = savedInstanceState.getBoolean(TAG_DOCUMENT_READY, false)
            }
            if (savedInstanceState.containsKey(TAG_HARDBACK_MODE)) {
                isHardBackOn = savedInstanceState.getBoolean(TAG_HARDBACK_MODE, false)
            }
            if (savedInstanceState.containsKey(TAG_CONNECTION_LOST)) {
                isLostConnection = savedInstanceState.getBoolean(TAG_CONNECTION_LOST, false)
            }
        } else {
            isDocumentReady = false
            isHardBackOn = false
            loadWebView(uri.toString())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && connectivityManager != null) {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
        }
    }

    private fun loadWebView(url: String) {
        webView.loadUrl(url, headers)
        progressBar.visibility = View.VISIBLE
    }


    private fun setStatusBarColor() {
        when (StringUtils.getExtension(cloudFile?.fileExst ?: "")) {
            StringUtils.Extension.DOC, StringUtils.Extension.PDF -> setStatusBarColor(lib.toolkit.base.R.color.colorStatusBarDocTint)
            StringUtils.Extension.PRESENTATION -> setStatusBarColor(lib.toolkit.base.R.color.colorStatusBarPresentationTint)
            StringUtils.Extension.SHEET -> setStatusBarColor(lib.toolkit.base.R.color.colorStatusBarSheetTint)
            else -> setStatusBarColor(lib.toolkit.base.R.color.colorSecondary)
        }
    }

    private fun addJsHardBackListener() {
        webView.evaluateJavascript(SCRIPT_CALLBACK_EVENTS_JSON) { value: String ->
            Log.d(TAG, "addJsHardBackListener() : $value")
        }
    }

    private fun addJsExitCommand() {
        webView.evaluateJavascript(SCRIPT_COMMAND_BACK) { value: String ->
            Log.d(TAG, "addJsExitCommand() : $value")
        }
    }

    /*
     * WebView callback class
     * Example reload url:
     *   https://alexanderyuzhin.teamlab.info/products/files/doceditor.aspx?fileid=7052736&action=view#reload
     * Example back url:
     *   https://alexanderyuzhin.onlyoffice.eu/products/files/#1423844
     *   https://alexanderyuzhin.onlyoffice.eu/products/projects/tmdocs.aspx?prjid=587511#2371465
     * */
    private inner class WebViewCallbacks : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (request.url.toString().contains("/auth.aspx")) {
                requireActivity().finish()
                show(requireContext())
            } else if (!StringUtils.equals(uri?.host, request.url.host) && !StringUtils.equals(
                    uri?.path,
                    request.url.path
                )
            ) {
                webView.stopLoading()
                showUrlInBrowser(request.url.toString())
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            val uri = Uri.parse(url)
            if (isPageLoad && StringUtils.equals(this@WebViewerFragment.uri?.host, uri.host) &&
                (url.lowercase().matches(Regex(PATTERN_BACK_1)) ||
                        url.lowercase(Locale.ROOT).matches(Regex(PATTERN_BACK_2)) ||
                        url.lowercase(Locale.ROOT).matches(Regex(PATTERN_BACK_3)))
            ) {
                requireActivity().finish()
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            addJsHardBackListener()
            progressBar.visibility = View.INVISIBLE
            swipeRefresh.isEnabled = false
            isPageLoad = true
        }

        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            progressBar.visibility = View.INVISIBLE
            this@WebViewerFragment.errorCode = errorCode
            if (!NetworkUtils.isOnline(requireContext())) {
                showSnackBarWithAction(
                    R.string.errors_connection_error, R.string.operation_snackbar_return
                ) { onBackPressed() }
            } else {
                swipeRefresh.isEnabled = true
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            onReceivedError(view, error.errorCode, error.description.toString(), request.url.toString())
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (errorResponse.statusCode >= ApiContract.HttpCodes.SERVER_ERROR) {
                swipeRefresh.isEnabled = true
            }
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            if (!networkSettings.getSslState()) {
                handler.proceed()
            } else {
                super.onReceivedSslError(view, handler, error)
            }
        }
    }

    /*
     * WebView download callback
     * Example:
     *   https://doc.onlyoffice.eu/cache/files/TVX_cPyfJuNK3wcEbFA__6840/output.pdf/Name.name.name.pdf?md5=2DYh9yN5kziiKkb1oXMx-w==&expires=1524920139&disposition=attachment&ooname=output.pdfb
     * */
    private inner class WebViewDownload : DownloadListener {
        override fun onDownloadStart(
            url: String,
            userAgent: String,
            contentDisposition: String,
            mimetype: String,
            contentLength: Long
        ) {
            downloadUrl(url)
        }
    }

    private fun downloadUrl(url: String) {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                // Get name without extension
                val path = Uri.parse(url).path
                val elements = path?.split("/")?.toTypedArray()
                val length = elements?.size ?: 0
                val fileName = elements?.get(if (length > 0) length - 1 else 0) ?: ""
                val title = StringUtils.getNameWithoutExtension(cloudFile!!.title)
                if (fileName.isNotEmpty() && !fileName.startsWith(title)) {
                    FirebaseUtils.addCrash(WebViewDownload::class.java.simpleName + " - wrong file name!")
                    FirebaseUtils.addCrash("Url: $url")
                    FirebaseUtils.addCrash("File name: $fileName")
                    showSnackBar(R.string.errors_viewer_download_name)
                    return@registerForActivityResult
                }
                downloadUrl = url
                downloadFile.launch(fileName)
            }
        }.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun downloadFile(uri: Uri) {
        showWaitingDialog(getString(R.string.download_manager_progress_title))
        downloadUrl?.let {
            FileUtils.downloadFromUrl(requireContext(), uri, it, object : FileUtils.Finish {
                override fun onFinish() {
                    hideDialog()
                    showSnackBar(R.string.download_manager_complete)
                }
            }, object : FileUtils.Error {
                override fun onError(message: String) {
                    showSnackBar(R.string.download_manager_error)
                    Log.d(TAG, "downloadError: $message")
                }
            })
        }
    }

    /*
     * WebView file chooser
     * */
    private inner class WebViewChromeClient : WebChromeClient() {

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            when (errorCode) {
                WebViewClient.ERROR_CONNECT, WebViewClient.ERROR_TIMEOUT -> {
                    AlertDialog.Builder(view.context)
                        .setTitle(android.R.string.dialog_alert_title)
                        .setMessage(message.trimIndent())
                        .setPositiveButton(
                            android.R.string.ok
                        ) { _: DialogInterface?, _: Int -> requireActivity().finish() }
                        .setCancelable(false)
                        .create()
                        .show()
                    return true
                }
            }
            return super.onJsAlert(view, url, message, result)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            Log.d(TAG, "onConsoleMessage(): " + consoleMessage.message())
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onShowFileChooser(
            webView: WebView,
            uploadMsg: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            readPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            valueCallback = uploadMsg
            return true
        }
    }

    private fun setValueCallback(uris: Array<Uri>?) {
        valueCallback?.let { callback ->
            uris?.let {
                callback.onReceiveValue(it)
            }
        }
    }

    /*
     * Events interface
     * */
    inner class WebViewEventsInterface {

        @JavascriptInterface
        fun events(json: String) {
            Log.d(TAG, "events(): $json")
            try {
                val jsonObject = StringUtils.getJsonObject(json)
                if (jsonObject != null) {
                    if (jsonObject.has(KEY_EVENT)) {
                        val event = jsonObject[KEY_EVENT].toString()
                        if (event == EVENT_REQUEST_EDIT) {
                            isHardBackOn = false
                        } else if (event.equals(EVENT_APP_READY, ignoreCase = true)) { // App ready
                            isDocumentReady = false
                            isHardBackOn = false
                        } else if (event.equals(EVENT_DOCUMENT_READY, ignoreCase = true)) { // Document ready
                            isDocumentReady = true
                        } else if (event.equals(EVENT_INTERNAL, ignoreCase = true)) { // Internals events
                            val eventObject = jsonObject[KEY_DATA] as JSONObject
                            val type = eventObject[KEY_TYPE].toString()
                            if (type.equals(TYPE_EVENT_MODE, ignoreCase = true)
                            ) { // Catch for hardback mode ON
                                isHardBackOn = true
                            } else if (type.equals(TYPE_EVENT_EXIT, ignoreCase = true)) { // Catch for exit
                                val isFinish = eventObject.getBoolean(KEY_DATA)
                                if (!isDocumentReady || isFinish) {
                                    requireActivity().finish()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                FirebaseUtils.addCrash(e)
            }
        }

        @JavascriptInterface
        fun isHardBackOn(isHardBackOn: Boolean) {
            Log.d(TAG, "isHardBackOn(): $isHardBackOn")
            this@WebViewerFragment.isHardBackOn = isHardBackOn
        }

        @JavascriptInterface
        fun onHardBack(isHardBack: Boolean) {
            Log.d(TAG, "onHardBack(): $isHardBack")
            if (isHardBackOn && isHardBack) {
                requireActivity().finish()
            }
        }
    }


}

internal class CreateDocument :
    ActivityResultContract<String?, Uri?>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }

    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT)
            .setType(StringUtils.getMimeTypeFromPath(input ?: "*/*"))
            .putExtra(Intent.EXTRA_TITLE, input)
    }
}

internal class ImagePick :
    ActivityResultContract<String?, Uri?>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }

    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "image/*"
        }
    }
}