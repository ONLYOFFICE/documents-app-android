package app.editors.manager.ui.fragments.login;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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
import androidx.appcompat.widget.AppCompatTextView;

import moxy.presenter.InjectPresenter;
import com.google.android.material.textfield.TextInputLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.presenters.login.EnterpriseCreateValidatePresenter;
import app.editors.manager.mvp.views.login.EnterpriseCreateValidateView;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseInputFilter;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

public class EnterpriseCreatePortalFragment extends BaseAppFragment implements EnterpriseCreateValidateView {

    public static final String TAG = EnterpriseCreatePortalFragment.class.getSimpleName();

    @BindView(R.id.login_create_portal_address_edit)
    protected AppCompatEditText mLoginCreatePortalAddressEdit;
    @BindView(R.id.login_create_portal_address_hint_end)
    protected AppCompatTextView mLoginCreatePortalAddressHintEnd;
    @BindView(R.id.login_create_portal_address_edit_layout)
    protected TextInputLayout mLoginCreatePortalAddressLayout;
    @BindView(R.id.login_create_portal_email_edit)
    protected AppCompatEditText mLoginCreatePortalEmailEdit;
    @BindView(R.id.login_create_portal_email_layout)
    protected TextInputLayout mLoginCreatePortalEmailLayout;
    @BindView(R.id.login_create_portal_first_name_edit)
    protected AppCompatEditText mLoginCreatePortalFirstNameEdit;
    @BindView(R.id.login_create_portal_first_name_layout)
    protected TextInputLayout mLoginCreatePortalFirstNameLayout;
    @BindView(R.id.login_create_portal_last_name_edit)
    protected AppCompatEditText mLoginCreatePortalLastNameEdit;
    @BindView(R.id.login_create_portal_last_name_layout)
    protected TextInputLayout mLoginCreatePortalLastNameLayout;
    @BindView(R.id.login_signin_create_portal_button)
    protected AppCompatButton mLoginNextButton;

    @InjectPresenter
    EnterpriseCreateValidatePresenter mCreatePortalPresenter;

    private Unbinder mUnbinder;
    private FieldsWatcher mFieldsWatcher;
    private int mPaddingTop;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingBottom;

    public static EnterpriseCreatePortalFragment newInstance() {
        return new EnterpriseCreatePortalFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_enterprise_create_portal, container, false);
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
    protected void onNextClick() {
        hideKeyboard(mLoginCreatePortalAddressEdit);
        final String address = mLoginCreatePortalAddressEdit.getText().toString();
        final String email = mLoginCreatePortalEmailEdit.getText().toString();
        final String first = mLoginCreatePortalFirstNameEdit.getText().toString();
        final String last = mLoginCreatePortalLastNameEdit.getText().toString();
        mCreatePortalPresenter.validatePortal(address, email, first, last);
    }

    @OnEditorAction(R.id.login_create_portal_last_name_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onNextClick();
            return true;
        }

