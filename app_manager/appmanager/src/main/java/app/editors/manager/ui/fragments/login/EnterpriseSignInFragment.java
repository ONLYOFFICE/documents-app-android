package app.editors.manager.ui.fragments.login;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.login.EnterpriseLoginPresenter;
import app.editors.manager.mvp.views.login.CommonSignInView;
import app.editors.manager.ui.activities.login.AuthAppActivity;
import app.editors.manager.ui.activities.login.SignInActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.custom.SocialViews;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTouch;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

import static app.editors.manager.mvp.presenters.login.EnterpriseLoginPresenter.TAG_DIALOG_FORGOT_PASSWORD;
import static app.editors.manager.mvp.presenters.login.EnterpriseLoginPresenter.TAG_DIALOG_LOGIN_FACEBOOK;
import static app.editors.manager.mvp.presenters.login.EnterpriseLoginPresenter.TAG_DIALOG_WAITING;
import static app.editors.manager.ui.fragments.login.AuthPagerFragment.KEY_FOURTH_FRAGMENT;


public class EnterpriseSignInFragment extends BaseAppFragment implements CommonSignInView,
        CommonDialog.OnClickListener, SocialViews.OnSocialNetworkCallbacks {

    public static final String TAG = EnterpriseSignInFragment.class.getSimpleName();

    private static final String TAG_FOCUS_EMAIL = "TAG_FOCUS_EMAIL";
    private static final String TAG_FOCUS_PWD = "TAG_FOCUS_PWD";

    protected Unbinder mUnbinder;
    @BindView(R.id.login_enterprise_portal_email_edit)
    protected AppCompatEditText mLoginPersonalPortalEmailEdit;
    @BindView(R.id.login_enterprise_portal_email_layout)
    protected TextInputLayout mLoginPersonalPortalEmailLayout;
    @BindView(R.id.login_enterprise_portal_password_edit)
    protected AppCompatEditText mLoginPersonalPortalPasswordEdit;
    @BindView(R.id.login_enterprise_portal_password_layout)
    protected TextInputLayout mLoginPersonalPortalPasswordLayout;
    @BindView(R.id.login_enterprise_signin_button)
    protected AppCompatButton mLoginPersonalSigninButton;
    @BindView(R.id.login_enterprise_signon_button)
    protected AppCompatButton mLoginPersonalSignonButton;
    @BindView(R.id.login_enterprise_forgot_pwd_button)
    protected AppCompatButton mLoginPersonalForgotPwdButton;
    @BindView(R.id.social_network_layout)
    protected ConstraintLayout mSocialNetworkLayout;

    @Inject
    protected PreferenceTool mPreferenceTool;

    @Inject
    protected AccountSqlTool mAccountSqlTool;

    @InjectPresenter
    EnterpriseLoginPresenter mEnterpriseSignInPresenter;

    private SignInActivity mSignInActivity;
    private SocialViews mSocialViews;
    private FieldsWatcher mFieldsWatcher;

    public static EnterpriseSignInFragment newInstance(String portal, String login) {
        Bundle args = new Bundle();
        args.putString(SignInActivity.KEY_PORTAL, portal);
        args.putString(SignInActivity.KEY_LOGIN, login);
        EnterpriseSignInFragment fragment = new EnterpriseSignInFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String getPortal() {
        if (getArguments() != null && getArguments().containsKey(SignInActivity.KEY_PORTAL)) {
            return getArguments().getString(SignInActivity.KEY_PORTAL);
        } else {
            return "";
        }
    }

    private String getLogin() {
        if (getArguments() != null && getArguments().containsKey(SignInActivity.KEY_LOGIN)) {
            return getArguments().getString(SignInActivity.KEY_LOGIN);
        } else {
            return "";
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
        try {
            mSignInActivity = (SignInActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(EnterpriseSignInFragment.class.getSimpleName() + " - must implement - " +
                    EnterpriseSignInFragment.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_enterprise_signin, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSignInActivity.setOnActivityResult(this);
        init(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLoginPersonalPortalEmailEdit != null) {
            outState.putBoolean(TAG_FOCUS_EMAIL, mLoginPersonalPortalEmailEdit.hasFocus());
        }

        if (mLoginPersonalPortalPasswordEdit != null) {
            outState.putBoolean(TAG_FOCUS_PWD, mLoginPersonalPortalPasswordEdit.hasFocus());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSocialViews.onDestroyView();
        mSignInActivity.setOnActivityResult(null);
        mUnbinder.unbind();
        mEnterpriseSignInPresenter.cancelRequest();
    }

    @Override
    public boolean onBackPressed() {
        hideKeyboard(mLoginPersonalPortalEmailEdit);
        hideKeyboard(mLoginPersonalPortalPasswordEdit);
        return super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SocialViews.GOOGLE_PERMISSION) {
            mEnterpriseSignInPresenter.retrySignInWithGoogle(getPortal());
        } else {
            mSocialViews.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_LOGIN_FACEBOOK:
                    mSocialViews.onFacebookContinue();
                    break;
                case TAG_DIALOG_FORGOT_PASSWORD:
                    mEnterpriseSignInPresenter.checkEmail(value.trim());
                    break;
            }
        }
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_WAITING:
                    mEnterpriseSignInPresenter.cancelRequest();
                    break;
                case TAG_DIALOG_LOGIN_FACEBOOK:
                    mSocialViews.onFacebookLogout();
                    break;
            }
        }
    }

    @OnClick(R.id.login_enterprise_signin_button)
    protected void signInButtonClick() {
        final String email = mLoginPersonalPortalEmailEdit.getText().toString();
        final String password = mLoginPersonalPortalPasswordEdit.getText().toString();
        mEnterpriseSignInPresenter.signInPortal(email.trim(), password, getPortal());
    }

    @OnClick(R.id.login_enterprise_signon_button)
    protected void onSignOnButtonClick() {
        //showFragment(EnterpriseSmsFragment.newInstance(false, null), EnterpriseSmsFragment.TAG, false);
        showFragment(SSOLoginFragment.newInstance(mPreferenceTool.getSsoUrl(), mPreferenceTool.getPortal()), SSOLoginFragment.TAG, true);
    }

    @OnClick(R.id.login_enterprise_forgot_pwd_button)
    protected void onForgotPwdClick() {
        //showUrlInBrowser(mPreferenceTool.getScheme() + mPreferenceTool.getPortal());
        showEditDialogCreate(getString(R.string.login_enterprise_password_reminder),
                "",
                getString(R.string.login_enterprise_email_hint),
                "",
                TAG_DIALOG_FORGOT_PASSWORD,
                getString(R.string.dialog_send_password_reminder), getString(R.string.dialogs_common_cancel_button));
    }

    @OnEditorAction(R.id.login_enterprise_portal_password_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            signInButtonClick();
            return true;
        }

        return false;
    }

    @OnTouch({R.id.login_enterprise_portal_email_edit, R.id.login_enterprise_portal_password_edit})
    protected boolean onEmailTouchListener() {
        mLoginPersonalPortalEmailEdit.setFocusableInTouchMode(true);
        mLoginPersonalPortalPasswordEdit.setFocusableInTouchMode(true);
        return false;
    }

    @Override
    public void onSuccessLogin() {
        hideDialog();
        MainActivity.show(getContext());
        getActivity().finish();
    }

    @Override
    public void onTwoFactorAuth(final boolean isPhone, AccountsSqlData sqlData) {
        hideDialog();
        if (isPhone) {
            showFragment(EnterprisePhoneFragment.newInstance(), EnterprisePhoneFragment.TAG, false);
        } else {
            showFragment(EnterpriseSmsFragment.newInstance(false, sqlData), EnterpriseSmsFragment.TAG, false);
        }
    }

    @Override
    public void onTwoFactorAuthTfa(boolean isSecret, AccountsSqlData sqlData) {
        hideDialog();
        if (isSecret) {
            AuthAppActivity.show(getActivity(), sqlData);
        } else {
            showFragment(AuthPageFragment.newInstance(KEY_FOURTH_FRAGMENT, sqlData), AuthPageFragment.TAG, false);
        }
    }

    @Override
    public void onGooglePermission(Intent intent) {
        getActivity().startActivityForResult(intent, SocialViews.GOOGLE_PERMISSION);
    }

    @Override
    public void onEmailNameError(String message) {
        hideDialog();
        mLoginPersonalPortalEmailLayout.setError(message);
    }

    @Override
    public void onWaitingDialog(String message, String tag) {
        showWaitingDialog(message, getString(R.string.dialogs_common_cancel_button), tag);
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        showSnackBar(message);
    }

    @Override
    public void onTwitterSuccess(String token) {
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING);
        mEnterpriseSignInPresenter.signInWithTwitter(token, getPortal());
    }

    @Override
    public void onTwitterFailed() {
        hideDialog();
        showSnackBar(R.string.socials_twitter_failed_auth);
    }

    @Override
    public void onFacebookSuccess(String token) {
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING);
        mEnterpriseSignInPresenter.signInWithFacebook(token, getPortal());
    }

    @Override
    public void onFacebookLogin(String message) {
        showQuestionDialog(getString(R.string.dialogs_question_facebook_title),
                getString(R.string.dialogs_question_facebook_question) + message,
                getString(R.string.dialogs_question_accept_yes), getString(R.string.dialogs_question_accept_no),
                TAG_DIALOG_LOGIN_FACEBOOK);
    }

    @Override
    public void onFacebookCancel() {
        hideDialog();
        showSnackBar(R.string.socials_facebook_cancel_auth);
    }

    @Override
    public void onFacebookFailed() {
        hideDialog();
        showSnackBar(R.string.socials_facebook_failed_auth);
    }

    @Override
    public void onGoogleSuccess(Account account) {
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING);
        mEnterpriseSignInPresenter.signInWithGoogle(account, getPortal());
    }

    @Override
    public void onGoogleFailed() {
        hideDialog();
        showSnackBar(R.string.socials_google_failed_auth);
    }

    private void init(final Bundle savedInstanceState) {
        initViews();
        getArgs();
        getIntent();
        restoreViews(savedInstanceState);
        mEnterpriseSignInPresenter.checkProviders();
    }

    private void getArgs() {
        final String portal = getPortal();
        if (portal != null && !portal.isEmpty()) {
            setActionBarTitle(getPortal());
        }

        final String login = getLogin();
        if (login != null && !login.isEmpty()) {
            mLoginPersonalPortalEmailEdit.setText(getLogin());
        }
    }

    private void initViews() {
        final String facebookId = mPreferenceTool.isPortalInfo()
                ? getString(R.string.facebook_app_id_info)
                : getString(R.string.facebook_app_id);
        mSocialViews = new SocialViews(getActivity(), mSocialNetworkLayout, facebookId);
        mSocialViews.setOnSocialNetworkCallbacks(this);
        mFieldsWatcher = new FieldsWatcher();
        mLoginPersonalPortalEmailEdit.addTextChangedListener(mFieldsWatcher);
        mLoginPersonalPortalPasswordEdit.addTextChangedListener(mFieldsWatcher);
        mLoginPersonalSigninButton.setEnabled(false);
        mLoginPersonalSignonButton.setEnabled(false);
    }

    private void getIntent() {
        final Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(SignInActivity.TAG_PORTAL_SIGN_IN_EMAIL) && mPreferenceTool.getLogin() != null) {
                mLoginPersonalPortalEmailEdit.setText(mPreferenceTool.getLogin());
            }
        }
    }

    private void restoreViews(final Bundle savedInstanceState) {
        final String ssoUrl = mPreferenceTool.getSsoUrl();
        if (ssoUrl != null && !ssoUrl.isEmpty()) {
            mLoginPersonalSignonButton.setVisibility(View.VISIBLE);
            setSSOButtonText();
            mLoginPersonalSignonButton.setEnabled(true);
        }

//        final String login = mPreferenceTool.getLogin();
//        if (login != null) {
//            mLoginPersonalPortalEmailEdit.setText(login);
//        }
//
////        final String password = mPreferenceTool.getPassword();
////        if (password != null) {
////            mLoginPersonalPortalPasswordEdit.setText(password);
////        }

        if (savedInstanceState == null) {
            mLoginPersonalPortalEmailEdit.setFocusable(false);
            mLoginPersonalPortalPasswordEdit.setFocusable(false);
        } else {
            if (savedInstanceState.getBoolean(TAG_FOCUS_EMAIL)) {
                showKeyboard(mLoginPersonalPortalEmailEdit);
            } else if (savedInstanceState.getBoolean(TAG_FOCUS_PWD)) {
                showKeyboard(mLoginPersonalPortalPasswordEdit);
            }
        }
    }

    private void setSSOButtonText() {
        String ssoLabel = mPreferenceTool.getSsoLabel();
        if (!ssoLabel.isEmpty()) {
            mLoginPersonalSignonButton.setText(getString(R.string.login_enterprise_single_sign_button_login, ssoLabel));
        } else {
            mLoginPersonalSignonButton.setText(getString(R.string.login_enterprise_single_sign_button_login, getString(R.string.login_enterprise_single_sign_button_login_default)));
        }
    }

    @Override
    public void showGoogleLogin(boolean isShow) {
        mSocialViews.showGoogleLogin(isShow);
    }

    @Override
    public void showFacebookLogin(boolean isShow) {
        mSocialViews.showFacebookLogin(isShow);
    }

    @Override
    public void onSuccessSendEmail(String message) {
        hideDialog();
        showSnackBar(message);
    }

    /*
     * Text input watcher
     * */
    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            mLoginPersonalPortalEmailLayout.setErrorEnabled(false);
            final String email = mLoginPersonalPortalEmailEdit.getText().toString();
            final String password = mLoginPersonalPortalPasswordEdit.getText().toString();
            if (!"".equals(email) && !"".equals(password)) {
                mLoginPersonalSigninButton.setEnabled(true);
            } else {
                mLoginPersonalSigninButton.setEnabled(false);
            }
        }
    }

}
