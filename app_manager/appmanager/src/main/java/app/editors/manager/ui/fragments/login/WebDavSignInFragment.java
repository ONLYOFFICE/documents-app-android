package app.editors.manager.ui.fragments.login;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.textfield.TextInputLayout;

import app.editors.manager.R;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.login.WebDavSignInPresenter;
import app.editors.manager.mvp.views.login.WebDavSignInView;
import app.editors.manager.ui.activities.login.NextCloudLoginActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import moxy.presenter.InjectPresenter;

public class WebDavSignInFragment extends BaseAppFragment implements WebDavSignInView {

    public static final String TAG = WebDavSignInFragment.class.getSimpleName();
    private static final String KEY_PROVIDER = "KEY_PROVIDER";
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";

    private static final String KEY_PORTAL = "KEY_PORTAL";
    private static final String KEY_LOGIN = "KEY_LOGIN";
    private static final String KEY_PASSWORD = "KEY_PASSWORD";

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

    private Unbinder mUnbinder;

    private WebDavApi.Providers mProvider;
    private AccountsSqlData mAccount;
    private TextWatcher mFieldsWatcher;

    @InjectPresenter
    WebDavSignInPresenter mPresenter;

    public static WebDavSignInFragment newInstance(WebDavApi.Providers provider, AccountsSqlData account) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_PROVIDER, provider);
        args.putParcelable(KEY_ACCOUNT, account);
        WebDavSignInFragment fragment = new WebDavSignInFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(KEY_PROVIDER)) {
            mProvider = (WebDavApi.Providers) getArguments().getSerializable(KEY_PROVIDER);
        }
        if (getArguments() != null && getArguments().containsKey(KEY_ACCOUNT)) {
            mAccount = getArguments().getParcelable(KEY_ACCOUNT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_PORTAL, mStorageWebDavUrlEdit.getText().toString());
        outState.putString(KEY_LOGIN, mStorageWebDavLoginEdit.getText().toString());
        outState.putString(KEY_PASSWORD, mStorageWebDavPasswordEdit.getText().toString());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_storage_web_dav, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(savedInstanceState);
    }

    @OnClick(R.id.storage_web_dav_save_button)
    public void onSaveClick() {
        final String url = mStorageWebDavUrlEdit.getText().toString().trim();
        final String login = mStorageWebDavLoginEdit.getText().toString().trim();
        final String password = mStorageWebDavPasswordEdit.getText().toString().trim();
        connect(url, login, password);
    }

    private void connect(String url, String login, String password) {
        hideKeyboard();
        if (mProvider == WebDavApi.Providers.NextCloud) {
            mPresenter.checkNextCloud(mProvider, url, false);
        } else {
            mPresenter.checkPortal(mProvider, url, login, password);
        }
    }

    @Override
    public void onLogin() {
        if (getActivity() != null) {
            MainActivity.show(getContext());
            getActivity().finish();
        }
    }

    @Override
    public void onNextCloudLogin(String url) {
        onDialogClose();
        NextCloudLoginActivity.show(requireActivity(), url);
    }

    @Override
    public void onUrlError(String string) {
        onDialogClose();
        mStorageWebDavUrlLayout.setError(string);
    }

    private void initViews(Bundle bundle) {
        mFieldsWatcher = new FieldsWatcher();
        mStorageWebDavUrlEdit.addTextChangedListener(mFieldsWatcher);
        mStorageWebDavLoginEdit.addTextChangedListener(mFieldsWatcher);
        mStorageWebDavPasswordEdit.addTextChangedListener(mFieldsWatcher);

        mStorageWebDavTitleLayout.setVisibility(View.GONE);
        mStorageWebDavSaveButton.setEnabled(false);
        restoreState(bundle);

        if (mProvider == WebDavApi.Providers.Yandex) {
            mStorageWebDavUrlEdit.setText("webdav.yandex.ru/");
            mStorageWebDavUrlLayout.setVisibility(View.GONE);
        }

        if (mProvider == WebDavApi.Providers.NextCloud) {
            mStorageWebDavPasswordLayout.setVisibility(View.GONE);
            mStorageWebDavLoginLayout.setVisibility(View.GONE);
            mStorageWebDavLoginEdit.removeTextChangedListener(mFieldsWatcher);
            mStorageWebDavPasswordEdit.removeTextChangedListener(mFieldsWatcher);
        }

        if (mAccount != null) {
            if (mProvider == WebDavApi.Providers.NextCloud || mProvider == WebDavApi.Providers.OwnCloud) {
                String url = mAccount.getScheme() + mAccount.getPortal();
                mStorageWebDavUrlEdit.setText(url);
            } else if (mProvider == WebDavApi.Providers.WebDav) {
                String url = mAccount.getScheme() + mAccount.getPortal() + mAccount.getWebDavPath();
                mStorageWebDavUrlEdit.setText(url);
            }
            mStorageWebDavLoginEdit.setText(mAccount.getLogin());
        }
        mStorageWebDavUrlEdit.postDelayed(() -> showKeyboard(mStorageWebDavUrlEdit), 500);
    }

    private void restoreState(Bundle bundle) {
        if (bundle != null) {
            mStorageWebDavUrlEdit.setText(bundle.getString(KEY_PORTAL));
            mStorageWebDavLoginEdit.setText(bundle.getString(KEY_LOGIN));
            mStorageWebDavPasswordEdit.setText(bundle.getString(KEY_PASSWORD));
        }
    }

    @Override
    public void onError(@Nullable String message) {
        onDialogClose();
        if (message != null) {
            showSnackBar(message);
        }
    }

    @Override
    public void onDialogWaiting(String title) {
        showWaitingDialog(title);
    }

    @Override
    public void onDialogClose() {
        hideDialog();
    }

    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            mStorageWebDavUrlLayout.setError("");

            final String url = mStorageWebDavUrlEdit.getText().toString();
            final String login = mStorageWebDavLoginEdit.getText().toString();
            final String password = mStorageWebDavPasswordEdit.getText().toString();

            if (mProvider == WebDavApi.Providers.NextCloud) {
                if (!"".equals(url)) {
                    mStorageWebDavSaveButton.setEnabled(true);
                } else {
                    mStorageWebDavSaveButton.setEnabled(false);
                }
            } else {
                if (!"".equals(url) && !"".equals(login) && !"".equals(password)) {
                    mStorageWebDavSaveButton.setEnabled(true);
                } else {
                    mStorageWebDavSaveButton.setEnabled(false);
                }
            }
        }
    }

}
