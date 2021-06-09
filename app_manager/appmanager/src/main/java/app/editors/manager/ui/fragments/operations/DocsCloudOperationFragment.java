package app.editors.manager.ui.fragments.operations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import app.documents.core.network.ApiContract;
import app.editors.manager.R;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.states.OperationsState;
import app.editors.manager.ui.activities.main.OperationActivity;
import app.editors.manager.ui.fragments.main.DocsCloudFragment;
import app.editors.manager.ui.fragments.main.DocsCommonFragment;
import app.editors.manager.ui.fragments.main.DocsMyFragment;
import app.editors.manager.ui.fragments.main.DocsProjectsFragment;
import app.editors.manager.ui.fragments.main.DocsShareFragment;


public class DocsCloudOperationFragment extends DocsCloudFragment implements OperationActivity.OnActionClickListener {

    public static final String TAG = DocsCloudOperationFragment.class.getSimpleName();

    private static final String TAG_OPERATION_SECTION_TYPE = "TAG_OPERATION_SECTION_TYPE";

    private OperationActivity mOperationActivity;
    private OperationsState.OperationType mOperationType;
    private int mSectionType;

    public static DocsCloudOperationFragment newInstance(final int sectionType) {
        final DocsCloudOperationFragment docsOperationFragment = new DocsCloudOperationFragment();
        final Bundle bundle = new Bundle();
        bundle.putInt(TAG_OPERATION_SECTION_TYPE, sectionType);
        docsOperationFragment.setArguments(bundle);
        return docsOperationFragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
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

    @Override
    public void onRemoveItemFromFavorites() {
        //stub
    }

    public void onActionClick() {
        switch (mOperationType) {
            case COPY:
                mCloudPresenter.copy();
                break;
            case MOVE:
                mCloudPresenter.move();
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
        mCloudPresenter.checkBackStack();
    }

    private void getArgs(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            mSectionType = bundle.getInt(TAG_OPERATION_SECTION_TYPE);
            final Intent intent = requireActivity().getIntent();
            mOperationType = (OperationsState.OperationType) intent.getSerializableExtra(OperationActivity.TAG_OPERATION_TYPE);

            if (savedInstanceState == null) {
                final Explorer explorer = (Explorer) intent.getSerializableExtra(OperationActivity.TAG_OPERATION_EXPLORER);
                if (explorer != null) {
                    mCloudPresenter.setOperationExplorer(explorer);
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
        mCloudPresenter.setFoldersMode(true);
        switch (mSectionType) {
            case ApiContract.SectionType.CLOUD_USER:
                mCloudPresenter.getItemsById(DocsMyFragment.ID);
                break;
            case ApiContract.SectionType.CLOUD_SHARE:
                mCloudPresenter.getItemsById(DocsShareFragment.ID);
                break;
            case ApiContract.SectionType.CLOUD_COMMON:
                mCloudPresenter.getItemsById(DocsCommonFragment.ID);
                break;
            case ApiContract.SectionType.CLOUD_PROJECTS:
                mCloudPresenter.getItemsById(DocsProjectsFragment.Companion.getID());
                break;
        }
    }

}
