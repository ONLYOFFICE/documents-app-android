package app.editors.manager.ui.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.managers.providers.CloudFileProvider;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.presenters.main.DocsBasePresenter;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import lib.toolkit.base.managers.utils.UiUtils;

public class DocsTrashFragment extends DocsCloudFragment implements View.OnClickListener {

    private MainActivity mMainActivity;
    private MenuItem mEmptyTrashItem;

    public static DocsTrashFragment newInstance() {
        return new DocsTrashFragment();
    }

    public static final String ID = CloudFileProvider.Section.Trash.getPath();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMainActivity = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(DocsTrashFragment.class.getSimpleName() + " - must implement - " +
                    MainActivity.class.getSimpleName());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        showMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        if (isVisible()) {
            showMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toolbar_item_empty_trash) {
            showQuestionDialog(getString(R.string.trash_dialog_empty_title), null,
                    getString(R.string.dialogs_question_accept_yes), getString(R.string.dialogs_question_accept_no),
                    DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY);
        }

        return super.onOptionsItemSelected(item);
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
        mCloudPresenter.setTrashMode(true);
        mCloudPresenter.getItemsById(ID);
        initViews();
    }

    @Override
    public void onClick(View v) {
        mCloudPresenter.moveContext();
    }

    @Override
    public void onItemContextClick(View view, int position) {
        final Item item = (Item) mExplorerAdapter.getItem(position);
        mCloudPresenter.onContextClick(item, position, true);
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
    protected void setMenuMainEnabled(boolean isEnabled) {
        super.setMenuMainEnabled(isEnabled);
        if (mEmptyTrashItem != null) {
            mEmptyTrashItem.setVisible(isEnabled);
        }
        if (mSearchItem != null) {
            mSearchItem.setVisible(isEnabled);
        }
    }

    @Override
    protected boolean isActivePage() {
        return true;
    }

    private void init() {
        mMainActivity.setNavigationButton(true);
    }

    private void showMenu(Menu menu) {
        if (mCloudPresenter.isSelectionMode()) {
            mDeleteItem.setVisible(true);
            mMoveItem.setVisible(true);
            mCopyItem.setVisible(false);
        } else {
            mEmptyTrashItem = menu.findItem(R.id.toolbar_item_empty_trash);
            mEmptyTrashItem.setVisible(false);
            setActionBarTitle("");
            UiUtils.setMenuItemTint(requireContext(), mEmptyTrashItem, R.color.colorWhite);
        }
    }

    private void initViews() {
        if (mRecyclerView != null) {
            mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), mRecyclerView.getPaddingTop(),
                    mRecyclerView.getPaddingLeft(), 0);
            mCloudPresenter.setTrashMode(true);
            mCloudPresenter.checkBackStack();
        }
    }

    @Override
    public void onContextButtonClick(ContextBottomDialog.Buttons buttons) {
        switch (buttons) {
            case DELETE:
                showQuestionDialog(getString(R.string.trash_popup_delete), mCloudPresenter.getItemTitle(),
                        getString(R.string.dialogs_question_accept_delete), getString(R.string.dialogs_common_cancel_button),
                        DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT);
                break;
            case MOVE:
                mCloudPresenter.moveContext();
                break;
        }
        mContextBottomDialog.dismiss();
    }

    @Override
    public void onDeleteBatch(List<Entity> list) {
        if (list.isEmpty()) {
            setMenuMainEnabled(false);
        }
        super.onDeleteBatch(list);
    }
}
