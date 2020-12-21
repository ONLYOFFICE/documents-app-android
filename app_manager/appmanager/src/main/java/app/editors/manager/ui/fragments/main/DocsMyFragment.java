package app.editors.manager.ui.fragments.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.managers.providers.CloudFileProvider;
import app.editors.manager.ui.activities.main.MainActivity;

public class DocsMyFragment extends DocsCloudFragment {

    public static DocsMyFragment newInstance() {
        return new DocsMyFragment();
    }

    public static final String ID = CloudFileProvider.Section.My.getPath();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    protected boolean onSwipeRefresh() {
        if (!super.onSwipeRefresh() && mCloudPresenter != null) {
            mCloudPresenter.getItemsById(ID);
            return true;
        }

        return false;
    }

    @Override
    public void onScrollPage() {
        super.onScrollPage();
        if (mCloudPresenter.getStack() == null) {
            mCloudPresenter.getItemsById(ID);
        }
    }


    @Override
    public void onStateEmptyBackStack() {
        super.onStateEmptyBackStack();
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(true);
        }
        mCloudPresenter.getItemsById(ID);
    }

    private void init() {
        mExplorerAdapter.setSectionMy(true);
        mCloudPresenter.checkBackStack();
        getArgs();
    }
}
