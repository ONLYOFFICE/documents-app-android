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

import app.editors.manager.BuildConfig;
import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.login.PersonalLoginPresenter;
import app.editors.manager.mvp.views.login.CommonSignInView;
import app.editors.manager.ui.activities.login.AuthAppActivity;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.activities.login.SignInActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.custom.SocialViews;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

import static app.editors.manager.ui.fragments.login.AuthPagerFragment.KEY_FOURTH_FRAGMENT;

public class PersonalPortalFragment extends BaseAppFragment implements CommonSignInView,
        SocialViews.OnSocialNetworkCallbacks {

    public static final String TAG = PersonalPortalFragment.class.getSimpleName();
    private static final String TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING";
    private static final String TAG_DIALOG_LOGIN_FACEBOOK = "TAG_DIALOG_LOGIN_FACEBOOK";

    private static final String KEY_EMAIL = "KEY_EMAIL";
    private static final String KEY_PASSWORD = "KEY_PASSWORD";

    protected Unbinder mUnbinder;
    @BindView(R.id.login_personal_portal_email_edit)
    protected AppCompatEditText mLoginPersonalPortalEmailEdit;
    @BindView(R.id.login_personal_portal_email_layout)
    protected TextInputLayout mLoginPersonalPortalEmailLayout;
    @BindView(R.id.login_personal_portal_password_edit)
    protected AppCompatEditText mLoginPersonalPortalPasswordEdit;
    @BindView(R.id.login_personal_portal_password_layout)
    protected TextInputLayout mLoginPersonalPortalPasswordLayout;
    @BindView(R.id.login_personal_signin_button)
    protected AppCompatButton mLoginPersonalSigninButton;
    @BindView(R.id.login_personal_info_text)
    protected TextView mLoginPersonalInfoText;
    @BindView(R.id.login_personal_signup_button)
    protected AppCompatButton mLoginPersonalSignupButton;
    @BindView(R.id.social_network_layout)
    protected ConstraintLayout mSocialNetworkLayout;

    @Inject
    protected PreferenceTool mPreferenceTool;

    @InjectPresenter
    PersonalLoginPresenter mPersonalSignInPresenter;

    private PortalsActivity mPortalsActivity;
    private FieldsWatcher mFieldsWatcher;
    private SocialViews mSocialViews;

    public static PersonalPortalFragment newInstance() {
        return new PersonalPortalFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
        try {
            mPortalsActivity = (PortalsActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(PersonalPortalFragment.class.getSimpleName() + " - must implement - " +
                    PortalsActivity.class.getSimpleName());
        }
    }

    @Override
    public boolean onBackPressed() {
        hideKeyboard();
        return super.onBackPressed();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_personal_portal, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        restoreValues(savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPortalsActivity.setOnActivityResult(this);
        init(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSocialViews.onDestroyView();
        mPortalsActivity.setOnActivityResult(null);
        mUnbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_EMAIL, mLoginPersonalPortalEmailEdit.getText().toString());
        outState.putString(KEY_PASSWORD, mLoginPersonalPortalPasswordEdit.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SocialViews.GOOGLE_PERMISSION) {
            mPersonalSignInPresenter.retrySignInWithGoogle(Api.PERSONAL_HOST);
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
            }
        }
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_WAITING:
                    mPersonalSignInPresenter.cancelRequest();
                    break;
                case TAG_DIALOG_LOGIN_FACEBOOK:
                    mSocialViews.onFacebookLogout();
                    break;
            }
        }
    }

    @OnClick(R.id.login_personal_signin_button)
    protected void onSignInClick() {
        hideKeyboard(mLoginPersonalPortalEmailEdit);
        final String email = mLoginPersonalPortalEmailEdit.getText().toString();
        final String password = mLoginPersonalPortalPasswordEdit.getText().toString();
        mPersonalSignInPresenter.signInPersonal(email, password);
    }

    @OnClick(R.id.login_personal_signup_button)
    protected void signUpClick() {
        SignInActivity.showPersonalSignUp(getContext());
    }

    @OnEditorAction(R.id.login_personal_portal_password_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignInClick();
            return true;
        }

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
            SignInActivity.showPhone(getContext());
        } else {
            SignInActivity.showSms(getContext());
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
        mPersonalSignInPresenter.signInPersonalWithTwitter(token);
    }

    @Override
    public void onTwitterFailed() {
        hideDialog();
        showSnackBar(R.string.socials_twitter_failed_auth);
    }

    @Override
    public void onFacebookSuccess(String token) {
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING);
        mPersonalSignInPresenter.signInPersonalWithFacebook(token);
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
        showSnackBar(R.string.socials_facebook_cancel_auth);
    }

    @Override
    public void onFacebookFailed() {
        showSnackBar(R.string.socials_facebook_failed_auth);
    }

    @Override
    public void onGoogleSuccess(Account account) {
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING);
        mPersonalSignInPresenter.signInPersonalWithGoogle(account);
    }

    @Override
    public void onGoogleFailed() {
        showSnackBar(R.string.socials_google_failed_auth);
    }

    private void init(final Bundle savedInstanceState) {
        final String facebookId = mPreferenceTool.isPortalInfo()
                ? getString(R.string.facebook_app_id_info)
                : getString(R.string.facebook_app_id);
        mSocialViews = new SocialViews(getActivity(), mSocialNetworkLayout, facebookId);
        mSocialViews.setOnSocialNetworkCallbacks(this);
        mFieldsWatcher = new FieldsWatcher();
        mLoginPersonalPortalEmailEdit.clearFocus();
        mLoginPersonalPortalEmailEdit.addTextChangedListener(mFieldsWatcher);
        mLoginPersonalPortalPasswordEdit.addTextChangedListener(mFieldsWatcher);
        mLoginPersonalSigninButton.setEnabled(false);
        restoreValues(savedInstanceState);
    }

    private void restoreValues(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLoginPersonalPortalEmailEdit.setText(savedInstanceState.getString(KEY_EMAIL));
            mLoginPersonalPortalPasswordEdit.setText(savedInstanceState.getString(KEY_PASSWORD));
        }
    }

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
