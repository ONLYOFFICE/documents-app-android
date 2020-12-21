package app.editors.manager.ui.fragments.login;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import moxy.presenter.InjectPresenter;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.receivers.SmsReceiver;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.login.EnterpriseSmsPresenter;
import app.editors.manager.mvp.views.login.EnterpriseSmsView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.custom.TimerViews;
import app.editors.manager.ui.views.edits.BaseEditText;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.StringUtils;


public class EnterpriseSmsFragment extends BaseAppFragment implements EnterpriseSmsView, BaseEditText.OnContextMenu {

    public static final String TAG = EnterpriseSmsFragment.class.getSimpleName();

    public static final String TAG_TIMER = "TAG_TIMER";
    public static final String TAG_SMS = "TAG_SMS";
    public static final String TAG_ACCOUNT = "TAG_ACCOUNT";

    private static final String SMS_CODE_PLACEHOLDER = "−";
    private static final String PATTER_DIGITS = ".*\\d+.*";
    private static final String PATTERN_NUMERIC = "^[0-9]*$";

    private static final int RESEND_TIMER = 30;

    protected Unbinder mUnbinder;
    @BindView(R.id.login_sms_code_number_text)
    protected TextView mLoginSmsCodeNumberText;
    @BindView(R.id.login_sms_code_edit)
    protected BaseEditText mLoginSmsCodeEdit;
    @BindView(R.id.login_sms_code_send_again_button)
    protected AppCompatButton mLoginSmsCodeSendAgainButton;
    @BindView(R.id.login_sms_code_change_number_button)
    protected AppCompatButton mLoginSmsCodeChangeNumberButton;

    @Inject
    protected PreferenceTool mPreferenceTool;

    @InjectPresenter
    EnterpriseSmsPresenter mEnterpriseSmsPresenter;

    private FieldsWatch mSmsInputWatch;
    private TimerViews mTimerViews;

