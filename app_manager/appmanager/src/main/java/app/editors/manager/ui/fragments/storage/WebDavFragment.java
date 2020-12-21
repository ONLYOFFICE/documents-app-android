package app.editors.manager.ui.fragments.storage;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.LinearLayoutCompat;

import moxy.presenter.InjectPresenter;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.presenters.storage.ConnectPresenter;
import app.editors.manager.mvp.views.storage.ConnectView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.StorageActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

public class WebDavFragment extends BaseAppFragment implements ConnectView {

    public static final String TAG = WebDavFragment.class.getSimpleName();
    public static final String TAG_URL = "TAG_MEDIA";
    public static final String TAG_TITLE = "TAG_TITLE";
    public static final String TAG_PROVIDER_KEY = "TAG_PROVIDER_KEY";

    @BindView(R.id.storage_web_dav_layout)
    protected LinearLayoutCompat mStorageWebDavLayout;
    @BindView(R.id.storage_web_dav_url_edit)
    protected AppCompatEditText mStorageWebDavUrlEdit;
    @BindView(R.id.storage_web_dav_url_layout)
    protected TextInputLayout mStorageWebDavUrlLayout;
    @BindView(R.id.storage_web_dav_login_edit)
    protected AppCompatEditText mStorageWebDavLoginEdit;
    @BindView(R.id.storage_web_dav_login_layout)
    protected TextInputLayout mStorageWebDavLoginLayout;
    @BindView(R.id.storage_web_dav_password_edit)
    protected AppCompatEditText mStorageWebDavPasswordEdit;
    @BindView(R.id.storage_web_dav_password_layout)
    protected TextInputLayout mStorageWebDavPasswordLayout;
    @BindView(R.id.storage_web_dav_title_edit)
    protected AppCompatEditText mStorageWebDavTitleEdit;
    @BindView(R.id.storage_web_dav_title_layout)
    protected TextInputLayout mStorageWebDavTitleLayout;
    @BindView(R.id.storage_web_dav_save_button)
    protected AppCompatButton mStorageWebDavSaveButton;

    @Inject
    PreferenceTool mPreferenceTool;

    @InjectPresenter
    ConnectPresenter mConnectPresenter;

    private Unbinder mUnbinder;
    private StorageActivity mStorageActivity;
    private FieldsWatcher mFieldsWatcher;
    private String mProviderKey;
    private String mUrl;
    private String mTitle;

    public static WebDavFragment newInstance(final String providerKey, final String url, final String title) {
        final WebDavFragment connectFragment = new WebDavFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(TAG_URL, url);
        bundle.putString(TAG_TITLE, title);
        bundle.putString(TAG_PROVIDER_KEY, providerKey);
        connectFragment.setArguments(bundle);
        return connectFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
        try {
            mStorageActivity = (StorageActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(ConnectFragment.class.getSimpleName() + " - must implement - " +
                    StorageActivity.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_storage_web_dav, container, false);
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

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        mConnectPresenter.cancelRequest();
    }

    @OnClick(R.id.storage_web_dav_save_button)
    public void onSaveClick() {
        final String url = mStorageWebDavUrlEdit.getText().toString();
        final String login = mStorageWebDavLoginEdit.getText().toString();
        final String password = mStorageWebDavPasswordEdit.getText().toString();
        final String title = mStorageWebDavTitleEdit.getText().toString();
        showWaitingDialog(getString(R.string.dialogs_wait_title_storage));
        mConnectPresenter.connectWebDav(mProviderKey, url, login, password, title, !mStorageActivity.isMySection());
    }

    @OnEditorAction(R.id.storage_web_dav_title_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSaveClick();
            return true;
        }

        return false;
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        showSnackBar(message);
    }

    @Override
    public void onUnauthorized(@Nullable String message) {
        getActivity().finish();
        MainActivity.show(getContext());
    }

    @Override
    public void onConnect(Folder folder) {
        hideDialog();
        mStorageActivity.finishWithResult(folder);
    }

    private void init(final Bundle savedInstanceState) {
        getArgs();
        setActionBarTitle(mTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initViews();
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        mUrl = bundle.getString(TAG_URL);
        mTitle = bundle.getString(TAG_TITLE);
        mProviderKey = bundle.getString(TAG_PROVIDER_KEY);
    }

    private void initViews() {
        mFieldsWatcher = new FieldsWatcher();
        mStorageWebDavUrlEdit.addTextChangedListener(mFieldsWatcher);
        mStorageWebDavLoginEdit.addTextChangedListener(mFieldsWatcher);
        mStorageWebDavPasswordEdit.addTextChangedListener(mFieldsWatcher);
        mStorageWebDavUrlEdit.setText(mUrl);
        mStorageWebDavTitleEdit.setText(mTitle);
        hideUrlLayout();
    }

    private void hideUrlLayout() {
        if (mUrl != null && !mUrl.equals("")) {
            mStorageWebDavUrlLayout.setVisibility(View.GONE);
            mStorageWebDavUrlEdit.setVisibility(View.GONE);
        }
    }

    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            final String url = mStorageWebDavUrlEdit.getText().toString();
            final String login = mStorageWebDavLoginEdit.getText().toString();
            final String password = mStorageWebDavPasswordEdit.getText().toString();
            final String title = mStorageWebDavPasswordEdit.getText().toString();

            if (!"".equals(url) && !"".equals(login) && !"".equals(password) && !"".equals(title)) {
                mStorageWebDavSaveButton.setEnabled(true);
            } else {
                mStorageWebDavSaveButton.setEnabled(false);
            }
        }
    }

}
