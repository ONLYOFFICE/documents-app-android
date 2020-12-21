package app.editors.manager.ui.fragments.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.R;
import app.editors.manager.managers.providers.CloudFileProvider;


public class DocsProjectsFragment extends DocsCloudFragment {

    public static DocsProjectsFragment newInstance() {
        return new DocsProjectsFragment();
    }

    public static final String ID = CloudFileProvider.Section.Projects.getPath();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    protected boolean onSwipeRefresh() {
        if (!super.onSwipeRefresh()) {
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
    public void onError(@Nullable String message) {
        if (message != null) {
            if (!message.equals(getString(R.string.errors_server_error) + "500")) {
                super.onError(message);
            }
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
        mCloudPresenter.checkBackStack();
    }

}
