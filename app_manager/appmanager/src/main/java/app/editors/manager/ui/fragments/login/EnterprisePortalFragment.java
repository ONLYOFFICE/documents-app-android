package app.editors.manager.ui.fragments.login;

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

import moxy.presenter.InjectPresenter;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.presenters.login.EnterprisePortalPresenter;
import app.editors.manager.mvp.views.login.EnterprisePortalView;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.activities.login.SignInActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.edits.BaseWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

public class EnterprisePortalFragment extends BaseAppFragment implements EnterprisePortalView,
        CommonDialog.OnClickListener {

    public static final String TAG = EnterprisePortalFragment.class.getSimpleName();
    private static final String TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING";
    private static final String TAG_DIALOG_HTTP = "TAG_DIALOG_HTTP";
    private static final String KEY_EMAIL = "KEY_EMAIL";

    protected Unbinder mUnbinder;
    @BindView(R.id.login_enterprise_portal_edit)
    protected AppCompatEditText mLoginEnterprisePortalEdit;
    @BindView(R.id.login_enterprise_portal_layout)
    protected TextInputLayout mLoginEnterprisePortalLayout;
    @BindView(R.id.login_enterprise_next_button)
    protected AppCompatButton mLoginEnterpriseNextButton;
    @BindView(R.id.login_enterprise_forgot_pwd_button)
    protected TextView mLoginEnterpriseInfoText;
    @BindView(R.id.login_enterprise_create_button)
    protected AppCompatButton mLoginEnterpriseCreateButton;

    @Inject
    protected AccountSqlTool mAccountSqlTool;

    @Inject
    protected PreferenceTool mPreferenceTool;

    @InjectPresenter
    EnterprisePortalPresenter mEnterprisePortalPresenter;

    private String mHttpPortal;

    public static EnterprisePortalFragment newInstance() {
        return new EnterprisePortalFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public boolean onBackPressed() {
        hideKeyboard();
        return super.onBackPressed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_enterprise_portal, container, false);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_EMAIL, mLoginEnterprisePortalEdit.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            if (TAG_DIALOG_HTTP.equals(tag)) {
                if (mHttpPortal != null) {
                    onSuccessPortal(mHttpPortal);
                    mHttpPortal = null;
                }
            }
        }
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_WAITING:
                    mEnterprisePortalPresenter.cancelRequest();
                    break;
            }
        }
    }

    @OnClick(R.id.login_enterprise_next_button)
    protected void onNextClick() {
        final String portalAddress = mLoginEnterprisePortalEdit.getText().toString();
        hideKeyboard(mLoginEnterprisePortalEdit);
        mEnterprisePortalPresenter.checkPortal(portalAddress);
    }

    @OnClick(R.id.login_enterprise_create_button)
    protected void onCreateClick() {
        SignInActivity.showPortalCreate(getContext());
    }

    @OnEditorAction(R.id.login_enterprise_portal_edit)
    protected boolean actionKeyPress(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onNextClick();
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
    public void onSuccessPortal(String portal) {
        hideDialog();
        SignInActivity.showPortalSignIn(getContext(), portal, "");
    }

    @Override
    public void onHttpPortal(String portal) {
        mHttpPortal = portal;
        showQuestionDialog(getString(R.string.dialogs_question_http_title),
                getString(R.string.dialogs_question_http_question), getString(R.string.dialogs_question_accept_yes),
                getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_HTTP);
    }

    @Override
    public void onPortalSyntax(String message) {
        mLoginEnterprisePortalLayout.setError(message);
    }

    @Override
    public void onShowDialog() {
        showWaitingDialog(getString(R.string.dialogs_check_portal_header_text), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING);
    }

    @Override
    public void onLoginPortal(String portal) {
        mLoginEnterprisePortalEdit.setText(portal);
    }

    private void init(final Bundle savedInstanceState) {
        mLoginEnterprisePortalEdit.clearFocus();
        mLoginEnterprisePortalEdit.addTextChangedListener(new FieldsWatcher());
        restoreValue(savedInstanceState);
        getIntent(savedInstanceState);
    }

    private void restoreValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLoginEnterprisePortalEdit.setText(savedInstanceState.getString(KEY_EMAIL));
        }
    }

    private void getIntent(final Bundle savedInstanceState) {
        final Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(PortalsActivity.TAG_ACTION_MESSAGE) && mPreferenceTool.getPortal() != null) {
                mLoginEnterprisePortalEdit.setText(mPreferenceTool.getPortal());
            }
        }
    }

    /*
     * Portal address edit field
     * */
    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mLoginEnterprisePortalLayout.setErrorEnabled(false);
        }
    }

}
