package app.editors.manager.ui.fragments.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.webview.KeyboardWebView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.UiUtils;

public class WebViewerFragment extends BaseAppFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = WebViewerFragment.class.getSimpleName();

    private static final int REQUEST_DOWNLOAD = 100;

    private static final int MESSAGE_LOST = 1;
    private static final int MESSAGE_AVAILABLE = 2;

    private static final String TAG_FILE = "TAG_FILE";
    private static final String TAG_DOWNLOAD_URL = "TAG_DOWNLOAD_URL";
    private static final String TAG_WEB_VIEW = "TAG_WEB_VIEW";
    private static final String TAG_PAGE_LOAD = "TAG_PAGE_LOAD";
    private static final String TAG_ERROR = "TAG_ERROR";
    private static final String TAG_DOCUMENT_READY = "TAG_DOCUMENT_READY";
    private static final String TAG_HARDBACK_MODE = "TAG_HARDBACK_MODE";
    private static final String TAG_CONNECTION_LOST = "TAG_CONNECTION_LOST";

    private static final String PATTERN_BACK_1 = ".*/files/#.*";
    private static final String PATTERN_BACK_3 = ".*/Files/#.*";
    private static final String PATTERN_BACK_2 = ".*projects.*\\d+#\\d+";

    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

    protected Unbinder mUnbinder;
    @BindView(R.id.web_viewer_layout)
    protected SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.web_viewer_webview)
    protected KeyboardWebView mWebView;
    @BindView(R.id.web_viewer_progress)
    protected ProgressBar mProgressBar;

    private File mFile;
    private Uri mUri;
    private String mDownloadUrl;
    private boolean mIsPageLoad;
    private int mErrorCode = WebViewClient.ERROR_UNKNOWN;
    private static ValueCallback<Uri[]> sValueCallback;
    private WebViewEventsInterface mWebViewEventsInterface;
    private boolean mIsDocumentReady;
    private boolean mIsHardBackOn;
    private boolean mIsDesktopMode;
    private boolean mIsLostConnection = false;

    @Nullable
    private ConnectivityManager mConnectivityManager = null;

    @Inject
    protected PreferenceTool mPreferenceTool;

    private Handler mConnectivityHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_LOST:
                    mConnectivityHandler.post(() -> {
                        mIsLostConnection = true;
                        mWebView.stopLoading();
                    });
                    break;
                case MESSAGE_AVAILABLE:
                    mConnectivityHandler.post(() -> {
                        if (mIsLostConnection) {
                            mIsLostConnection = false;
                            mIsDocumentReady = false;
                            mIsHardBackOn = false;
                            loadWebView(mUri.toString());
                        }
                    });
                    break;
            }

        }

    };

    private ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {

        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Message message = new Message();
            message.what = MESSAGE_AVAILABLE;
            mConnectivityHandler.handleMessage(message);

        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Message message = new Message();
            message.what = MESSAGE_LOST;
            mConnectivityHandler.handleMessage(message);
        }
    };

    public static WebViewerFragment newInstance(final File file) {
        final WebViewerFragment fileWebViewerFragment = new WebViewerFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_FILE, file);
        fileWebViewerFragment.setArguments(bundle);
        return fileWebViewerFragment;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean currentMode = UiUtils.checkDeXEnabled(getResources().getConfiguration());
        if (currentMode != mIsDesktopMode) {
            mIsDesktopMode = currentMode;

            Intent intent = new Intent();
            intent.putExtra(TAG_FILE, mFile);
            requireActivity().setResult(Activity.RESULT_OK, intent);
            requireActivity().finish();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_viewer_web, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BaseAppActivity.REQUEST_ACTIVITY_IMAGE_PICKER: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    setValueCallback(new Uri[]{data.getData()});
                } else if (sValueCallback != null) {
                    setValueCallback(new Uri[]{});
                }
                break;
            }
            case REQUEST_DOWNLOAD: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.getData() != null && mDownloadUrl != null) {
                        downloadFile(data.getData());
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_WRITE_STORAGE: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mDownloadUrl != null) {
                        downloadUrl(mDownloadUrl);
                    }
                }
                break;
            }

            case PERMISSION_READ_STORAGE: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImagesPickerActivity();
                } else {
                    setValueCallback(new Uri[]{});
                }
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TAG_PAGE_LOAD, mIsPageLoad);
        outState.putBoolean(TAG_DOCUMENT_READY, mIsDocumentReady);
        outState.putBoolean(TAG_HARDBACK_MODE, mIsHardBackOn);
        outState.putInt(TAG_ERROR, mErrorCode);
        outState.putBoolean(TAG_CONNECTION_LOST, mIsLostConnection);

        // Save WebView state
        if (mWebView != null) {
            final Bundle bundle = new Bundle();
            mWebView.saveState(bundle);
            outState.putBundle(TAG_WEB_VIEW, bundle);
        }

        // Save download url
        if (mDownloadUrl != null) {
            outState.putString(TAG_DOWNLOAD_URL, mDownloadUrl);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mIsHardBackOn) {
            addJsExitCommand();
            return true;
        }
        if (mWebView.canGoBack() && (mIsDesktopMode || UiUtils.isHuaweiDesktopMode(getResources().getConfiguration()))) {
            mWebView.goBack();
            return true;
        }
        requireActivity().finish();
        return super.onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWebView.removeJavascriptInterface(WebViewEventsInterface.INTERFACE);
        mWebView.setWebViewClient(null);
        mWebView.setDownloadListener(null);
        mWebView.setWebChromeClient(null);
        mUnbinder.unbind();
        mWebView = null;
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mConnectivityManager != null) {
           mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
       }
    }

    @Override
    public void onRefresh() {
        mSwipeRefresh.setRefreshing(false);
        loadWebView(mUri.toString());
    }

    private void init(final Bundle savedInstanceState) {
        mConnectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mIsDesktopMode = UiUtils.checkDeXEnabled(getResources().getConfiguration());

        mIsPageLoad = false;
        mSwipeRefresh.setOnRefreshListener(this);
        mWebViewEventsInterface = new WebViewEventsInterface(getContext());
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent),
                PorterDuff.Mode.SRC_IN);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewCallbacks());
        mWebView.setDownloadListener(new WebViewDownload());
        mWebView.setWebChromeClient(new WebViewChromeClient());
        mWebView.clearCache(true);
        mWebView.clearHistory();
        mWebView.clearFormData();
        mWebView.addJavascriptInterface(mWebViewEventsInterface, WebViewEventsInterface.INTERFACE);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        if (mIsDesktopMode || UiUtils.isHuaweiDesktopMode(getResources().getConfiguration())) {
            mWebView.getSettings().setUserAgentString(DESKTOP_USER_AGENT);
        }
        NetworkUtils.clearCookies(requireContext());
        getArgs();
        setStatusBarColor();
        restoreStates(savedInstanceState);
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        mFile = (File) bundle.getSerializable(TAG_FILE);
        if (mFile.isReadOnly()) {
            mUri = Uri.parse(mFile.getWebUrl());
            InputMethodManager im = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(mWebView.getWindowToken(), 0);
            if (mUri.getQueryParameter(Api.Parameters.ARG_ACTION) == null) {
                mUri = mUri.buildUpon()
                        .appendQueryParameter(Api.Parameters.ARG_ACTION, Api.Parameters.VAL_ACTION_VIEW).build();
            }
        } else {
            mUri = Uri.parse(mFile.getWebUrl());
        }
    }

    private void restoreStates(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_WEB_VIEW)) {
                final Bundle bundle = savedInstanceState.getBundle(TAG_WEB_VIEW);
                mProgressBar.setVisibility(View.VISIBLE);
                mWebView.restoreState(bundle);
            }

            if (savedInstanceState.containsKey(TAG_PAGE_LOAD)) {
                mIsPageLoad = savedInstanceState.getBoolean(TAG_PAGE_LOAD);
            }

            if (savedInstanceState.containsKey(TAG_DOWNLOAD_URL)) {
                mDownloadUrl = savedInstanceState.getString(TAG_DOWNLOAD_URL);
            }

            if (savedInstanceState.containsKey(TAG_ERROR)) {
                mErrorCode = savedInstanceState.getInt(TAG_ERROR);
            }

            if (savedInstanceState.containsKey(TAG_DOCUMENT_READY)) {
                mIsDocumentReady = savedInstanceState.getBoolean(TAG_DOCUMENT_READY, false);
            }

            if (savedInstanceState.containsKey(TAG_HARDBACK_MODE)) {
                mIsHardBackOn = savedInstanceState.getBoolean(TAG_HARDBACK_MODE, false);
            }
            if (savedInstanceState.containsKey(TAG_CONNECTION_LOST)) {
                mIsLostConnection = savedInstanceState.getBoolean(TAG_CONNECTION_LOST, false);
            }
        } else {
            mIsDocumentReady = false;
            mIsHardBackOn = false;
            loadWebView(mUri.toString());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mConnectivityManager != null)  {
            mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
        }
    }

    private void loadWebView(final String url) {
        mWebView.loadUrl(url, getHeaders());
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.convertServerVersion(mPreferenceTool.getServerVersion()) >= 10) {
            headers.put(Api.HEADER_AUTHORIZATION, "Bearer " + mPreferenceTool.getToken());
        } else {
            headers.put(Api.HEADER_AUTHORIZATION, mPreferenceTool.getToken());
        }

        headers.put(Api.HEADER_ACCEPT, Api.VALUE_ACCEPT);
        return headers;
    }

    private void setStatusBarColor() {
        switch (StringUtils.getExtension(mFile.getFileExst())) {
            case DOC:
            case PDF:
                setStatusBarColor(R.color.colorStatusBarDocTint);
                break;
            case PRESENTATION:
                setStatusBarColor(R.color.colorStatusBarPresentationTint);
                break;
            case SHEET:
                setStatusBarColor(R.color.colorStatusBarSheetTint);
                break;
        }
    }


    private void addJsHardBackListener() {
        mWebView.evaluateJavascript(WebViewEventsInterface.SCRIPT_CALLBACK_EVENTS_JSON,
                value -> Log.d(TAG, "addJsHardBackListener() : " + value));
    }

    private void addJsExitCommand() {
        mWebView.evaluateJavascript(WebViewEventsInterface.SCRIPT_COMMAND_BACK,
                value -> Log.d(TAG, "addJsExitCommand() : " + value));
    }

    /*
     * WebView callback class
     * Example reload url:
     *   https://alexanderyuzhin.teamlab.info/products/files/doceditor.aspx?fileid=7052736&action=view#reload
     * Example back url:
     *   https://alexanderyuzhin.onlyoffice.eu/products/files/#1423844
     *   https://alexanderyuzhin.onlyoffice.eu/products/projects/tmdocs.aspx?prjid=587511#2371465
     * */
    private class WebViewCallbacks extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().toString().contains("/auth.aspx")) {
                requireActivity().finish();
                MainActivity.show(getContext());
            } else if (!StringUtils.equals(mUri.getHost(), request.getUrl().getHost()) && !StringUtils.equals(mUri.getPath(), request.getUrl().getPath())) {
                mWebView.stopLoading();
                showUrlInBrowser(request.getUrl().toString());
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            final Uri uri = Uri.parse(url);
            if (mIsPageLoad && StringUtils.equals(mUri.getHost(), uri.getHost()) &&
                    (url.toLowerCase().matches(PATTERN_BACK_1) ||
                            url.toLowerCase().matches(PATTERN_BACK_2) ||
                            url.toLowerCase().matches(PATTERN_BACK_3))) {
                requireActivity().finish();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            addJsHardBackListener();
            mProgressBar.setVisibility(View.INVISIBLE);
            mSwipeRefresh.setEnabled(false);
            mIsPageLoad = true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mErrorCode = errorCode;
            if (!NetworkUtils.isOnline(requireContext())) {
                showSnackBarWithAction(R.string.errors_connection_error, R.string.operation_snackbar_return,
                        v -> onBackPressed());
            } else {
                mSwipeRefresh.setEnabled(true);
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (errorResponse.getStatusCode() >= Api.HttpCodes.SERVER_ERROR) {
                mSwipeRefresh.setEnabled(true);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (!mPreferenceTool.getSslState()) {
                handler.proceed();
            } else {
                super.onReceivedSslError(view, handler, error);
            }
        }
    }

    /*
     * WebView download callback
     * Example:
     *   https://doc.onlyoffice.eu/cache/files/TVX_cPyfJuNK3wcEbFA__6840/output.pdf/Name.name.name.pdf?md5=2DYh9yN5kziiKkb1oXMx-w==&expires=1524920139&disposition=attachment&ooname=output.pdfb
     * */
    private class WebViewDownload implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            downloadUrl(url);
        }
    }

    private void downloadUrl(final String url) {
        if (!checkWritePermission()) {
            mDownloadUrl = url;
            return;
        }
        // Get name without extension
        final String path = Uri.parse(url).getPath();
        final String[] elements = path.split("/");
        final int length = elements.length;
        final String fileName = elements[length > 0 ? length - 1 : 0];
        final String title = StringUtils.getNameWithoutExtension(mFile.getTitle());
        if (!fileName.startsWith(title)) {
            FirebaseUtils.addCrash(WebViewDownload.class.getSimpleName() + " - wrong file name!");
            FirebaseUtils.addCrash("Url: " + url);
            FirebaseUtils.addCrash("File name: " + fileName);
            showSnackBar(R.string.errors_viewer_download_name);
            return;
        }

        mDownloadUrl = url;
        showSaveActivity(fileName);
    }

    private void showSaveActivity(String name) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(StringUtils.getMimeTypeFromPath(name));
        intent.putExtra(Intent.EXTRA_TITLE, name);
        startActivityForResult(intent, REQUEST_DOWNLOAD);
    }

    private void downloadFile(@NonNull Uri uri) {
        showWaitingDialog(getString(R.string.download_manager_progress_title));
        FileUtils.downloadFromUrl(requireContext(), uri, mDownloadUrl, () -> {
            hideDialog();
            showSnackBar(R.string.download_manager_complete);
        }, message -> {
            showSnackBar(R.string.download_manager_error);
            Log.d(TAG, "downloadError: " + message);
        });
    }

    /*
     * WebView file chooser
     * */
    private class WebViewChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            switch (mErrorCode) {
                case WebViewClient.ERROR_CONNECT:
                case WebViewClient.ERROR_TIMEOUT:
                    new AlertDialog.Builder(view.getContext())
                            .setTitle(android.R.string.dialog_alert_title)
                            .setMessage("\n" + message)
                            .setPositiveButton(android.R.string.ok,
                                    (dialog, which) -> requireActivity().finish())
                            .setCancelable(false)
                            .create()
                            .show();
                    return true;
            }

            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(TAG, "onConsoleMessage(): " + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, FileChooserParams fileChooserParams) {
            if (checkReadPermission()) {
                showImagesPickerActivity();
            }

            sValueCallback = uploadMsg;
            return true;
        }

    }

    private void setValueCallback(final Uri[] uris) {
        if (sValueCallback != null && uris != null) {
            sValueCallback.onReceiveValue(uris);
        }
    }

    /*
     * Events interface
     * */
    public class WebViewEventsInterface {

        public static final String INTERFACE = "Android";
        public static final String KEY_EVENT = "event";
        public static final String KEY_DATA = "data";
        public static final String KEY_TYPE = "type";
        public static final String EVENT_COMMON = "message";
        public static final String EVENT_APP_READY = "onAppReady";
        public static final String EVENT_DOCUMENT_READY = "onDocumentReady";
        public static final String EVENT_INTERNAL = "onInternalMessage";
        public static final String EVENT_REQUEST_EDIT = "onRequestEditRights";
        public static final String TYPE_EVENT_MODE = "listenHardBack";
        public static final String TYPE_EVENT_EXIT = "hardBack";

        /*
         * Scripts for execute
         * */
        public static final String SCRIPT_TEST = "(function() { alert('Hello world!'); })();";
        public static final String SCRIPT_COMMAND_BACK = "window.ASC.Files.Editor.docEditor.serviceCommand('" + TYPE_EVENT_EXIT + "');";
        public static final String SCRIPT_CALLBACK_EVENTS_JSON = "window.addEventListener('" +
                EVENT_COMMON + "', function(event){ " +
                "if (event) {" +
                INTERFACE + ".events(JSON.stringify(event.data));" +
                "}" +
                "}, {once: false});";

        public static final String SCRIPT_CALLBACK_EVENTS_PARSED = "window.addEventListener('" +
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
                "}}, {once: false});";

        private Context mContext;

        public WebViewEventsInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void events(final String json) {
            Log.d(TAG, "events(): " + json);
            try {
                final JSONObject jsonObject = StringUtils.getJsonObject(json);
                if (jsonObject != null) {
                    if (jsonObject.has(KEY_EVENT)) {
                        final String event = String.valueOf(jsonObject.get(KEY_EVENT));
                        if (event.equals(EVENT_REQUEST_EDIT)) {
                            mIsHardBackOn = false;
                        } else if (event.equalsIgnoreCase(EVENT_APP_READY)) { // App ready
                            mIsDocumentReady = false;
                            mIsHardBackOn = false;
                        } else if (event.equalsIgnoreCase(EVENT_DOCUMENT_READY)) { // Document ready
                            mIsDocumentReady = true;
                        } else if (event.equalsIgnoreCase(EVENT_INTERNAL)) { // Internals events
                            final JSONObject eventObject = (JSONObject) jsonObject.get(KEY_DATA);
                            final String type = String.valueOf(eventObject.get(KEY_TYPE));
                            if (type.equalsIgnoreCase(TYPE_EVENT_MODE)) { // Catch for hardback mode ON
                                mIsHardBackOn = true;
                            } else if (type.equalsIgnoreCase(TYPE_EVENT_EXIT)) { // Catch for exit
                                final boolean isFinish = eventObject.getBoolean(KEY_DATA);
                                if (!mIsDocumentReady || isFinish) {
                                    requireActivity().finish();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                FirebaseUtils.addCrash(e);
            }
        }

        @JavascriptInterface
        public void isHardBackOn(boolean isHardBackOn) {
            Log.d(TAG, "isHardBackOn(): " + isHardBackOn);
            mIsHardBackOn = isHardBackOn;
        }

        @JavascriptInterface
        public void onHardBack(boolean isHardBack) {
            Log.d(TAG, "onHardBack(): " + isHardBack);
            if (mIsHardBackOn && isHardBack) {
                requireActivity().finish();
            }
        }
    }

}