    public static EnterpriseSmsFragment newInstance(final boolean isSms, AccountsSqlData accountsSql) {
        final EnterpriseSmsFragment inputSmsFragment = new EnterpriseSmsFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(TAG_SMS, isSms);
        bundle.putParcelable(TAG_ACCOUNT, accountsSql);
        inputSmsFragment.setArguments(bundle);
        return inputSmsFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_enterprise_sms, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        setDataFromClipboard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTimerViews.removeFragment();
        mTimerViews.cancel(true);
        mUnbinder.unbind();
        mEnterpriseSmsPresenter.cancelRequest();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAG_TIMER, mTimerViews.getCurrentTimer());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO check permission, output result or repeat sms request
                }
                break;
            }
        }
    }

    @OnClick(R.id.login_sms_code_send_again_button)
    protected void resendSmsClick() {
        hideKeyboard(mLoginSmsCodeEdit);
        startTimer(RESEND_TIMER);
        mEnterpriseSmsPresenter.resendSms();
    }

    @OnClick(R.id.login_sms_code_change_number_button)
    protected void changeNumberClick() {
        showFragment(EnterprisePhoneFragment.newInstance(), EnterprisePhoneFragment.TAG, false);
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
    public void onResendSms() {
        showSnackBar(R.string.login_sms_resend_ok);
    }

    @Override
    public boolean onTextPaste(@Nullable String text) {
        if (text != null) {
            final String code = SmsReceiver.getCodeFromSms(text);
            if (!code.isEmpty()) {
                mLoginSmsCodeEdit.setText(code);
                return true;
            }
        }

        return false;
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.login_sms_phone_header));
        mLoginSmsCodeNumberText.setText(String.valueOf(mPreferenceTool.getPhoneNoise()));
        mSmsInputWatch = new FieldsWatch();
        mLoginSmsCodeEdit.setTextColor(mLoginSmsCodeEdit.getHintTextColors());
        mLoginSmsCodeEdit.addTextChangedListener(mSmsInputWatch);
        mLoginSmsCodeEdit.setOnClickListener(new SetSelectionOnClick());
        mLoginSmsCodeEdit.setCursorVisible(false);
        mLoginSmsCodeEdit.setOnContextMenu(this);
        mLoginSmsCodeChangeNumberButton.setVisibility(View.INVISIBLE);

        getArgs();
        showKeyboard(mLoginSmsCodeEdit);
        restoreStates(savedInstanceState);

        if (savedInstanceState == null) {
            clearClipboard();
        }
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        if (bundle.getBoolean(TAG_SMS)) {
            mLoginSmsCodeChangeNumberButton.setVisibility(View.VISIBLE);
            mEnterpriseSmsPresenter.resendSms();
        }
    }

    private void restoreStates(final Bundle savedInstanceState) {
        int timer = RESEND_TIMER;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_TIMER)) {
                timer = savedInstanceState.getInt(TAG_TIMER);
            }
        } else {
            mLoginSmsCodeEdit.setSelection(0);
        }

        startTimer(timer);
    }

    /*
     * Get data from clipboard
     * */
    private void setDataFromClipboard() {
        final String data = getDataFromClipboard();
        if (SmsReceiver.isSmsCode(data)) {
            mLoginSmsCodeEdit.setText(SmsReceiver.getCodeFromSms(data));
            mLoginSmsCodeEdit.performClick();
        }
    }

    public void startTimer(final int timer) {
        if (mTimerViews == null || mTimerViews.isCancelled() || !mTimerViews.isActive()) {
            mTimerViews = new TimerViews(timer);
            mTimerViews.setFragment(this);
            mTimerViews.execute();
        }
    }

    public void setTimerButton(final int timer) {
        mLoginSmsCodeSendAgainButton.setEnabled(false);
        mLoginSmsCodeSendAgainButton.setText(getString(R.string.login_sms_send_again_after) + " " + timer);
    }

    public void setTimerButton() {
        mLoginSmsCodeSendAgainButton.setText(getString(R.string.login_sms_send_again));
        mLoginSmsCodeSendAgainButton.setEnabled(true);
    }


    /*
     * Set selection on click
     * */
    private class SetSelectionOnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            final String string = mLoginSmsCodeEdit.getText().toString();
            for (int i = 0; i < string.length(); i++) {
                if (Character.isDigit(string.charAt(i))) {
                    mLoginSmsCodeEdit.setSelection(i + 1);
                } else {
                    mLoginSmsCodeEdit.setSelection(i);
                    break;
                }
            }
        }
    }

    /*
     * Edit sms code controller
     * * */
    private class FieldsWatch extends BaseWatcher {

        private StringBuilder mSrcString;
        private String mSubString;
        private int mSelectPosition;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mSrcString = new StringBuilder(s);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (start < SmsReceiver.SMS_CODE_LENGTH && count <= SmsReceiver.SMS_CODE_LENGTH) {
                mSubString = s.subSequence(start, start + count).toString();
                // Input symbol or delete
                if (!"".equals(mSubString)) {
                    mSelectPosition = start + count;
                    mSrcString.replace(start, start + count, mSubString);
                } else {
                    final String repeat = StringUtils.repeatString(SMS_CODE_PLACEHOLDER, before);
                    mSelectPosition = start;
                    mSrcString.replace(start, start + before, repeat);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Result text
            final String resultString = mSrcString.toString();
            if (resultString.matches(PATTER_DIGITS)) {
                mLoginSmsCodeEdit.setTextColor(getResources().getColor(android.R.color.black));
            } else {
                mLoginSmsCodeEdit.setTextColor(mLoginSmsCodeEdit.getHintTextColors());
            }

            // Remove listener, else will be recursion
            mLoginSmsCodeEdit.removeTextChangedListener(mSmsInputWatch);
            mLoginSmsCodeEdit.setText(resultString);
            mLoginSmsCodeEdit.setSelection(mSelectPosition);
            mLoginSmsCodeEdit.addTextChangedListener(mSmsInputWatch);

            // Check length of sms code
            if (resultString.matches(PATTERN_NUMERIC)) {
                showWaitingDialog(getString(R.string.dialogs_wait_title));
                mEnterpriseSmsPresenter.signInPortal(resultString, getArguments().getParcelable(TAG_ACCOUNT));
            }
        }
    }

}
