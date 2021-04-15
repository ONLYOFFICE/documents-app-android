package app.editors.manager.ui.fragments.login;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.presenters.login.EnterpriseSSOPresenter;
import app.editors.manager.mvp.views.login.EnterpriseSSOView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.NetworkUtils;
import moxy.presenter.InjectPresenter;

public class SSOLoginFragment extends BaseAppFragment implements EnterpriseSSOView {

    public static final String TAG = SSOLoginFragment.class.getSimpleName();

    public static final String KEY_URL = "KEY_URL";
    public static final String KEY_PORTAL = "KEY_PORTAL";

    public static SSOLoginFragment newInstance(String url, String portal) {
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        args.putString(KEY_PORTAL, portal);
        SSOLoginFragment fragment = new SSOLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected PreferenceTool mPreferenceTool;

    @Nullable
    private Unbinder mUnbinder;
    @Nullable
    private String mUrl;
    @Nullable
    private String mPortal;

    @InjectPresenter
    EnterpriseSSOPresenter mEnterpriseSSOPresenter;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.webView)
    WebView mWebView;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sso_login_layout, container, false);
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
        if (args != null && args.containsKey(KEY_URL) && args.containsKey(KEY_PORTAL)) {
            mUrl = args.getString(KEY_URL);
            mPortal = args.getString(KEY_PORTAL);
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUserAgentString(getString(R.string.app_name_full));
        mWebView.setWebViewClient(new SSOLoginFragment.WebViewCallbacks());
        mWebView.setWebChromeClient(new SSOLoginFragment.WebViewChromeClient());
        mWebView.clearHistory();
        mWebView.clearCache(true);

        mWebView.loadUrl(mUrl);
    }

    @Override
    public void onSuccessLogin() {
        hideDialog();
        MainActivity.show(getContext());
        getActivity().finish();
    }

    @Override
    public void onError(@Nullable String message) {
        showSnackBar(message);
    }

    private class WebViewCallbacks extends WebViewClient{

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            String[] args = url.split("=");
            if (url.contains("token")) {
                mEnterpriseSSOPresenter.signInWithSSO(args[1], mPortal);
            } else if (url.contains("error")) {
                onError(args[1]);
            }
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if(!mPreferenceTool.getSslState()) {
                handler.proceed();
            } else
            {
                super.onReceivedSslError(view, handler, error);
            }
        }
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
