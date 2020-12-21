package app.editors.manager.ui.fragments.login;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import moxy.presenter.InjectPresenter;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.receivers.SmsReceiver;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.login.EnterpriseAppAuthPresenter;
import app.editors.manager.mvp.views.login.EnterpriseAppView;
import app.editors.manager.ui.activities.login.AuthAppActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseEditText;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.ActivitiesUtils;
import lib.toolkit.base.managers.utils.KeyboardUtils;
import lib.toolkit.base.managers.utils.StringUtils;

public class AuthPageFragment extends BaseAppFragment implements EnterpriseAppView {

    public static final String TAG = AuthPageFragment.class.getSimpleName();
    public static String SECRET_LABEL = "SECRET_LABEL";

    private static String KEY_POSITION = "KEY_POSITION";
    private static String KEY_TITLE = "KEY_TITLE";
    private static String GOOGLE_AUTHENTICATOR_URL = "com.google.android.apps.authenticator2";

    private static final String CODE_PLACEHOLDER = "−";
    private static final String PATTERN_CODE = "^[a-zA-Z0-9_-]*$";

    @BindView(R.id.auth_page_header)
    AppCompatTextView mAuthPageHeader;
    @BindView(R.id.auth_page_image)
    AppCompatImageView mAuthPageImage;
    @BindView(R.id.auth_page_info)
    AppCompatTextView mAuthPageInfo;
    @BindView(R.id.auth_secret_key_edit_text)
    AppCompatEditText mAuthSecretKeyEditText;
    @BindView(R.id.auth_secret_key_layout)
    LinearLayoutCompat mAuthSecretKeyLayout;
    @BindView(R.id.confirm_button)
    AppCompatButton mConfirmButton;
    @BindView(R.id.auth_copy_button)
    AppCompatImageButton mCopyButton;
    @BindView(R.id.auth_code_edit)
    BaseEditText mAuthCode;

    private int mFragmentPosition;

    private Unbinder mUnbinder;
    private PreferenceTool mPreferenceTool;
    private FieldsWatch mCodeListener;
    private int mPosition = 0;

    @InjectPresenter
    public EnterpriseAppAuthPresenter mTfaPresenter;

