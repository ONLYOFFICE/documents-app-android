package app.editors.manager.ui.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.mvp.models.account.Recent;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.presenters.main.DocsBasePresenter;
import app.editors.manager.mvp.presenters.main.DocsRecentPresenter;
import app.editors.manager.mvp.views.main.DocsBaseView;
import app.editors.manager.mvp.views.main.DocsRecentView;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.MediaActivity;
import app.editors.manager.ui.activities.main.WebViewerActivity;
import app.editors.manager.ui.adapters.RecentAdapter;
import app.editors.manager.ui.adapters.diffutilscallback.RecentDiffUtilsCallback;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.presenter.InjectPresenter;

public class DocsRecentFragment extends DocsBaseFragment implements DocsRecentView, RecentAdapter.OnClick {

    public static String TAG = DocsRecentFragment.class.getSimpleName();

    public static DocsRecentFragment newInstance() {
        return new DocsRecentFragment();
    }

    private static final String KEY_FILTER = "KEY_FILTER";

    @InjectPresenter
    DocsRecentPresenter mDocsRecentPresenter;

    private MainActivity mMainActivity;
    private RecentAdapter mAdapter;
    private CharSequence mFilterValue;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mMainActivity = (MainActivity) context;
        } else {
            throw new RuntimeException(DocsRecentFragment.class.getSimpleName() + " - must implement - " +
                    MainActivity.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FILTER)) {
            mFilterValue = savedInstanceState.getCharSequence(KEY_FILTER);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSearchView != null) {
            outState.putCharSequence(KEY_FILTER, mSearchView.getQuery());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
        getActivity().invalidateOptionsMenu();
        return super.onBackPressed();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        setMenuSearchEnabled(true);
        if (mMainItem != null) {
            mMainItem.setVisible(false);
        }
        if (mSortItem != null) {
            mSortItem.setVisible(true);
            mSortItem.setEnabled(true);
            mSortItem.getSubMenu().findItem(R.id.toolbar_sort_item_owner).setVisible(false);
//            mSortItem.getSubMenu().findItem(R.id.toolbar_sort_item_date_update).setChecked(true);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BaseAppActivity.REQUEST_ACTIVITY_WEB_VIEWER:
                mDocsRecentPresenter.getRecentFiles();
                break;
            case REQUEST_DOCS:
            case REQUEST_SHEETS:
            case REQUEST_PRESENTATION:
            case REQUEST_PDF:
                if (resultCode == Activity.RESULT_CANCELED) {
                    mDocsRecentPresenter.deleteTempFile();
                    break;
                } else if (resultCode == Activity.RESULT_OK) {
                    if (data.getData() != null) {
                        if (data.getBooleanExtra("EXTRA_IS_MODIFIED", false)) {
                            mDocsRecentPresenter.upload(data.getData(), null);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onStateUpdateFilter(boolean isFilter, @Nullable String value) {
        super.onStateUpdateFilter(isFilter, value);
        if (isFilter) {
            mMainActivity.setAppBarStates(false);
            if (mSearchView != null) {
                mSearchView.setQuery(mFilterValue, true);
            }
        } else {
            mMainActivity.setAppBarStates(false);
            mMainActivity.setNavigationButton(true);
        }
    }

    @Override
    public void onRemoveItemFromFavorites() {

    }

    @Override
    protected Boolean isWebDav() {
        return false;
    }

    private void init() {
        mMainActivity.setAppBarStates(false);
        mMainActivity.setNavigationButton(true);
        mMainActivity.setActionButtonVisibility(false);
        mMainActivity.setOffToolbarAccount();

        mAdapter = new RecentAdapter(getContext());
        mAdapter.setOnClick(this);

        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setPadding(getResources().getDimensionPixelSize(R.dimen.screen_left_right_padding),
                    getResources().getDimensionPixelSize(R.dimen.screen_top_bottom_padding),
                    getResources().getDimensionPixelSize(R.dimen.screen_left_right_padding),
                    getResources().getDimensionPixelSize(R.dimen.screen_bottom_padding));
        }
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setEnabled(false);
        }
        if (checkReadPermission()) {
            mDocsRecentPresenter.getRecentFiles();
        }

        setActionBarTitle(getString(R.string.fragment_recent_title));
    }

    @Override
    protected void onListEnd() {
        mDocsRecentPresenter.loadMore(mAdapter.getItemCount());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMenu != null) {
            boolean isAscending = mMenu.findItem(R.id.toolbar_sort_item_asc).isChecked();
            switch (item.getItemId()) {
                case R.id.toolbar_item_sort:
                    mMainActivity.setAppBarStates(false);
                    mMainActivity.setNavigationButton(true);
                    break;
                case R.id.toolbar_sort_item_title:
                    if(item.isChecked()) {
                        mDocsRecentPresenter.reverseSortOrder(mAdapter.getItemList());
                    } else {
                        mDocsRecentPresenter.sortBy(Api.Parameters.VAL_SORT_BY_TITLE, isAscending);
                    }
                    item.setChecked(true);
                    break;
                case R.id.toolbar_sort_item_date_update:
                    if(item.isChecked()) {
                        mDocsRecentPresenter.reverseSortOrder(mAdapter.getItemList());
                    } else {
                        mDocsRecentPresenter.sortBy(Api.Parameters.VAL_SORT_BY_UPDATED, isAscending);
                    }
                    item.setChecked(true);
                    break;
                case R.id.toolbar_sort_item_owner:
                    if(item.isChecked()) {
                        mDocsRecentPresenter.reverseSortOrder(mAdapter.getItemList());
                    } else {
                        mDocsRecentPresenter.sortBy(Api.Parameters.VAL_SORT_BY_OWNER, isAscending);
                    }
                    item.setChecked(true);
                    break;
                case R.id.toolbar_sort_item_size:
                    if(item.isChecked()) {
                        mDocsRecentPresenter.reverseSortOrder(mAdapter.getItemList());
                    } else {
                        mDocsRecentPresenter.sortBy(Api.Parameters.VAL_SORT_BY_SIZE, isAscending);
                    }
                    item.setChecked(true);
                    break;
                case R.id.toolbar_sort_item_type:
                    if(item.isChecked()) {
                        mDocsRecentPresenter.reverseSortOrder(mAdapter.getItemList());
                    } else {
                        mDocsRecentPresenter.sortBy(Api.Parameters.VAL_SORT_BY_TYPE, isAscending);
                    }
                    item.setChecked(true);
                    break;
                case R.id.toolbar_sort_item_asc:
                case R.id.toolbar_sort_item_desc:
                    mDocsRecentPresenter.reverseList(mAdapter.getItemList(), isAscending);
                    break;
            }
        }
        item.setChecked(true);
        return false;
    }

    @Override
    public void onReverseSortOrder(List<Entity> itemList) {
        mAdapter.setData(itemList);
        mAdapter.notifyDataSetChanged();
        if(mMenu.findItem(R.id.toolbar_sort_item_desc).isChecked()) {
            mMenu.findItem(R.id.toolbar_sort_item_asc).setChecked(true);
        } else {
            mMenu.findItem(R.id.toolbar_sort_item_desc).setChecked(true);
        }
    }


    @Override
    public void updateFiles(List<Entity> files) {
        if (!files.isEmpty()) {
            if (mAdapter.getItemList() != null && mRecyclerView != null) {
                updateDiffUtils(files);
                mRecyclerView.scrollToPosition(0);
            } else {
                mAdapter.setItems(files);
            }
            mPlaceholderViews.setVisibility(false);
            updateMenu(true);
        } else {
            mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.SEARCH);
            updateMenu(false);
        }
    }

    private void updateMenu(boolean isEnable) {
        if (mMenu != null && mSortItem != null && mSearchItem != null && mDeleteItem != null) {
            mSortItem.setEnabled(isEnable);
            mSearchItem.setEnabled(isEnable);
            mDeleteItem.setVisible(isEnable);
        }
    }

    private void updateDiffUtils(List<Entity> files) {
        RecentDiffUtilsCallback diffUtils = new RecentDiffUtilsCallback(files, mAdapter.getItemList());
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(diffUtils);
        mAdapter.set(files, result);
    }

    @Override
    public void openFile(File file) {
        String ext = file.getFileExst();
        if (StringUtils.isVideoSupport(ext) || StringUtils.isImage(ext)) {
            MediaActivity.show(this, getExplorer(file), false);
        } else if (StringUtils.isDocument(ext)) {
            WebViewerActivity.show(requireActivity(), file);
        } else {
            onError(getString(R.string.error_unsupported_format));
        }
    }

    private Explorer getExplorer(File file) {
        Explorer explorer = new Explorer();
        List<File> files = new ArrayList<>();
        files.add(file);
        explorer.setFiles(files);
        return explorer;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_READ_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mDocsRecentPresenter.getRecentFiles();
            } else {
                mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.ACCESS);
            }
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchCloseButton.setVisibility(newText.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        if (!newText.equals("")) {
            mDocsRecentPresenter.searchRecent(newText.toLowerCase());
        } else {
            mDocsRecentPresenter.getRecentFiles();
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public void onFileClick(Recent recent, int position) {
        mDocsRecentPresenter.fileClick(recent, position);
    }

    @Override
    public void onContextClick(Recent recent, int position) {
        mDocsRecentPresenter.contextClick(recent, position);
    }

    @Override
    public void onMoveElement(Recent recent, int position) {
        mAdapter.moveItem(position, 0);
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onContextShow(ContextBottomDialog.State state) {
        if (getFragmentManager() != null) {
            mContextBottomDialog.setState(state);
            mContextBottomDialog.show(getFragmentManager(), ContextBottomDialog.TAG);
        }
    }

    @Override
    public void onDeleteItem(int position) {
        mAdapter.removeItem(position);
    }

    @Override
    public void onContextButtonClick(ContextBottomDialog.Buttons buttons) {
        if (buttons == ContextBottomDialog.Buttons.DELETE) {
            mDocsRecentPresenter.deleteRecent();
        }
        mContextBottomDialog.dismiss();
    }

    @Override
    public void onOpenDocs(Uri uri) {
        showEditors(uri, EditorsType.DOCS);
    }

    @Override
    public void onOpenCells(Uri uri) {
        showEditors(uri, EditorsType.CELLS);
    }

    @Override
    public void onOpenPresentation(Uri uri) {
        showEditors(uri, EditorsType.PRESENTATION);
    }

    @Override
    public void onOpenPdf(Uri uri) {
        showEditors(uri, EditorsType.PDF);
    }

    @Override
    public void onOpenMedia(Explorer images, boolean isWebDav) {
        MediaActivity.show(this, images, isWebDav);
    }

    @Override
    protected DocsBasePresenter<? extends DocsBaseView> getPresenter() {
        return mDocsRecentPresenter;
    }
}
