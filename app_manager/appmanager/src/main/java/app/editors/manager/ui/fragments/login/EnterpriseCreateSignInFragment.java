package app.editors.manager.ui.fragments.login;


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

import moxy.presenter.InjectPresenter;
import com.google.android.material.textfield.TextInputLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.presenters.login.EnterpriseCreateLoginPresenter;
import app.editors.manager.mvp.views.login.EnterpriseCreateSignInView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

public class EnterpriseCreateSignInFragment extends BaseAppFragment implements EnterpriseCreateSignInView {

    public static final String TAG = EnterpriseCreateSignInFragment.class.getSimpleName();

    @BindView(R.id.login_signin_password_edit)
    protected AppCompatEditText mLoginSigninPasswordEdit;
    @BindView(R.id.login_signin_password_layout)
    protected TextInputLayout mLoginSigninPasswordLayout;
    @BindView(R.id.login_signin_repeat_edit)
    protected AppCompatEditText mLoginSigninRepeatHint;
    @BindView(R.id.login_signin_repeat_layout)
    protected TextInputLayout mLoginSigninRepeatLayout;
    @BindView(R.id.login_signin_create_portal_button)
    protected AppCompatButton mLoginCreateButton;
    @BindView(R.id.login_signin_terms_info_button)
    protected AppCompatButton mLoginSigninInfo;

    @InjectPresenter
    EnterpriseCreateLoginPresenter mSignInPortalPresenter;

    private Unbinder mUnbinder;
    private FieldsWatcher mFieldsWatcher;

    public static EnterpriseCreateSignInFragment newInstance() {
        final EnterpriseCreateSignInFragment fragment = new EnterpriseCreateSignInFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_enterprise_create_signin, container, false);
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

    @OnClick(R.id.login_signin_create_portal_button)
    protected void onSignInClick() {
        hideKeyboard(mLoginSigninPasswordEdit);
        final String password = mLoginSigninPasswordEdit.getText().toString();
        final String repeat = mLoginSigninRepeatHint.getText().toString();
        if (!password.equals(repeat)) {
            mLoginSigninRepeatLayout.setError(getString(R.string.login_create_signin_passwords_mismatch));
            return;
        }

        showWaitingDialog(getString(R.string.dialogs_wait_title));
        mSignInPortalPresenter.createPortal(password);
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable final String tag) {
        super.onCancelClick(dialogs, tag);
        mSignInPortalPresenter.cancelRequest();
    }

    @OnClick(R.id.login_signin_terms_info_button)
    protected void onAgreeTerms() {
        showUrlInBrowser(getString(R.string.app_url_terms));
    }

    @OnEditorAction(R.id.login_signin_repeat_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignInClick();
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
    public void onSuccessLogin() {
        hideDialog();
        MainActivity.show(getContext());
        getActivity().finish();
    }

    @Override
    public void onTwoFactorAuth(boolean isPhone) {
        hideDialog();
        if (isPhone) {
            showFragment(EnterprisePhoneFragment.newInstance(), EnterprisePhoneFragment.TAG, false);
        } else {
            showFragment(EnterpriseSmsFragment.newInstance(false, null), EnterpriseSmsFragment.TAG, false);
        }
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.login_create_signin_title));
        showKeyboard(mLoginSigninPasswordEdit);
        mFieldsWatcher = new FieldsWatcher();
        mLoginCreateButton.setEnabled(false);
        mLoginSigninPasswordEdit.requestFocus();
        mLoginSigninPasswordEdit.addTextChangedListener(mFieldsWatcher);
        mLoginSigninRepeatHint.addTextChangedListener(mFieldsWatcher);
    }

    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            mLoginSigninRepeatLayout.setError(null);
            final String password = mLoginSigninPasswordEdit.getText().toString();
            final String repeat = mLoginSigninRepeatHint.getText().toString();
            if (!"".equals(password) && !"".equals(repeat)) {
                mLoginCreateButton.setEnabled(true);
            } else {
                mLoginCreateButton.setEnabled(false);
            }
        }
    }

}
