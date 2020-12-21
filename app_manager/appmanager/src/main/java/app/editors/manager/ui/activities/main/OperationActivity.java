package app.editors.manager.ui.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.states.OperationsState;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment;
import app.editors.manager.ui.fragments.operations.DocsOperationSectionFragment;
import app.editors.manager.ui.fragments.operations.DocsWebDavOperationFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class OperationActivity extends BaseAppActivity {

    public static final String TAG = OperationActivity.class.getSimpleName();
    public static final String TAG_OPERATION_TYPE = "TAG_OPERATION_OPERATION_TYPE";
    public static final String TAG_OPERATION_EXPLORER = "TAG_OPERATION_EXPLORER";
    public static final String TAG_IS_WEB_DAV = "TAG_IS_WEB_DAV";

    public interface OnActionClickListener {
        void onActionClick();
    }

    private Unbinder mUnbinder;
    @BindView(R.id.app_layout)
    protected CoordinatorLayout mAppLayout;
    @BindView(R.id.app_bar_layout)
    protected AppBarLayout mAppBarLayout;
    @BindView(R.id.app_bar_toolbar)
    protected Toolbar mAppBarToolbar;
    @BindView(R.id.operation_cancel_button)
    protected AppCompatButton mOperationCancelButton;
    @BindView(R.id.operation_action_button)
    protected AppCompatButton mOperationActionButton;

    @Inject
    protected PreferenceTool mPreferenceTool;

    private OperationsState.OperationType mOperationType;
    private OnActionClickListener mOnActionClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getApp().getAppComponent().inject(this);
        setContentView(R.layout.activity_operation);
        mUnbinder = ButterKnife.bind(this);
        init(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnClick({R.id.operation_cancel_button, R.id.operation_action_button})
    protected void onButtonClick(final View view) {
        switch (view.getId()) {
            case R.id.operation_cancel_button:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.operation_action_button:
                if (mOnActionClickListener != null) {
                    mOnActionClickListener.onActionClick();
                }
                break;
        }
    }

    private void init(final Bundle savedInstanceState) {
        setFinishOnTouchOutside(true);
        setSupportActionBar(mAppBarToolbar);
        mOperationType = (OperationsState.OperationType) getIntent().getSerializableExtra(OperationActivity.TAG_OPERATION_TYPE);
        initButton(mOperationType);

        if (savedInstanceState == null) {
            if (mPreferenceTool.isPersonalPortal()) {
                showFragment(DocsCloudOperationFragment.newInstance(Api.SectionType.CLOUD_USER), null);
            } else if (App.getApp().getAppComponent().getAccountsSql().getAccountOnline().isWebDav()) {
                AccountsSqlData account = App.getApp().getAppComponent().getAccountsSql().getAccountOnline();
                showFragment(DocsWebDavOperationFragment.newInstance(WebDavApi.Providers.valueOf(account.getWebDavProvider())), null);
            } else {
                showFragment(DocsOperationSectionFragment.newInstance(), null);
            }
        }
    }

    private void initButton(OperationsState.OperationType actionOperationType) {
        switch (actionOperationType) {
            case COPY:
                mOperationActionButton.setText(R.string.operation_panel_copy_button);
                break;
            case MOVE:
                mOperationActionButton.setText(R.string.operation_panel_move_button);
                break;
        }
    }

    public void setEnabledActionButton(final boolean isEnabled) {
        mOperationActionButton.setEnabled(isEnabled);
    }

    public void setOnActionClickListener(OnActionClickListener onActionClickListener) {
        mOnActionClickListener = onActionClickListener;
    }

    public static void showCopy(final Fragment fragment, @NonNull Explorer explorer) {
        final Intent intent = new Intent(fragment.getContext(), OperationActivity.class);
        intent.putExtra(OperationActivity.TAG_OPERATION_TYPE, OperationsState.OperationType.COPY);
        intent.putExtra(OperationActivity.TAG_OPERATION_EXPLORER, explorer);
        fragment.startActivityForResult(intent, REQUEST_ACTIVITY_OPERATION);
    }

    public static void showMove(final Fragment fragment, @NonNull Explorer explorer) {
        final Intent intent = new Intent(fragment.getContext(), OperationActivity.class);
        intent.putExtra(OperationActivity.TAG_OPERATION_TYPE, OperationsState.OperationType.MOVE);
        intent.putExtra(OperationActivity.TAG_OPERATION_EXPLORER, explorer);
        fragment.startActivityForResult(intent, REQUEST_ACTIVITY_OPERATION);
    }

}
