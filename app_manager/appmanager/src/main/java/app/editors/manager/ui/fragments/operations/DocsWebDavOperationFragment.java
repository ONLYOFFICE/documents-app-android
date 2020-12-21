package app.editors.manager.ui.fragments.operations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.states.OperationsState;
import app.editors.manager.ui.activities.main.OperationActivity;
import app.editors.manager.ui.fragments.main.DocsWebDavFragment;

public class DocsWebDavOperationFragment extends DocsWebDavFragment implements OperationActivity.OnActionClickListener {

    public static final String TAG = DocsWebDavOperationFragment.class.getSimpleName();

    private OperationActivity mOperationActivity;
    private OperationsState.OperationType mOperationType;

    public static DocsWebDavOperationFragment newInstance(WebDavApi.Providers provider) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_PROVIDER, provider);
        DocsWebDavOperationFragment fragment = new DocsWebDavOperationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(KEY_PROVIDER)) {
            mProvider = (WebDavApi.Providers) getArguments().getSerializable(KEY_PROVIDER);
        }
        setHasOptionsMenu(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOperationActivity = (OperationActivity) context;
            mOperationActivity.setOnActionClickListener(this);
        } catch (ClassCastException e) {
            throw new RuntimeException(DocsCloudOperationFragment.class.getSimpleName() + " - must implement - " +
                    OperationActivity.class.getSimpleName());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        mOperationActivity.setEnabledActionButton(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mOperationActivity.setOnActionClickListener(null);
        mOperationActivity.setEnabledActionButton(false);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        // Not actions
    }

    @Override
    public void onError(@Nullable String message) {
        super.onError(message);
        mOperationActivity.setEnabledActionButton(false);
    }

    @Override
    public void onDocsGet(List<Entity> list) {
        super.onDocsGet(list);
        mOperationActivity.setEnabledActionButton(true);
    }

    @Override
    public void onDocsBatchOperation() {
        super.onDocsBatchOperation();
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }

    @Override
    public void onStateEmptyBackStack() {
        super.onStateEmptyBackStack();
        setActionBarTitle(getString(R.string.operation_title));
        mSwipeRefresh.setRefreshing(true);

        getDocs();
    }

    public void onActionClick() {
        switch (mOperationType) {
            case COPY:
                mWebDavPresenter.copy();
                break;
            case MOVE:
                mWebDavPresenter.move();
                break;
        }
    }

    private void init(final Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        getArgs(savedInstanceState);
        initViews();
        mWebDavPresenter.checkBackStack();
    }

    private void getArgs(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            final Intent intent = requireActivity().getIntent();
            mOperationType = (OperationsState.OperationType) intent.getSerializableExtra(OperationActivity.TAG_OPERATION_TYPE);

            if (savedInstanceState == null) {
                final Explorer explorer = (Explorer) intent.getSerializableExtra(OperationActivity.TAG_OPERATION_EXPLORER);
                if (explorer != null) {
                    mWebDavPresenter.setOperationExplorer(explorer);
                } else {
                    requireActivity().finish();
                }
            }
        }
    }

    private void initViews() {
        mOperationActivity.setEnabledActionButton(false);
        mExplorerAdapter.setFoldersMode(true);
        mRecyclerView.setPadding(0, 0, 0, 0);
    }

    private void getDocs() {
        mWebDavPresenter.setFoldersMode(true);
        AccountsSqlData account = App.getApp().getAppComponent().getAccountsSql().getAccountOnline();

        WebDavApi.Providers provider = WebDavApi.Providers.valueOf(account.getWebDavProvider());
        if (provider == WebDavApi.Providers.NextCloud || provider == WebDavApi.Providers.OwnCloud ||
                provider == WebDavApi.Providers.WebDav) {
            mWebDavPresenter.getItemsById(account.getWebDavPath());
        } else {
            mWebDavPresenter.getItemsById(WebDavApi.Providers.valueOf(account.getWebDavProvider()).getPath());
        }
    }

}
