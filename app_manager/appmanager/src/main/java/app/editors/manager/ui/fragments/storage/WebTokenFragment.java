package app.editors.manager.ui.fragments.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.utils.StorageUtils;
import app.editors.manager.mvp.models.account.Storage;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.NetworkUtils;

public class WebTokenFragment extends BaseAppFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = WebTokenFragment.class.getSimpleName();
    private static final String TAG_STORAGE = "TAG_MEDIA";
    private static final String TAG_WEB_VIEW = "TAG_WEB_VIEW";
    private static final String TAG_PAGE_LOAD = "TAG_PAGE_LOAD";

    @BindView(R.id.web_storage_layout)
    protected LinearLayoutCompat mWebLayout;
    @BindView(R.id.web_storage_webview)
    protected WebView mWebView;
    @BindView(R.id.web_storage_swipe)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private Unbinder mUnbinder;
    private String mUrl;
    private Storage mStorage;
    private String mRedirectUrl;
    private boolean mIsPageLoad;

    @Inject
    PreferenceTool mPreferenceTool;

    public static WebTokenFragment newInstance(final Storage storage) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(TAG_STORAGE, storage);
        final WebTokenFragment fileViewerFragment = new WebTokenFragment();
        fileViewerFragment.setArguments(bundle);
        return fileViewerFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_storage_web, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TAG_PAGE_LOAD, mIsPageLoad);

        // Save WebView state
        if (mWebView != null) {
            final Bundle bundle = new Bundle();
            mWebView.saveState(bundle);
            outState.putBundle(TAG_WEB_VIEW, bundle);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CookieManager.getInstance().removeAllCookies(null);
        mWebView.setWebViewClient(null);
        mUnbinder.unbind();
    }

    @Override
    public void onRefresh() {
        loadWebView(mUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.storage_web_title));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mIsPageLoad = false;
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setUserAgentString(getString(R.string.google_user_agent));
        mWebView.setWebViewClient(new WebViewCallbacks());
        mWebView.clearHistory();

        getArgs();
        restoreStates(savedInstanceState);
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            mStorage = bundle.getParcelable(TAG_STORAGE);
            if (mStorage != null) {
                mUrl = StorageUtils.getStorageUrl(mStorage.getName(), mStorage.getClientId(), mStorage.getRedirectUrl());
                mRedirectUrl = mStorage.getRedirectUrl();
            }
        }

    }

    private void restoreStates(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_WEB_VIEW)) {
                final Bundle bundle = savedInstanceState.getBundle(TAG_WEB_VIEW);
                mSwipeRefreshLayout.setRefreshing(true);
                if (bundle != null) {
                    mWebView.restoreState(bundle);
                }
            }

            if (savedInstanceState.containsKey(TAG_PAGE_LOAD)) {
                mIsPageLoad = savedInstanceState.getBoolean(TAG_PAGE_LOAD);
            }
        } else {
            loadWebView(mUrl);
        }
    }

    private void loadWebView(final String url) {
        mSwipeRefreshLayout.setRefreshing(true);
        mWebView.loadUrl(url);
    }

    /*
     * WebView callback class
     * Example token response:
     *       https://service.teamlab.info/oauth2.aspx?code=4/AAAJYg3drTzabIIAPiYq_FEieoyhj7FqOjON8k0l3kEN3v5Qc3xmA_Hqp3TxSa5aiwSSToMJefTDDZcrJJLfguQ#
     *       https://login.live.com/err.srf?lc=1049#error=invalid_request&error_description=The+provided+value+for+the+input+parameter+'redirect_uri'+is+not+valid.+The+expected+value+is+'https://login.live.com/oauth20_desktop.srf'+or+a+URL+which+matches+the+redirect+URI+registered+for+this+client+application.
     *       https://login.live.com/oauth20_authorize.srf?client_id=000000004413039F&redirect_uri=https://service.teamlab.info/oauth2.aspx&response_type=code&scope=wl.signin%20wl.skydrive_update%20wl.offline_access
     * */
    private class WebViewCallbacks extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (url.startsWith(mRedirectUrl)) {
                final Uri uri = Uri.parse(url);
                final String token = uri.getQueryParameter(StorageUtils.ARG_CODE);
                if (token != null && !token.equalsIgnoreCase("null")) {
                    showFragment(ConnectFragment.newInstance(token, mStorage), ConnectFragment.TAG, false);
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSwipeRefreshLayout.setRefreshing(false);
            mIsPageLoad = true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            mSwipeRefreshLayout.setRefreshing(false);
            if (!NetworkUtils.isOnline(requireContext())) {
                showSnackBar(R.string.errors_connection_error);
            }
        }
    }

}
