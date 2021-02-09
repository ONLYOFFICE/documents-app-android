package app.editors.manager.ui.fragments.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.R;
import app.editors.manager.managers.providers.CloudFileProvider;
import app.editors.manager.mvp.presenters.main.DocsBasePresenter;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

public class DocsShareFragment extends DocsCloudFragment {

    public static DocsShareFragment newInstance() {
        return new DocsShareFragment();
    }

    public static final String ID = CloudFileProvider.Section.Shared.getPath();

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        if (mCloudPresenter.isSelectionMode()) {
            MenuItem mShareDeleteItem = menu.findItem(R.id.toolbar_selection_share_delete);
            mShareDeleteItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toolbar_selection_share_delete) {
            mCloudPresenter.removeShare();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollPage() {
        super.onScrollPage();
        if (mCloudPresenter.getStack() == null) {
            mCloudPresenter.getItemsById(ID);
        }
    }

    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            if (DocsBasePresenter.TAG_DIALOG_ACTION_REMOVE_SHARE.equals(tag)) {
                mCloudPresenter.removeShareSelected();
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

    @Override
    public void onRemoveItemFromFavorites() {

    }

    private void init() {
        mCloudPresenter.checkBackStack();
    }

}
