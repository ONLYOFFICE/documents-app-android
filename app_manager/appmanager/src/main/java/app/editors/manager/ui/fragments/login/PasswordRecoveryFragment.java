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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.presenters.login.PasswordRecoveryPresenter;
import app.editors.manager.mvp.views.login.PasswordRecoveryView;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import moxy.presenter.InjectPresenter;

public class PasswordRecoveryFragment extends BaseAppFragment implements PasswordRecoveryView {

    public static String TAG = PasswordRecoveryFragment.class.getSimpleName();

    public static final String KEY_EMAIL = "KEY_EMAIL";
    public static final String KEY_PERSONAL = "KEY_PERSONAL";

    public static PasswordRecoveryFragment newInstance(String email, Boolean isPersonal) {
        Bundle args = new Bundle();
        args.putString(KEY_EMAIL, email);
        args.putBoolean(KEY_PERSONAL, isPersonal);
        PasswordRecoveryFragment fragment = new PasswordRecoveryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @InjectPresenter
    public PasswordRecoveryPresenter mPresenter;

    protected Unbinder mUnbinder;

    private boolean mIsPasswordRecovered = false;

    @BindView(R.id.login_password_recovery_hint)
    protected TextView mLoginPasswordRecoveryHint;
    @BindView(R.id.login_password_recovery_button)
    protected AppCompatButton mRecoverButton;
    @BindView(R.id.login_password_recovery_email_layout)
    protected TextInputLayout mPasswordRecoveryEmailLayout;
    @BindView(R.id.login_password_recovery_email_edit)
    protected AppCompatEditText mPasswordRecoveryEmailEdit;
    @BindView(R.id.login_password_recovery_image)
    protected AppCompatImageView mPasswordRecoveryImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_password_recovery, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void init() {

        mPasswordRecoveryEmailEdit.setText(getArguments().getString(KEY_EMAIL));
        if(getArguments().getString(KEY_EMAIL).isEmpty()) {
            mRecoverButton.setEnabled(false);
        }
        mPasswordRecoveryEmailEdit.addTextChangedListener(new FieldsWatcher());
        setActionBarTitle(getContext().getString(R.string.login_password_recovery_toolbar_title));
    }

    @OnClick(R.id.login_password_recovery_button)
    protected void onRecoverButtonClick() {
        if(!mIsPasswordRecovered) {
            mPresenter.recoverPassword(mPasswordRecoveryEmailEdit.getText().toString().trim(), getArguments().getBoolean(KEY_PERSONAL));
        } else {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onPasswordRecoverySuccess(String email) {
        mPasswordRecoveryEmailLayout.setVisibility(View.INVISIBLE);
        mLoginPasswordRecoveryHint.setText(getContext().getString(R.string.login_password_recovery_success_hint, email));
        mIsPasswordRecovered = true;
        mRecoverButton.setVisibility(View.VISIBLE);
        mPasswordRecoveryImageView.setVisibility(View.VISIBLE);
        mRecoverButton.setText(getContext().getString(R.string.login_password_recovery_button_text));
    }

    @Override
    public void onEmailError() {
        hideDialog();
        mPasswordRecoveryEmailLayout.setError(getContext().getString(R.string.errors_email_syntax_error));
    }

    @Override
    public void onError(@Nullable String message) {
        showSnackBar(message).show();
    }

    @OnEditorAction(R.id.login_password_recovery_email_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onRecoverButtonClick();
            hideKeyboard();
            return true;
        }

        return false;
    }

    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            mPasswordRecoveryEmailLayout.setErrorEnabled(false);
            final String email = mPasswordRecoveryEmailEdit.getText().toString();
            if (!"".equals(email)) {
                mRecoverButton.setEnabled(true);
            } else {
                mRecoverButton.setEnabled(false);
            }
        }
    }

}
