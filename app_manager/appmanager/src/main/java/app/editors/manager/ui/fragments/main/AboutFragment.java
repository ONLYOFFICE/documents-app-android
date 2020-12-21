package app.editors.manager.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import app.editors.manager.BuildConfig;
import app.editors.manager.R;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.FileUtils;

public class AboutFragment extends BaseAppFragment {

    public static final String TAG = AboutFragment.class.getSimpleName();
    public static final String ASSETS_SDK_VERSION_PATH = "sdk.version";

    protected Unbinder mUnbinder;
    @BindView(R.id.about_app_version)
    protected AppCompatTextView mAboutAppVersion;
    @BindView(R.id.about_terms)
    protected RelativeLayout mAboutTerms;
    @BindView(R.id.about_policy)
    protected RelativeLayout mAboutPolicy;
    @BindView(R.id.about_license)
    protected RelativeLayout mAboutLicense;
    @BindView(R.id.about_website)
    protected RelativeLayout mAboutWebsite;

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);
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

    @OnClick({ R.id.about_terms,
                     R.id.about_policy,
                     R.id.about_license,
                     R.id.about_website})
    protected void onItemClick(final View view) {
        switch (view.getId()) {
            case R.id.about_terms:
                showUrlInBrowser(getString(R.string.app_url_terms));
                break;
            case R.id.about_policy:
                showUrlInBrowser(getString(R.string.app_url_policy));
                break;
            case R.id.about_license:
                showFragment(LicenseFragment.newInstance(), LicenseFragment.TAG, false);
                break;
            case R.id.about_website:
                showUrlInBrowser(getString(R.string.app_url_main));
                break;
        }
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.about_title));

        final String sdkVersion = FileUtils.readSdkVersion(requireContext(), ASSETS_SDK_VERSION_PATH);

        mAboutAppVersion.setText(getString(R.string.about_app_version, BuildConfig.VERSION_NAME,
                String.valueOf(BuildConfig.VERSION_CODE), sdkVersion));
    }

}