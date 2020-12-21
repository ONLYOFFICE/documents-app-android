package app.editors.manager.ui.fragments.login;

import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
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

import java.util.Locale;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.CountriesCodesTool;
import app.editors.manager.mvp.presenters.login.EnterprisePhonePresenter;
import app.editors.manager.mvp.views.login.EnterprisePhoneView;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;


public class EnterprisePhoneFragment extends BaseAppFragment implements EnterprisePhoneView {

    public static final String TAG = EnterprisePhoneFragment.class.getSimpleName();

    private static final String TAG_CODE = "TAG_CODE";
    private static final String TAG_NAME = "TAG_NAME";
    private static final String TAG_REGION = "TAG_REGION";

    protected Unbinder mUnbinder;
    @BindView(R.id.login_phone_country_edit)
    protected AppCompatEditText mLoginPhoneCountryEdit;
    @BindView(R.id.login_phone_country_layout)
    protected TextInputLayout mLoginPhoneCountryLayout;
    @BindView(R.id.login_phone_number_edit)
    protected AppCompatEditText mLoginPhoneNumberEdit;
    @BindView(R.id.login_phone_number_layout)
    protected TextInputLayout mLoginPhoneNumberLayout;
    @BindView(R.id.login_phone_send_button)
    protected AppCompatButton mLoginPhoneSendButton;

    @Inject
    protected CountriesCodesTool mCountriesCodesTool;

    @InjectPresenter
    EnterprisePhonePresenter mEnterprisePhonePresenter;

    private int mCountryCode;
    private String mCountryName;
    private String mCountryRegion;

    public static EnterprisePhoneFragment newInstance() {
        return new EnterprisePhoneFragment();
    }

    public static EnterprisePhoneFragment newInstance(final int code, final String name, final String region) {
        final EnterprisePhoneFragment phoneFragment = new EnterprisePhoneFragment();
        final Bundle bundle = new Bundle();
        bundle.putInt(TAG_CODE, code);
        bundle.putString(TAG_NAME, name);
        bundle.putString(TAG_REGION, region);
        phoneFragment.setArguments(bundle);
        return phoneFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_enterprise_phone, container, false);
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
        mEnterprisePhonePresenter.cancelRequest();
    }

    @OnClick(R.id.login_phone_send_button)
    protected void sendSmsClick() {
        final String phoneNumber = mLoginPhoneNumberEdit.getText().toString().trim().replace(" ", "");
        final String validNumber = mCountriesCodesTool.getPhoneE164(phoneNumber, mCountryRegion);
        if (validNumber != null) {
            showWaitingDialog(getString(R.string.dialogs_wait_title));
            mEnterprisePhonePresenter.setPhone(validNumber);
        } else {
            final String message = getString(R.string.login_sms_phone_error_format);
            mLoginPhoneNumberLayout.setError(message);
        }
    }

    @OnEditorAction(R.id.login_phone_number_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            sendSmsClick();
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
    public void onSuccessChange() {
        hideDialog();
        showFragment(EnterpriseSmsFragment.newInstance(false, null), EnterpriseSmsFragment.TAG, false);
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.login_sms_phone_number_verification));
        mLoginPhoneCountryEdit.setOnClickListener(v -> showFragment(CountriesCodesFragment.newInstance(), CountriesCodesFragment.TAG, false));
        final CountriesCodesTool.Codes codes = mCountriesCodesTool.getCodeByRegion(Locale.getDefault().getCountry());
        if (codes != null) {
            mCountryCode = codes.mNumber;
            mCountryName = codes.mName;
            mCountryRegion = codes.mCode;
        }

        showKeyboard(mLoginPhoneNumberEdit);
        mLoginPhoneNumberEdit.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mLoginPhoneNumberEdit.setText("+" + mCountryCode);
        mLoginPhoneNumberEdit.addTextChangedListener(new FieldsWatcher());

        mLoginPhoneCountryEdit.setText(mCountryName);
        mLoginPhoneCountryEdit.setKeyListener(null);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(TAG_CODE) && bundle.containsKey(TAG_NAME) && bundle.containsKey(TAG_REGION)) {
                mCountryCode = bundle.getInt(TAG_CODE);
                mCountryName = bundle.getString(TAG_NAME);
                mCountryRegion = bundle.getString(TAG_REGION);
                mLoginPhoneCountryEdit.setText(mCountryName);
                mLoginPhoneNumberEdit.setText("+" + mCountryCode);
            }
        }

        final int position = mLoginPhoneNumberEdit.getText().toString().length();
        mLoginPhoneNumberEdit.setSelection(position);
    }

    /*
     * Phone edit field
     * */
    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mLoginPhoneNumberLayout.setErrorEnabled(false);
        }
    }

}