        return false;
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable final String tag) {
        super.onCancelClick(dialogs, tag);
        mCreatePortalPresenter.cancelRequest();
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        showSnackBar(message);
    }

    @Override
    public void onValidatePortalSuccess() {
        hideDialog();
        showFragment(EnterpriseCreateSignInFragment.newInstance(),
                EnterpriseCreateSignInFragment.TAG, false);
    }

    @Override
    public void onPortalNameError(String message) {
        setEditHintVisibility(false);
        mLoginCreatePortalAddressLayout.setError(message);
    }

    @Override
    public void onEmailNameError(String message) {
        mLoginCreatePortalEmailLayout.setError(message);
    }

    @Override
    public void onFirstNameError(String message) {
        mLoginCreatePortalFirstNameLayout.setError(message);
    }

    @Override
    public void onLastNameError(String message) {
        mLoginCreatePortalLastNameLayout.setError(message);
    }

    @Override
    public void onRegionDomain(String domain) {
        final int textWidth = UiUtils.measureTextSizes(domain + "X", (int) mLoginCreatePortalAddressHintEnd.getTextSize()).x;
        mLoginCreatePortalAddressHintEnd.getLayoutParams().width = textWidth;
        mLoginCreatePortalAddressHintEnd.setText(domain);
        mPaddingRight = textWidth;
    }

    @Override
    public void onShowWaitingDialog(@StringRes int title) {
        showWaitingDialog(getString(title));
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.login_create_portal_title));
        showKeyboard(mLoginCreatePortalAddressEdit);
        mFieldsWatcher = new FieldsWatcher();
        mLoginNextButton.setEnabled(false);
        mLoginCreatePortalAddressEdit.requestFocus();
        mLoginCreatePortalAddressEdit.addTextChangedListener(mFieldsWatcher);
        mLoginCreatePortalAddressEdit.setFilters(new InputFilter[]{new FieldsFilter()});
        mLoginCreatePortalEmailEdit.addTextChangedListener(mFieldsWatcher);
        mLoginCreatePortalFirstNameEdit.addTextChangedListener(mFieldsWatcher);
        mLoginCreatePortalLastNameEdit.addTextChangedListener(mFieldsWatcher);
        mCreatePortalPresenter.getDomain();
        setPadding();
    }

    private void setPadding() {
        mPaddingTop = mLoginCreatePortalAddressEdit.getPaddingTop();
        mPaddingLeft = mLoginCreatePortalAddressEdit.getPaddingLeft();
        mPaddingRight = mLoginCreatePortalAddressEdit.getPaddingRight();
        mPaddingBottom = mLoginCreatePortalAddressEdit.getPaddingBottom();
    }

    private void setEditHintVisibility(final boolean isVisible) {
        if (isVisible) {
            mLoginCreatePortalAddressHintEnd.setVisibility(View.VISIBLE);
            mLoginCreatePortalAddressEdit.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
        } else {
            mLoginCreatePortalAddressHintEnd.setVisibility(View.GONE);
            mLoginCreatePortalAddressEdit.setPadding(mPaddingLeft, mPaddingTop, 0, mPaddingBottom);
        }
    }

    /*
     * Filtering inputs
     * */
    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            mLoginCreatePortalEmailLayout.setErrorEnabled(false);
            mLoginCreatePortalFirstNameLayout.setErrorEnabled(false);
            mLoginCreatePortalLastNameLayout.setErrorEnabled(false);
            final String address = mLoginCreatePortalAddressEdit.getText().toString();
            final String email = mLoginCreatePortalEmailEdit.getText().toString();
            final String first = mLoginCreatePortalFirstNameEdit.getText().toString();
            final String last = mLoginCreatePortalLastNameEdit.getText().toString();

            if (StringUtils.isAlphaNumeric(address)) {
                mLoginCreatePortalAddressLayout.setErrorEnabled(false);
            }

            if (!"".equals(first) & StringUtils.isCreateUserName(first)) {
                mLoginCreatePortalFirstNameLayout.setError(getString(R.string.errors_first_name));
                mLoginNextButton.setEnabled(false);
            } else if (!"".equals(last) & StringUtils.isCreateUserName(last)) {
                mLoginCreatePortalLastNameLayout.setError(getString(R.string.errors_last_name));
                mLoginNextButton.setEnabled(false);
            } else if (!"".equals(address) && !"".equals(email) && !"".equals(first) && !"".equals(last) && StringUtils.isAlphaNumeric(address)) {
                mLoginNextButton.setEnabled(true);
            } else {
                mLoginNextButton.setEnabled(false);
            }

        }
    }

    private class FieldsFilter extends BaseInputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            super.filter(source, start, end, dest, dstart, dend);
            if (mCreatePortalPresenter.checkPhrase(mResultString)) {
                return null;
            }

            if (!StringUtils.isAlphaNumeric(mResultString)) {
                mLoginCreatePortalAddressLayout.setError(getString(R.string.login_api_portal_name_content));
                return source;
            } else {
                mLoginCreatePortalAddressLayout.setErrorEnabled(false);
                setEditHintVisibility(true);
                return null;
            }
        }
    }

}
