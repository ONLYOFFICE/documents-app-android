package app.editors.manager.ui.fragments.storage;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.Storage;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.presenters.storage.ConnectPresenter;
import app.editors.manager.mvp.views.storage.ConnectView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.StorageActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

public class ConnectFragment extends BaseAppFragment implements ConnectView {

    public static final String TAG = ConnectFragment.class.getSimpleName();
    public static final String TAG_TOKEN = "TAG_TOKEN";
    public static final String TAG_STORAGE = "TAG_MEDIA";

    @BindView(R.id.storage_connect_title_edit)
    protected AppCompatEditText mTitleEdit;
    @BindView(R.id.storage_connect_title_layout)
    protected TextInputLayout mTitleEditLayout;
    @BindView(R.id.storage_connect_save)
    protected AppCompatButton mSaveButton;

    @InjectPresenter
    ConnectPresenter mConnectPresenter;

    @Inject
    PreferenceTool mPreferenceTool;

    private Unbinder mUnbinder;
    private StorageActivity mStorageActivity;
    private Storage mStorage;
    private String mToken;

    public static ConnectFragment newInstance(final String token, final Storage storage) {
        final ConnectFragment connectFragment = new ConnectFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(TAG_TOKEN, token);
        bundle.putParcelable(TAG_STORAGE, storage);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_storage_connect, container, false);
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

    @OnClick(R.id.storage_connect_save)
    public void onSaveClick() {
        final String storageTitle = mTitleEdit.getText().toString();
        if (!storageTitle.isEmpty()) {
            showWaitingDialog(getString(R.string.dialogs_wait_title_storage));
            mConnectPresenter.connectService(mToken, mStorage.getName(), storageTitle, !mStorageActivity.isMySection());
        } else {
            showSnackBar(R.string.storage_connect_empty_title);
        }
    }

    @OnEditorAction(R.id.storage_connect_title_edit)
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
        setActionBarTitle(getString(R.string.storage_connect_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getArgs();
        initViews();
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        mToken = bundle.getString(TAG_TOKEN);
        mStorage = bundle.getParcelable(TAG_STORAGE);
    }

    private void initViews() {
        String title = getString(R.string.storage_connect_title);
        title = title.concat(" ");
        // Set default title
        switch (mStorage.getName()) {
            case Api.Storage.BOXNET:
                mTitleEdit.setText(R.string.storage_select_box);
                title = title.concat(getString(R.string.storage_select_box));
                break;
            case Api.Storage.DROPBOX:
                mTitleEdit.setText(R.string.storage_select_drop_box);
                title = title.concat(getString(R.string.storage_select_drop_box));
                break;
            case Api.Storage.SHAREPOINT:
                mTitleEdit.setText(R.string.storage_select_share_point);
                title = title.concat(getString(R.string.storage_select_share_point));
                break;
            case Api.Storage.GOOGLEDRIVE:
                mTitleEdit.setText(R.string.storage_select_google_drive);
                title = title.concat(getString(R.string.storage_select_google_drive));
                break;
            case Api.Storage.ONEDRIVE:
                mTitleEdit.setText(R.string.storage_select_one_drive);
                title = title.concat(getString(R.string.storage_select_one_drive));
                break;
            case Api.Storage.YANDEX:
                mTitleEdit.setText(R.string.storage_select_yandex);
                title = title.concat(getString(R.string.storage_select_yandex));
                break;
            case Api.Storage.WEBDAV:
                mTitleEdit.setText(R.string.storage_select_web_dav);
                title = title.concat(getString(R.string.storage_select_web_dav));
                break;
        }
        setActionBarTitle(title);
    }

}
