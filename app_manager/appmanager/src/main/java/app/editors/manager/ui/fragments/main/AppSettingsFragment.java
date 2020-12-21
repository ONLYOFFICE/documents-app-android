package app.editors.manager.ui.fragments.main;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import app.editors.manager.R;
import app.editors.manager.mvp.presenters.main.AppSettingsPresenter;
import app.editors.manager.mvp.views.main.AppSettingsView;
import app.editors.manager.ui.activities.main.AboutActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.ActivitiesUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

public class AppSettingsFragment extends BaseAppFragment implements AppSettingsView {

    public static final String TAG = AppSettingsFragment.class.getSimpleName();

    public static AppSettingsFragment newInstance() {
        return new AppSettingsFragment();
    }

    private static final String TAG_DIALOG_TRASH = "TAG_DIALOG_TRASH";
    private static final String TAG_DIALOG_RATE_FEEDBACK = "TAG_DIALOG_RATE_FEEDBACK";

    @BindView(R.id.wifiSwitch)
    SwitchCompat mWifiSwitch;
    @BindView(R.id.clearCacheLayout)
    RelativeLayout mClearCacheLayout;
    @BindView(R.id.cacheSizeTextView)
    AppCompatTextView mCacheSizeText;
    @BindView(R.id.settingAboutItem)
    FrameLayout mAboutItem;
    @BindView(R.id.settingHelpItem)
    FrameLayout mHelpItem;
    @BindView(R.id.settingSupportItem)
    FrameLayout mSupportItem;
    @BindView(R.id.analyticSwitch)
    SwitchCompat mAnalyticSwitch;

    private Unbinder mUnbinder;

    @InjectPresenter
    AppSettingsPresenter mSettingsPresenter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_settings_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        initSettingItems();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_WRITE_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mSettingsPresenter.clearCache();
        }
    }

    @Override
    public void onSetCacheSize(String size) {
        mCacheSizeText.setText(size);
    }

    @Override
    public void onSetWifiState(boolean state) {
        mWifiSwitch.setChecked(state);
    }

    @Override
    public void onAnalyticState(boolean isAnalyticEnable) {
        mAnalyticSwitch.setChecked(isAnalyticEnable);
    }

    @Override
    public void onMessage(String message) {
        showSnackBar(message);
    }

    private void init() {
        setActionBarTitle(getString(R.string.settings_item_title));
        mSettingsPresenter.getCacheSize();
        mSettingsPresenter.initWifiState();
        mSettingsPresenter.initAnalyticState();

        mWifiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mSettingsPresenter.setWifiState(isChecked));
        mAnalyticSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mSettingsPresenter.setAnalyticState(isChecked));
    }

    private void initSettingItems() {
        AppCompatTextView text;
        AppCompatImageView image;

        text = mAboutItem.findViewById(R.id.settingText);
        image = mAboutItem.findViewById(R.id.settingIcon);
        image.setImageResource(R.drawable.ic_drawer_menu_about);
        text.setText(getString(R.string.about_title));

        text = mHelpItem.findViewById(R.id.settingText);
        image = mHelpItem.findViewById(R.id.settingIcon);
        image.setImageResource(R.drawable.drawable_ic_drawer_menu_help_fill);
        image = mHelpItem.findViewById(R.id.settingIconArrow);
        image.setImageResource(R.drawable.ic_open_in_new);
        text.setText(getString(R.string.navigation_drawer_menu_help));

        text = mSupportItem.findViewById(R.id.settingText);
        image = mSupportItem.findViewById(R.id.settingIcon);
        image.setImageResource(R.drawable.ic_drawer_menu_feedback);
        image = mSupportItem.findViewById(R.id.settingIconArrow);
        image.setImageResource(R.drawable.ic_open_in_new);
        text.setText(getString(R.string.navigation_drawer_menu_feedback));
    }

    @Override
    public void onAcceptClick(@Nullable CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_TRASH: {
                    if (checkWritePermission()) {
                        mSettingsPresenter.clearCache();
                    }
                    break;
                }
                case TAG_DIALOG_RATE_FEEDBACK: {
                    if (value != null) {
                        showEmailClientTemplate(value);
                    }
                    break;
                }
            }
        }
        hideDialog();
    }

    @OnClick({R.id.clearCacheLayout,
            R.id.settingAboutItem,
            R.id.settingHelpItem,
            R.id.settingSupportItem})
    void onClick(View view) {
        final int id = view.getId();
        switch (id) {
            case R.id.clearCacheLayout: {
                showQuestionDialog(requireContext().getString(R.string.dialog_clear_cache), null, getString(R.string.dialogs_common_ok_button),
                        getString(R.string.dialogs_common_cancel_button),
                        TAG_DIALOG_TRASH);
                break;
            }
            case R.id.settingAboutItem: {
                AboutActivity.show(getContext());
                break;
            }
            case R.id.settingHelpItem: {
                showUrlInBrowser(getString(R.string.app_url_help));
                break;
            }
            case R.id.settingSupportItem: {
                mBaseActivity.showEditMultilineDialog(getString(R.string.dialogs_edit_feedback_title)
                        , getString(R.string.dialogs_edit_feedback_rate_hint),
                        getString(R.string.dialogs_edit_feedback_rate_accept),
                        getString(R.string.dialogs_common_cancel_button),
                        TAG_DIALOG_RATE_FEEDBACK);
                break;
            }
        }
    }

    private void showEmailClientTemplate(@NonNull final String message) {
        ActivitiesUtils.showEmail(requireContext(),
                getString(R.string.chooser_email_client),
                getString(R.string.app_support_email),
                getString(R.string.about_email_subject),
                message + UiUtils.getDeviceInfoString(requireContext(), false));
    }

}
