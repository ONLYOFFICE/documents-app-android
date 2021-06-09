package app.editors.manager.ui.fragments.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.managers.providers.CloudFileProvider;

public class DocsCommonFragment extends DocsCloudFragment {

    public static DocsCommonFragment newInstance() {
        return new DocsCommonFragment();
    }

    public static final String ID = CloudFileProvider.Section.Common.getPath();

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
        if  (mCloudPresenter.getStack() == null){
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

    @Override
    public void onRemoveItemFromFavorites() {

    }

    private void init() {
        mCloudPresenter.checkBackStack();
    }

}
