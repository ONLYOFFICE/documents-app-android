package app.editors.manager.ui.fragments.login;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.StringUtils;

public class NextCloudLoginFragment extends BaseAppFragment {

    public static final String TAG = NextCloudLoginFragment.class.getSimpleName();

    private static final String KEY_PORTAL = "KEY_PORTAL";
    private static final String LOGIN_SUFFIX = "/index.php/login/flow";
    private static final String LOGIN_HEADER = "OCS-APIREQUEST";
    private static final String BACK_PATTERN_1 = "apps";
    private static final String BACK_PATTERN_2 = "files";

    public static NextCloudLoginFragment newInstance(String portal) {
        Bundle args = new Bundle();
        args.putString(KEY_PORTAL, portal);
        NextCloudLoginFragment fragment = new NextCloudLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.webView)
    WebView mWebView;

    @Nullable
    private Unbinder mUnbinder;
    @Nullable
    private String mPortal;
    private boolean mIsClear = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.next_cloud_login_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getArgs();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NetworkUtils.clearCookies(requireContext());
        mWebView.clearCache(true);
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            requireActivity().setResult(Activity.RESULT_CANCELED);
            requireActivity().finish();
            return super.onBackPressed();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void getArgs() {
        final Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_PORTAL)) {
            mPortal = args.getString(KEY_PORTAL);
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUserAgentString(getString(R.string.app_name_full));
        mWebView.setWebViewClient(new WebViewCallbacks());
        mWebView.setWebChromeClient(new WebViewChromeClient());
        mWebView.clearHistory();
        mWebView.clearCache(true);

        mWebView.loadUrl(mPortal + LOGIN_SUFFIX, getHeaders());
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(LOGIN_HEADER, "true");
        headers.put("USER_AGENT", getString(R.string.app_name));
        headers.put("ACCEPT_LANGUAGE", App.getLocale());
        return headers;
    }

    private class WebViewCallbacks extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (uri != null) {
                if (uri.toString().contains(BACK_PATTERN_1) || uri.toString().contains(BACK_PATTERN_2)) {
                    mIsClear = true;
                    NetworkUtils.clearCookies(requireContext());
                    mWebView.clearHistory();
                    mWebView.clearCache(true);
                    mWebView.loadUrl(mPortal + LOGIN_SUFFIX, getHeaders());
                    return true;
                }

                if (uri.getScheme() != null
                        && uri.getScheme().equals("nc")
                        && uri.getHost() != null
                        && uri.getHost().equals("login")) {
                    final String path = uri.getPath();
                    if (path != null) {
                        saveUser(path);
                        return true;
                    }
                }
            }

            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.contains("nc") && url.contains("login")) {
                saveUser(url.substring(url.indexOf(":") + 1));
            } else {
                mSwipe.setRefreshing(true);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mIsClear) {
                mWebView.clearHistory();
                mIsClear = false;
            }
            mSwipe.setRefreshing(false);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }
    }

    private void saveUser(String path) {
        final String[] args = path.split("&");
        if (args.length == 3) {
            final String portal = args[0].substring(args[0].indexOf(":") + 1);
            final String login = args[1].substring(args[1].indexOf(":") + 1);
            final String password = args[2].substring(args[2].indexOf(":") + 1);
            AccountsSqlData account = new AccountsSqlData();
            account.setWebDav(true);
            try {
                URL url = new URL(portal);

                final StringBuilder builder = new StringBuilder()
                        .append(url.getHost());

                if (url.getPath() != null && !url.getPath().isEmpty()) {
                    builder.append(url.getPath());
                    if (url.getPort() == -1) {
                        builder.append("/");
                    }
                }

                if (url.getPort() != -1) {
                    builder.append(":").append(url.getPort());
                }
                account.setPortal(builder.toString());
                account.setLogin(login);
                account.setPassword(password);
                account.setScheme(url.getProtocol() +"://");
                if (url.getPath() != null && !url.getPath().isEmpty()) {
                    account.setWebDavPath(url.getPath() + WebDavApi.Providers.NextCloud.getPath() + login + "/");
                } else {
                    account.setWebDavPath(WebDavApi.Providers.NextCloud.getPath() + login + "/");
                }
                account.setWebDavProvider(WebDavApi.Providers.NextCloud.name());
                setPreferences(account, password);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPreferences(AccountsSqlData account, String password) {
        PreferenceTool mPreferenceTool = App.getApp().getAppComponent().getPreference();
        mPreferenceTool.setDefault();
        mPreferenceTool.setLogin(account.getLogin());
        mPreferenceTool.setPassword(password);
        mPreferenceTool.setPortal(StringUtils.getUrlWithoutScheme(account.getPortal()));
        mPreferenceTool.setScheme(account.getScheme());
        login(account);
    }

    private void login(AccountsSqlData account) {
        AccountSqlTool accountSqlTool = App.getApp().getAppComponent().getAccountsSql();
        AccountsSqlData onlineAccount = accountSqlTool.getAccountOnline();
        if (onlineAccount != null) {
            onlineAccount.setOnline(false);
            accountSqlTool.setAccount(onlineAccount);
        }
        account.setOnline(true);
        accountSqlTool.setAccount(account);

        MainActivity.show(requireContext());
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }

    private class WebViewChromeClient extends WebChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            setActionBarTitle(title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            ActionBar actionBar = requireActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setIcon(new BitmapDrawable(getResources(), icon));
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            showToast(message);
            return super.onJsAlert(view, url, message, result);
        }

    }
}
