package app.editors.manager.ui.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import app.editors.manager.R;
import app.documents.core.webdav.WebDavApi;
import app.editors.manager.managers.utils.Constants;
import app.editors.manager.mvp.models.account.Storage;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.activities.login.WebDavLoginActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.onedrive.ui.fragments.OneDriveSignInFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.UiUtils;

public class CloudsFragment extends BaseAppFragment {

    public static final String TAG = CloudsFragment.class.getSimpleName();

    @BindView(R.id.cloudsItemOnlyOffice)
    FrameLayout mOnlyOfficeItem;
    @BindView(R.id.cloudsItemNextCloud)
    FrameLayout mNextCloudItem;
    @BindView(R.id.cloudsItemYandex)
    FrameLayout mYandexItem;
    @BindView(R.id.cloudsItemOwnCloud)
    FrameLayout mOwnCloudItem;
    @BindView(R.id.cloudsItemWebDav)
    FrameLayout mWebDavItem;
    @BindView(R.id.cloudsItemOneDrive)
    FrameLayout mOneDriveItem;
    @BindView(R.id.settingIcon)
    AppCompatImageView mOnlyOfficeIcon;
    @BindView(R.id.settingText)
    AppCompatTextView mOnlyOfficeText;

    private Unbinder mUnbinder;

    private MainActivity mMainActivity;

    private static String KEY_IS_BACK = "KEY_IS_BACK";

    private boolean mIsBack = false;

    public static CloudsFragment newInstance(boolean isBack) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_IS_BACK, isBack);
        CloudsFragment fragment = new CloudsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mMainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_clouds, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(KEY_IS_BACK)) {
            mIsBack = getArguments().getBoolean(KEY_IS_BACK);
        }

        init();
    }

    void init() {
        setActionBarTitle(getString(R.string.fragment_clouds_title));
        if (mMainActivity != null) {
//            mMainActivity.setCloudsState(mIsBack);
        }
        if (getContext() != null) {
            Context context = getContext();
            final float size = UiUtils.dpToPixel(40, context);

            initNextCloudItem(context, size);
            initYandexItem(context, size);
            initOwnCloudItem(context, size);
            initWebDavItem(context, size);
            initOneDriveItem(context, size);
        }
    }


    private void initWebDavItem(Context context, float size) {
        AppCompatImageView image = mWebDavItem.findViewById(R.id.settingIcon);
        setImageSize(image, size);
        image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_storage_webdav));
        ((AppCompatTextView) mWebDavItem.findViewById(R.id.settingText)).setText(R.string.storage_select_web_dav);
    }

    private void initOwnCloudItem(Context context, float size) {
        AppCompatImageView image = mOwnCloudItem.findViewById(R.id.settingIcon);
        setImageSize(image, size);
        image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_storage_owncloud));
        ((AppCompatTextView) mOwnCloudItem.findViewById(R.id.settingText)).setText(R.string.storage_select_own_cloud);
    }

    private void initNextCloudItem(Context context, float size) {
        AppCompatImageView image = mNextCloudItem.findViewById(R.id.settingIcon);
        setImageSize(image, size);
        image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_storage_nextcloud));
        ((AppCompatTextView) mNextCloudItem.findViewById(R.id.settingText)).setText(R.string.storage_select_next_cloud);
    }

    private void initYandexItem(Context context, float size) {
        mYandexItem.setVisibility(View.GONE);
//        AppCompatImageView image = mYandexItem.findViewById(R.id.settingIcon);
//        setImageSize(image, size);
//        image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_storage_yandex));
//        ((AppCompatTextView) mYandexItem.findViewById(R.id.settingText)).setText(R.string.storage_select_yandex);
    }

    private void initOneDriveItem(Context context, float size) {
        AppCompatImageView image = mOneDriveItem.findViewById(R.id.settingIcon);
        setImageSize(image, size);
        image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_storage_onedrive));
        ((AppCompatTextView) mOneDriveItem.findViewById(R.id.settingText)).setText(R.string.storage_select_one_drive);
    }

    private void setImageSize(AppCompatImageView image, float size) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) image.getLayoutParams();
        params.height = (int) size;
        params.width = (int) size;
        image.setLayoutParams(params);
    }

    @OnClick({R.id.cloudsItemOnlyOffice, R.id.cloudsItemNextCloud,
            R.id.cloudsItemYandex, R.id.cloudsItemOwnCloud, R.id.cloudsItemWebDav, R.id.cloudsItemOneDrive})
    public void onClick(View view) {
        if (getContext() != null && NetworkUtils.isOnline(getContext())) {
            switch (view.getId()) {
                case R.id.cloudsItemOnlyOffice:
                    PortalsActivity.show(getActivity());
                    break;
                case R.id.cloudsItemNextCloud:
                    WebDavLoginActivity.show(getActivity(), WebDavApi.Providers.NextCloud, null);
                    break;
                case R.id.cloudsItemYandex:
                    WebDavLoginActivity.show(getActivity(), WebDavApi.Providers.Yandex, null);
                    break;
                case R.id.cloudsItemOwnCloud:
                    WebDavLoginActivity.show(getActivity(), WebDavApi.Providers.OwnCloud, null);
                    break;
                case R.id.cloudsItemWebDav:
                    WebDavLoginActivity.show(getActivity(), WebDavApi.Providers.WebDav, null);
                    break;
                case R.id.cloudsItemOneDrive:
                    Storage storage = new Storage("OneDrive", Constants.OneDrive.COM_CLIENT_ID, Constants.OneDrive.COM_REDIRECT_URL);
                    showFragment(OneDriveSignInFragment.Companion.newInstance(storage), OneDriveSignInFragment.Companion.getTAG(), false);
                    break;
            }
        } else {
            showSnackBar(R.string.errors_connection_error);
        }
    }
}
