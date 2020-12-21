package app.editors.manager.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.R;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LicenseFragment extends BaseAppFragment {

    public static final String TAG = LicenseFragment.class.getSimpleName();

    protected Unbinder mUnbinder;
    @BindView(R.id.license_webview)
    protected WebView mWebview;

    public static LicenseFragment newInstance() {
        return new LicenseFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about_license, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.about_license));
        mWebview.loadUrl(getString(R.string.app_licenses_path));
    }

}