    public static AuthPageFragment newInstance(int position, AccountsSqlData sqlData) {
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        args.putParcelable(AuthAppActivity.ACCOUNT_KEY, sqlData);
        AuthPageFragment fragment = new AuthPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(KEY_POSITION)) {
            mFragmentPosition = getArguments().getInt(KEY_POSITION);
        }
        mPreferenceTool = App.getApp().getAppComponent().getPreference();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPosition == AuthPagerFragment.KEY_FOURTH_FRAGMENT || getParentFragment() == null) {
            checkCode();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            checkCode();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
            outState.putString(KEY_TITLE, getSupportActionBar().getTitle().toString());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_page, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment(savedInstanceState);
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_TITLE)) {
            setActionBarTitle(savedInstanceState.getString(KEY_TITLE));
        }
        Context context;
        if (getContext() != null) {
            context = getContext();
            switch (mFragmentPosition) {
                case AuthPagerFragment.KEY_FIRST_FRAGMENT:
                    initFirstFragment(context);
                    break;
                case AuthPagerFragment.KEY_SECOND_FRAGMENT:
                    initSecondFragment(context);
                    break;
                case AuthPagerFragment.KEY_THIRD_FRAGMENT:
                    initThirdFragment(context);
                    break;
                case AuthPagerFragment.KEY_FOURTH_FRAGMENT:
                    initFourthFragment(context);
                    break;
            }
        }
    }

    private void initFirstFragment(Context context) {
        mAuthPageHeader.setText(R.string.auth_header_screen_1);
        mAuthPageImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.image_auth_screen_1));
        mAuthPageInfo.setText(R.string.auth_info_screen_1);
        mAuthSecretKeyLayout.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);
        mConfirmButton.setText(getString(R.string.app_versions_without_release_accept));
        mConfirmButton.setOnClickListener(v -> ActivitiesUtils.showPlayMarket(context, GOOGLE_AUTHENTICATOR_URL));
    }

    private void initSecondFragment(Context context) {
        mAuthPageHeader.setText(R.string.auth_header_screen_2);
        mAuthPageImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.image_auth_screen_2));
        mAuthPageInfo.setText(R.string.auth_info_screen_2);
        mAuthSecretKeyLayout.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.GONE);
    }

    private void initThirdFragment(Context context) {
        mAuthPageHeader.setText(R.string.auth_header_screen_3);
        mAuthPageImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.image_auth_screen_3));
        mAuthPageInfo.setText(R.string.auth_info_screen_3);
        mConfirmButton.setVisibility(View.GONE);
        mAuthSecretKeyLayout.setVisibility(View.VISIBLE);
        mAuthSecretKeyEditText.setText(mPreferenceTool.getSecretKey());
        mCopyButton.setOnClickListener(v -> {
            if (mAuthSecretKeyEditText.getText() != null) {
                KeyboardUtils.setDataToClipboard(context, mAuthSecretKeyEditText.getText().toString(), SECRET_LABEL);
            }
        });

    }

    private void initFourthFragment(Context context) {
        mCodeListener = new FieldsWatch();

        mAuthPageHeader.setText(R.string.auth_header_screen_4);
        mAuthPageImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.image_auth_screen_4));
        mAuthPageInfo.setText(R.string.auth_info_screen_4);
        mCopyButton.setVisibility(View.GONE);
        mAuthCode.setVisibility(View.VISIBLE);
        mAuthCode.addTextChangedListener(mCodeListener);
        mAuthCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && mConfirmButton.isEnabled()) {
                mConfirmButton.callOnClick();
                return true;
            }
            return false;
        });
        mAuthSecretKeyLayout.setVisibility(View.GONE);

        mConfirmButton.setVisibility(View.VISIBLE);
        mConfirmButton.setEnabled(false);
        mConfirmButton.setText(getString(R.string.on_boarding_next_button));
        mConfirmButton.setOnClickListener(v -> {
            showWaitingDialog(getString(R.string.dialogs_wait_title));
            mTfaPresenter.signInPortal(mAuthCode.getText().toString(), getArguments().getParcelable(AuthAppActivity.ACCOUNT_KEY));
        });
    }

    public void onPageSelected(int position) {
        mPosition = position;
        if (position == AuthPagerFragment.KEY_FOURTH_FRAGMENT) {
            checkCode();
        } else if (position == AuthPagerFragment.KEY_THIRD_FRAGMENT) {
            new Handler().postDelayed(this::openAuth, 1000);
        }
    }

    private void openAuth() {
        try {
            String uri = "otpauth://totp/" + mPreferenceTool.getLogin() + "?secret=" + mPreferenceTool.getSecretKey() + "&issuer= " + mPreferenceTool.getPortal();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "openAuth: " + e.getMessage());
        }

    }

    private void checkCode() {
        if (getContext() != null){
            final String code = KeyboardUtils.getTextFromClipboard(getContext());
            if (code != null && !code.isEmpty() && code.length() == 6 &&
                    mFragmentPosition == AuthPagerFragment.KEY_FOURTH_FRAGMENT) {
                mAuthCode.setText(code);
                clearClipboard();
                new Handler().postDelayed(() -> mConfirmButton.callOnClick(), 500);
            }
        }
    }

    @Override
    public void onSuccessLogin() {
        hideDialog();
        MainActivity.show(getContext());
        getActivity().finish();
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        showSnackBar(message);
    }

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

                if (!"".equals(mSubString)) {
                    if (count == 6) {
                        mSelectPosition = 0;
                    } else {
                        mSelectPosition = start + count;
                    }
                    mSrcString.replace(start, start + count, mSubString);
                } else {
                    final String repeat = StringUtils.repeatString(CODE_PLACEHOLDER, before);
                    mSelectPosition = start;
                    mSrcString.replace(start, start + before, repeat);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

            String resultString = mSrcString.toString();
            if (resultString.length() > 6) {
                resultString = KeyboardUtils.getTextFromClipboard(getContext());
            }

            mAuthCode.removeTextChangedListener(mCodeListener);
            mAuthCode.setText(resultString);
            mAuthCode.setSelection(mSelectPosition);
            mAuthCode.addTextChangedListener(mCodeListener);

            if (resultString.matches(PATTERN_CODE)) {
                mConfirmButton.setEnabled(true);
            } else {
                mConfirmButton.setEnabled(false);
            }
        }
    }

}