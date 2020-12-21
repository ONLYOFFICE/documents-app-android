package app.editors.manager.ui.fragments.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import moxy.presenter.InjectPresenter;
import com.google.android.material.textfield.TextInputLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.presenters.login.PersonalSignUpPresenter;
import app.editors.manager.mvp.views.login.PersonalRegisterView;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

public class PersonalSignUpFragment extends BaseAppFragment implements PersonalRegisterView {

    public static final String TAG = PersonalSignUpFragment.class.getSimpleName();

    private static final String TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING";
    private static final String TAG_DIALOG_INFO = "TAG_DIALOG_INFO";

    protected Unbinder mUnbinder;
    @BindView(R.id.login_personal_portal_email_edit)
    protected AppCompatEditText mLoginPersonalPortalEmailEdit;
    @BindView(R.id.login_personal_portal_email_layout)
    protected TextInputLayout mLoginPersonalPortalEmailLayout;
    @BindView(R.id.login_personal_signup_button)
    protected AppCompatButton mLoginPersonalSignupButton;

    @InjectPresenter
    PersonalSignUpPresenter mPersonalSignUpPresenter;

    private FieldsWatcher mFieldsWatcher;

    public static PersonalSignUpFragment newInstance() {
        return new PersonalSignUpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_personal_signup, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_INFO:
                    getActivity().finish();
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_WAITING:
                    mPersonalSignUpPresenter.cancelRequest();
                    break;
                case TAG_DIALOG_INFO:
                    hideDialog();
                    break;
            }
        }
    }

    @OnClick(R.id.login_personal_signup_button)
    public void onSignUpClick() {
        hideKeyboard(mLoginPersonalPortalEmailEdit);
        final String email = mLoginPersonalPortalEmailEdit.getText().toString();
        if (StringUtils.isEmailValid(email)) {
            showWaitingDialog(getString(R.string.dialogs_sign_in_register_portal), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING);
            mPersonalSignUpPresenter.registerPortal(email);
        } else {
            setMessage(R.string.errors_email_syntax_error, true);
        }
    }

    @OnEditorAction(R.id.login_personal_portal_email_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignUpClick();
            return true;
        }

        return false;
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        setMessage(message, true);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onRegisterPortal() {
        showQuestionDialog(getString(R.string.dialogs_question_personal_confirm_link_title),
                getString(R.string.login_personal_signup_dialog_info, mLoginPersonalPortalEmailEdit.getText().toString()),
                getString(R.string.dialogs_question_personal_confirm_accept), getString(R.string.dialogs_question_personal_confirm_cancel),
                TAG_DIALOG_INFO);
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.login_personal_signup_title));
        showKeyboard(mLoginPersonalPortalEmailEdit);
        mFieldsWatcher = new FieldsWatcher();
        mLoginPersonalPortalEmailEdit.addTextChangedListener(mFieldsWatcher);
        mLoginPersonalSignupButton.setEnabled(false);
        setMessage(R.string.login_personal_signup_edit_info, false);
    }

    private void setMessage(final String message, final boolean isError) {
        mLoginPersonalPortalEmailLayout.setErrorTextAppearance(isError ? R.style.TextInputErrorRed : R.style.TextInputErrorGrey);
        mLoginPersonalPortalEmailLayout.setError(message);
    }

    private void setMessage(@StringRes final int resId, final boolean isError) {
        setMessage(getString(resId), isError);
    }

    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            mLoginPersonalPortalEmailLayout.setErrorTextAppearance(R.style.TextInputErrorGrey);
            mLoginPersonalPortalEmailLayout.setError(getString(R.string.login_personal_signup_edit_info));
            final String email = mLoginPersonalPortalEmailEdit.getText().toString();
            if (!"".equals(email)) {
                mLoginPersonalSignupButton.setEnabled(true);
            } else {
                mLoginPersonalSignupButton.setEnabled(false);
            }
        }
    }

}
