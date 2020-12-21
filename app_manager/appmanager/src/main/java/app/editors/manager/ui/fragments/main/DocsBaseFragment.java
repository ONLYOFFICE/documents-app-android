package app.editors.manager.ui.fragments.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Collections;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.presenters.main.DocsBasePresenter;
import app.editors.manager.mvp.views.base.BaseViewExt;
import app.editors.manager.mvp.views.main.DocsBaseView;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.MediaActivity;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import app.editors.manager.ui.adapters.diffutilscallback.EntityDiffUtilsCallback;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;
import app.editors.manager.ui.adapters.holders.factory.TypeFactoryExplorer;
import app.editors.manager.ui.dialogs.ActionBottomDialog;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.dialogs.MoveCopyDialog;
import app.editors.manager.ui.fragments.base.ListFragment;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import lib.toolkit.base.managers.utils.PermissionUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import lib.toolkit.base.ui.activities.base.BaseActivity;
import lib.toolkit.base.ui.adapters.BaseAdapter;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

import static lib.toolkit.base.ui.activities.base.BaseActivity.REQUEST_ACTIVITY_MEDIA;

public abstract class DocsBaseFragment extends ListFragment implements DocsBaseView, BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemContextListener, BaseAdapter.OnItemLongClickListener, ContextBottomDialog.OnClickListener,
        ActionBottomDialog.OnClickListener, SearchView.OnQueryTextListener, MoveCopyDialog.DialogButtonOnClick {

    private static final long CLICK_TIME_INTERVAL = 350;

    protected static final int REQUEST_OPEN_FILE = 10000;
    protected static final int REQUEST_DOCS = 10001;
    protected static final int REQUEST_PRESENTATION = 10002;
    protected static final int REQUEST_SHEETS = 10003;
    protected static final int REQUEST_PDF = 10004;

    protected enum EditorsType {
        DOCS, CELLS, PRESENTATION, PDF
    }

    /*
     * Toolbar menu
     * */
    protected MenuItem mSearchItem;
    protected MenuItem mOpenItem;
    protected MenuItem mSortItem;
    protected MenuItem mMainItem;
    protected MenuItem mDeleteItem;
    protected MenuItem mMoveItem;
    protected MenuItem mCopyItem;
    protected MenuItem mDownloadItem;
    protected MenuItem mSelectItem;
    protected SearchView mSearchView;
    protected ImageView mSearchCloseButton;

    protected ExplorerAdapter mExplorerAdapter;
    ContextBottomDialog mContextBottomDialog;
    ActionBottomDialog mActionBottomDialog;

    private long mLastClickTime = 0;

    private TypeFactory mTypeFactory = TypeFactoryExplorer.getFactory();
    MoveCopyDialog mMoveCopyDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isActivePage() && resultCode == Activity.RESULT_CANCELED && requestCode == BaseAppActivity.REQUEST_ACTIVITY_OPERATION) {
            onRefresh();
        } else if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_DOCS:
                case REQUEST_SHEETS:
                case REQUEST_PRESENTATION:
                    removeCommonDialog();
                case REQUEST_ACTIVITY_MEDIA:
                    break;
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isActivePage()) {
            switch (requestCode) {
                case PERMISSION_WRITE_STORAGE: {
                    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getPresenter().download();
                    }
                    break;
                }

                case PERMISSION_READ_STORAGE: {
                    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showMultipleFilePickerActivity();
                    }
                    break;
                }

                case PERMISSION_CAMERA: {
                    if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        showCameraActivity(TimeUtils.getFileTimeStamp());
                    }
                }
            }
        }
        if (requestCode == PERMISSION_READ_UPLOAD) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requireActivity().getIntent() != null) {
                    final Uri uri = requireActivity().getIntent().getClipData().getItemAt(0).getUri();
                    getPresenter().uploadToMy(uri);
                    requireActivity().setIntent(null);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        if (menu != null) {
            menu.clear();
            getPresenter().initMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Sort by
            case R.id.toolbar_item_search:
            case R.id.toolbar_item_sort:
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_date_update:
                getPresenter().sortBy(Api.Parameters.VAL_SORT_BY_UPDATED);
                item.setChecked(true);
                break;
//            case R.id.toolbar_sort_item_date_create:
//                getPresenter().sortBy(Api.Parameters.VAL_SORT_BY_CREATED);
//                item.setSelected(true);
//                break;
            case R.id.toolbar_sort_item_title:
                getPresenter().sortBy(Api.Parameters.VAL_SORT_BY_TITLE);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_type:
                getPresenter().sortBy(Api.Parameters.VAL_SORT_BY_TYPE);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_size:
                getPresenter().sortBy(Api.Parameters.VAL_SORT_BY_SIZE);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_owner:
                getPresenter().sortBy(Api.Parameters.VAL_SORT_BY_OWNER);
                item.setChecked(true);
                break;

            // Sort order
            case R.id.toolbar_sort_item_asc:
                getPresenter().orderBy(Api.Parameters.VAL_SORT_ORDER_ASC);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_desc:
                getPresenter().orderBy(Api.Parameters.VAL_SORT_ORDER_DESC);
                item.setChecked(true);
                break;

            // Main menu options
//            case R.id.toolbar_main_item_share:
//                break;
//            case R.id.toolbar_main_item_move:
//                getPresenter().moveSelected();
//                break;
//            case R.id.toolbar_main_item_rename:
//                break;
//            case R.id.toolbar_main_item_delete:
//                break;

            case R.id.toolbar_main_item_select:
                getPresenter().setSelection(true);
                break;
            case R.id.toolbar_main_item_select_all:
                getPresenter().setSelectionAll();
                break;

            // Selection menu
//            case R.id.toolbar_selection_share:
//                break;
            case R.id.toolbar_selection_delete:
                getPresenter().delete();
                break;
            case R.id.toolbar_selection_move:
                getPresenter().moveSelected();
                break;
            case R.id.toolbar_selection_copy:
                getPresenter().copySelected();
                break;
            case R.id.toolbar_selection_deselect:
                getPresenter().deselectAll();
                break;
            case R.id.toolbar_selection_select_all:
                getPresenter().selectAll();
                break;
            case R.id.toolbar_selection_download:
                if (checkWritePermission()) {
                    getPresenter().downloadSelected();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        return getPresenter().getBackStack();
    }

    @Override
    protected void onListEnd() {
        super.onListEnd();
        mExplorerAdapter.isLoading(true);
        getPresenter().getNextList();
    }

    @Override
    public final void onRefresh() {
        onSwipeRefresh();
    }

    protected boolean onSwipeRefresh() {
        return getPresenter().refresh();
    }

    /*
     * Views callbacks
     * */
    @Override
    public boolean onQueryTextSubmit(String query) {
        getPresenter().filter(query, true);
        mSearchView.onActionViewCollapsed();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchCloseButton.setVisibility(newText.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        getPresenter().filterWait(newText);
        return false;
    }

    @Override
    public void onItemContextClick(View view, int position) {
        final Entity item = mExplorerAdapter.getItem(position);
        if (item instanceof Item && !isFastClick()) {
            getPresenter().onContextClick((Item) item, position, false);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (!isFastClick() || mExplorerAdapter.isSelectMode()) {
            final Item item = (Item) mExplorerAdapter.getItem(position);
            getPresenter().onItemClick(item, position);
        }
    }

    protected boolean isFastClick() {
        long now = System.currentTimeMillis();
        if (now - mLastClickTime < CLICK_TIME_INTERVAL) {
            return true;
        } else {
            mLastClickTime = now;
            return false;
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        final Item item = (Item) mExplorerAdapter.getItem(position);
        getPresenter().setSelection(true);
        getPresenter().onItemClick(item, position);
    }

    @Override
    public void onNoProvider() {
        if (getActivity() != null && getContext() != null) {
            getActivity().finish();
            MainActivity.show(getContext());
        }
    }

    @Override
    public void onOpenLocalFile(File file) {
        Uri uri = Uri.parse(file.getWebUrl());
        switch (StringUtils.getExtension(file.getFileExst())) {
            case DOC:
                getPresenter().addRecent(file);
                showEditors(uri, EditorsType.DOCS);
                break;
            case SHEET:
                getPresenter().addRecent(file);
                showEditors(uri, EditorsType.CELLS);
                break;
            case PRESENTATION:
                getPresenter().addRecent(file);
                showEditors(uri, EditorsType.PRESENTATION);
                break;
            case PDF:
                getPresenter().addRecent(file);
                showEditors(uri, EditorsType.PDF);
                break;
            case VIDEO_SUPPORT:
                getPresenter().addRecent(file);
                Explorer explorer = new Explorer();
                File videoFile = file.clone();
                videoFile.setWebUrl(uri.getPath());
                videoFile.setId("");
                explorer.setFiles(Collections.singletonList(videoFile));
                MediaActivity.show(this, explorer, true);
                break;
            case UNKNOWN:
            case EBOOK:
            case ARCH:
            case VIDEO:
            case HTML:
                onSnackBar(getString(R.string.download_manager_complete));
                break;
        }
    }

    @Override
    public void onContextButtonClick(ContextBottomDialog.Buttons buttons) {
        switch (buttons) {
            case MOVE:
                getPresenter().moveContext();
                break;
            case COPY:
                getPresenter().copyContext();
                break;
            case DOWNLOAD:
                onFileDownloadPermission();
                break;
            case RENAME:
                if (getPresenter().getItemClicked() instanceof File) {
                    showEditDialogRename(getString(R.string.dialogs_edit_rename_title),
                            StringUtils.getNameWithoutExtension(getPresenter().getItemTitle()),
                            getString(R.string.dialogs_edit_hint), DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                            getString(R.string.dialogs_edit_accept_rename), getString(R.string.dialogs_common_cancel_button));
                } else {
                    showEditDialogRename(getString(R.string.dialogs_edit_rename_title),
                            getPresenter().getItemTitle(),
                            getString(R.string.dialogs_edit_hint), DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                            getString(R.string.dialogs_edit_accept_rename), getString(R.string.dialogs_common_cancel_button));
                }
                break;
            case DELETE:
                showQuestionDialog(getString(R.string.dialogs_question_delete), getPresenter().getItemTitle(),
                        getString(R.string.dialogs_question_accept_remove), getString(R.string.dialogs_common_cancel_button),
                        DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT);
                break;
        }
    }

    @Override
    public void onActionButtonClick(ActionBottomDialog.Buttons buttons) {
        switch (buttons) {
            case SHEET:
                showEditDialogCreate(getString(R.string.dialogs_edit_create_sheet), getString(R.string.dialogs_edit_create_sheet),
                        getString(R.string.dialogs_edit_hint), "." + Api.Extension.XLSX.toLowerCase(), DocsBasePresenter.TAG_DIALOG_ACTION_SHEET,
                        getString(R.string.dialogs_edit_accept_create), getString(R.string.dialogs_common_cancel_button));
                break;
            case PRESENTATION:
                showEditDialogCreate(getString(R.string.dialogs_edit_create_presentation), getString(R.string.dialogs_edit_create_presentation),
                        getString(R.string.dialogs_edit_hint), "." + Api.Extension.PPTX.toLowerCase(), DocsBasePresenter.TAG_DIALOG_ACTION_PRESENTATION,
                        getString(R.string.dialogs_edit_accept_create), getString(R.string.dialogs_common_cancel_button));
                break;
            case DOC:
                showEditDialogCreate(getString(R.string.dialogs_edit_create_docs), getString(R.string.dialogs_edit_create_docs),
                        getString(R.string.dialogs_edit_hint), "." + Api.Extension.DOCX.toLowerCase(), DocsBasePresenter.TAG_DIALOG_ACTION_DOC,
                        getString(R.string.dialogs_edit_accept_create), getString(R.string.dialogs_common_cancel_button));
                break;
            case FOLDER:
                showEditDialogCreate(getString(R.string.dialogs_edit_create_folder), "",
                        getString(R.string.dialogs_edit_hint), null, DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER,
                        getString(R.string.dialogs_edit_accept_create), getString(R.string.dialogs_common_cancel_button));
                break;
            case UPLOAD:
                getPresenter().uploadPermission();
                break;
        }
    }


    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            switch (tag) {
                case DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT:
                    getPresenter().delete();
                    break;
                case DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME:
                    getPresenter().rename(value);
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_SHEET:
                    getPresenter().createDocs(value + "." + Api.Extension.XLSX.toLowerCase());
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_PRESENTATION:
                    getPresenter().createDocs(value + "." + Api.Extension.PPTX.toLowerCase());
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_DOC:
                    getPresenter().createDocs(value + "." + Api.Extension.DOCX.toLowerCase());
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER:
                    getPresenter().createFolder(value);
                    break;
                case DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_SELECTED:
                    getPresenter().deleteItems();
                    break;
            }
        }
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        if (tag != null) {
            switch (tag) {
                case DocsBasePresenter.TAG_DIALOG_CANCEL_DOWNLOAD:
                    getPresenter().cancelDownload();
                    break;
                case DocsBasePresenter.TAG_DIALOG_CANCEL_UPLOAD:
                    getPresenter().cancelUpload();
                    break;
                case DocsBasePresenter.TAG_DIALOG_CANCEL_SINGLE_OPERATIONS:
                    getPresenter().cancelSingleOperationsRequests();
                    break;
                case DocsBasePresenter.TAG_DIALOG_CANCEL_BATCH_OPERATIONS:
                    getPresenter().terminate();
                    return;
            }
        }

        super.onCancelClick(dialogs, tag);
    }

    /*
     * Presenter callbacks
     * */
    @Override
    public void onError(@Nullable String message) {
        resetIndicators();
        hideDialog();
        if (message != null) {
//            TODO add webdav exeption
            if (message.equals("HTTP 503 Service Unavailable")) {
                setAccessDenied();
                getPresenter().clearStack();
                return;
            } else if (message.equals(getString(R.string.errors_client_host_not_found)) || message.equals(getString(R.string.errors_client_unauthorized))) {
                if (requireActivity() instanceof BaseViewExt) {
                    ((BaseViewExt) requireActivity()).onUnauthorized(message);
                    return;
                }
            }
            showSnackBar(message);
        }
    }

    private void setAccessDenied() {
        resetIndicators();
        setVisibilityActionButton(false);
        setScrollViewPager(false);
        setVisibleTabs(false);
        onStateMenuEnabled(false);
        setActionBarTitle("");
        onPlaceholder(PlaceholderViews.Type.ACCESS);
        getPresenter().setAccessDenied();
    }

    @Override
    public void onUnauthorized(@Nullable String message) {
        requireActivity().finish();
        MainActivity.show(getContext());
    }

    /*
     * Docs
     * */
    @Override
    public void onDocsGet(@Nullable List<Entity> list) {
        final boolean isEmpty = list != null && list.isEmpty();
        setViewState(isEmpty);
        onStateMenuEnabled(!isEmpty);
        mExplorerAdapter.setItems(list);
    }

    @Override
    public void onDocsRefresh(@Nullable List<Entity> list) {
        final boolean isEmpty = list != null && list.isEmpty();
        setViewState(isEmpty);
        onStateMenuEnabled(!isEmpty);
        mExplorerAdapter.setItems(list);
        mRecyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onDocsFilter(@Nullable List<Entity> list) {
        final boolean isEmpty = list != null && list.isEmpty();
        setViewState(isEmpty);
        setMenuMainEnabled(!isEmpty);
        mExplorerAdapter.setItems(list);
    }

    @Override
    public void onDocsNext(@Nullable List<Entity> list) {
        setViewState(false);
        mExplorerAdapter.setItems(list);
    }

    @Override
    public void onDocsAccess(boolean isAccess, @NonNull final String message) {
        setContextDialogExternalLinkEnable(true);
        setContextDialogExternalLinkSwitch(isAccess, message);
    }

    @Override
    public void onDocsBatchOperation() {
        // Stub
    }

    /*
     * Views states
     * */
    @Override
    public void onStateUpdateRoot(boolean isRoot) {
        if (isActivePage()) {
            setViewsModalState(!isRoot);
        }
    }

    @Override
    public void onStateUpdateFilter(boolean isFilter, @Nullable String value) {
        if (isActivePage()) {
            if (isFilter) {
                setViewsModalState(true);
                // Set previous text in search field
                if (mSearchView != null && mSearchView.getQuery().toString().isEmpty()) {
                    mSearchView.setQuery(value, false);
                }

                // Set close button visibility
                if (mSearchCloseButton != null) {
                    mSearchCloseButton.setVisibility(value != null && !value.isEmpty() ?
                            View.VISIBLE : View.INVISIBLE);
                }
            } else {
                if (mSearchView != null) {
                    mSearchView.setQuery("", false);
                    if (!mSearchView.isIconified()) {
                        mSearchView.setIconified(true);
                    }
                }
            }
        }
    }

    @Override
    public void onStateUpdateSelection(boolean isSelection) {
        if (isActivePage()) {
            if (isSelection) {
                setViewsModalState(true);
                setVisibilityActionButton(false);
                mExplorerAdapter.setSelectMode(true);
            } else {
                setVisibilityActionButton(true);
                mExplorerAdapter.setSelectMode(false);
            }

            onCreateOptionsMenu(mMenu, getActivity().getMenuInflater());
        }
    }

    @Override
    public void onStateAdapterRoot(boolean isRoot) {
        mExplorerAdapter.setRoot(isRoot);
    }

    @Override
    public void onStateMenuDefault(@NonNull String sortBy, boolean isAsc) {
        if (mMenu != null && mMenuInflater != null) {
            mMenuInflater.inflate(R.menu.docs_main, mMenu);
            mSortItem = mMenu.findItem(R.id.toolbar_item_sort);
            mMainItem = mMenu.findItem(R.id.toolbar_item_main);
            mSelectItem = mMenu.findItem(R.id.toolbar_main_item_options);
            mOpenItem = mMenu.findItem(R.id.toolbar_item_open);
            mSearchItem = mMenu.findItem(R.id.toolbar_item_search);
            mSearchView = (SearchView) mSearchItem.getActionView();
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setMaxWidth(Integer.MAX_VALUE);
            mSearchView.setIconified(!getPresenter().isFilteringMode());
            mSearchCloseButton = mSearchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            mSearchCloseButton.setVisibility(View.INVISIBLE);
            mSearchCloseButton.setOnClickListener(v -> {
                if (!isSearchViewClear()) {
                    onBackPressed();
                }
            });

            // On search open
            mSearchView.setOnSearchClickListener(v -> getPresenter().setFiltering(true));

            // Init order by
            if (isAsc) {
                mMenu.findItem(R.id.toolbar_sort_item_asc).setChecked(true);
            } else {
                mMenu.findItem(R.id.toolbar_sort_item_desc).setChecked(true);
            }

            // Init sortBy by
            switch (sortBy) {
                case Api.Parameters.VAL_SORT_BY_UPDATED:
                    mMenu.findItem(R.id.toolbar_sort_item_date_update)
                            .setEnabled(false).setChecked(true).setEnabled(true);
                    break;
                case Api.Parameters.VAL_SORT_BY_TITLE:
                    mMenu.findItem(R.id.toolbar_sort_item_title)
                            .setEnabled(false).setChecked(true).setEnabled(true);
                    break;
                case Api.Parameters.VAL_SORT_BY_TYPE:
                    mMenu.findItem(R.id.toolbar_sort_item_type)
                            .setEnabled(false).setChecked(true).setEnabled(true);
                    break;
                case Api.Parameters.VAL_SORT_BY_SIZE:
                    mMenu.findItem(R.id.toolbar_sort_item_size)
                            .setEnabled(false).setChecked(true).setEnabled(true);
                    break;
                case Api.Parameters.VAL_SORT_BY_OWNER:
                    mMenu.findItem(R.id.toolbar_sort_item_owner)
                            .setEnabled(false).setChecked(true).setEnabled(true);
                    break;
            }

            getPresenter().initMenuSearch();
            getPresenter().initMenuState();
        }
    }

    @Override
    public void onStateMenuSelection() {

    }

    @Override
    public void onStateMenuEnabled(boolean isEnabled) {
        setMenuMainEnabled(isEnabled);
        setMenuSearchEnabled(isEnabled);
    }

    @Override
    public void onStateActionButton(boolean isVisible) {
        if (isActivePage()) {
            setVisibilityActionButton(isVisible);
        }
    }

    @Override
    public void onStateEmptyBackStack() {
        // Stub
    }

    /*
     * Changes
     * */
    @Override
    public void onCreateFolder(Folder folder) {
        // Stub
    }

    @Override
    public void onCreateFile(File file) {
        showViewerActivity(file);
    }

    @Override
    public void onDeleteBatch(List<Entity> list) {
        EntityDiffUtilsCallback callback = new EntityDiffUtilsCallback(list, mExplorerAdapter.getItemList());
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        mExplorerAdapter.setData(list);
        result.dispatchUpdatesTo(mExplorerAdapter);
    }

    @Override
    public void onRename(Item item, int position) {
        mExplorerAdapter.setItem(item, position);
    }

    @Override
    public void onBatchMove(@NonNull Explorer explorer) {
        showOperationMoveActivity(explorer);
    }

    @Override
    public void onBatchCopy(@NonNull Explorer explorer) {
        showOperationCopyActivity(explorer);
    }

    protected abstract Boolean isWebDav();

    @Override
    public void onActionBarTitle(String title) {
    }

    @Override
    public void onItemsSelection(String countSelected) {
        onActionBarTitle(countSelected);
        mExplorerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(int position, String countSelected) {
        onActionBarTitle(countSelected);
        mExplorerAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemContext(@NonNull ContextBottomDialog.State state) {
        showContextDialog(state);
    }

    @Override
    public void onActionDialog(boolean isThirdParty, boolean isDocs) {
        mActionBottomDialog.setOnClickListener(this);
        mActionBottomDialog.setThirdParty(isThirdParty);
        mActionBottomDialog.setDocs(isDocs);
        mActionBottomDialog.show(requireFragmentManager(), ActionBottomDialog.TAG);
    }

    @Override
    public void onDownloadActivity() {
        showDownloadFolderActivity();
    }

    @Override
    public void onFileMedia(Explorer explorer, boolean isWebDAv) {
        showMediaActivity(explorer, isWebDAv);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onFileDownloadPermission() {
        if (checkWritePermission()) {
            getPresenter().download();
        }
    }

    @Override
    public void onFileUploadPermission() {
        if (checkReadPermission()) {
            showMultipleFilePickerActivity();
        }
    }

    @Override
    public void onScrollToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onSwipeEnable(boolean isSwipeEnable) {
        mSwipeRefresh.setRefreshing(isSwipeEnable);
    }

    @Override
    public void onPlaceholder(PlaceholderViews.Type type) {
        mPlaceholderViews.setTemplatePlaceholder(type);
    }

    @Override
    public void onDialogClose() {
        if (isActivePage()) {
            hideDialog();
        }
    }

    @Override
    public void onDialogWaiting(@Nullable String title, @Nullable String tag) {
        if (isActivePage()) {
            showWaitingDialog(title, getString(R.string.dialogs_common_cancel_button), tag);
        }
    }

    @Override
    public void onDialogQuestion(String title, String question, String tag) {
        if (isActivePage()) {
            showQuestionDialog(title, question, getString(R.string.dialogs_question_accept_yes),
                    getString(R.string.dialogs_question_accept_no), tag);
        }
    }

    @Override
    public void onDialogProgress(@Nullable String title, boolean isHideButtons, @Nullable String tag) {
        if (isActivePage()) {
            showProgressDialog(title, isHideButtons, getString(R.string.dialogs_common_cancel_button), tag);
        }
    }

    @Override
    public void onDialogProgress(int total, int progress) {
        if (isActivePage()) {
            updateProgressDialog(total, progress);
        }
    }

    @Override
    public void onSnackBar(@NonNull String message) {
        if (isActivePage()) {
            showSnackBar(message);
        }
    }

    @Override
    public void onSnackBarWithAction(@NonNull String message, @NonNull String button, @NonNull View.OnClickListener action) {
        if (isActivePage()) {
            showSnackBarWithAction(message, button, action);
        }
    }

    @Override
    public void onClearMenu() {
        if (mMenu != null && mExplorerAdapter.getItemList().size() == 0) {
            mMenu.findItem(R.id.toolbar_item_empty_trash).setVisible(false);
            mSortItem.setVisible(false);
            mSearchItem.setVisible(false);
        }
    }

    @Override
    public void onUploadFileProgress(int progress, String id) {
        UploadFile uploadFile = mExplorerAdapter.getUploadFileById(id);
        uploadFile.setProgress(progress);
        mExplorerAdapter.updateItem(uploadFile);
    }

    @Override
    public void onDeleteUploadFile(String id) {
        mExplorerAdapter.removeUploadItemById(id);
    }

    @Override
    public void onRemoveUploadHead() {
        mExplorerAdapter.removeHeader(App.getApp().getString(R.string.upload_manager_progress_title));
    }

    @Override
    public void onAddUploadsFile(List<? extends Entity> uploadFiles) {
        onRemoveUploadHead();
        mExplorerAdapter.addItemsAtTop((List<Entity>) uploadFiles);
        mExplorerAdapter.addItemAtTop(new Header(getString(R.string.upload_manager_progress_title)));
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void continueClick(@NonNull String tag, String action) {
        int operationType = Api.Operation.OVERWRITE;
        switch (tag) {
            case MoveCopyDialog.TAG_DUPLICATE:
                operationType = Api.Operation.DUPLICATE;
                break;
            case MoveCopyDialog.TAG_OVERWRITE:
                operationType = Api.Operation.OVERWRITE;
                break;
            case MoveCopyDialog.TAG_SKIP:
                operationType = Api.Operation.SKIP;
                break;
        }
        if (action.equals(MoveCopyDialog.ACTION_COPY)) {
            getPresenter().transfer(operationType, false);
        } else {
            getPresenter().transfer(operationType, true);
        }
    }

    /*
     * Clear SearchView
     * */
    private boolean isSearchViewClear() {
        if (mSearchView != null && mSearchView.getQuery().length() > 0) {
            mSearchView.setQuery("", true);
            return true;
        }

        return false;
    }

    /*
     * On pager scroll callback
     * */
    public void onScrollPage() {
        getPresenter().initViews();
    }

    /*
     * Menu methods
     * */
    protected void setMenuMainEnabled(final boolean isEnabled) {
        if (mSortItem != null) {
            mSortItem.setVisible(isEnabled);
        }

        if (mMainItem != null) {
            mMainItem.setVisible(isEnabled);
        }
    }

    void setMenuSearchEnabled(final boolean isEnabled) {
        if (mSearchItem != null) {
            mSearchItem.setVisible(isEnabled);
        }
    }

    /*
     * Initialisations
     * */
    private void init(@Nullable final Bundle savedInstanceState) {
        setDialogs();

        mExplorerAdapter = new ExplorerAdapter(mTypeFactory);
        mExplorerAdapter.setOnItemContextListener(this);
        mExplorerAdapter.setOnItemClickListener(this);
        mExplorerAdapter.setOnItemLongClickListener(this);

        mRecyclerView.setAdapter(mExplorerAdapter);
        mRecyclerView.setPadding(getResources().getDimensionPixelSize(R.dimen.screen_left_right_padding),
                getResources().getDimensionPixelSize(R.dimen.screen_top_bottom_padding),
                getResources().getDimensionPixelSize(R.dimen.screen_left_right_padding),
                getResources().getDimensionPixelSize(R.dimen.screen_bottom_padding));
    }

    /*
     * Views states for root/empty and etc...
     * */
    private void setViewsModalState(final boolean isModal) {
        if (isActivePage()) {
            setToolbarState(!isModal);
            setScrollViewPager(!isModal);
        }
    }

    private void expandRootViews() {
        if (isActivePage()) {
            setExpandToolbar();
        }
    }

    private void resetIndicators() {
        mSwipeRefresh.post(() -> {
            if (mSwipeRefresh != null) {
                mSwipeRefresh.setRefreshing(false);
            }
        });
        mExplorerAdapter.isLoading(false);
        setContextDialogExternalLinkEnable(true);
    }

    private void setViewState(final boolean isEmpty) {
        resetIndicators();
        if (isEmpty) {
            expandRootViews();
        }
    }

    void showFolderChooser() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(i, BaseActivity.REQUEST_SELECT_FOLDER);
    }

    /*
     * BottomSheetFragmentDialogs context/action
     * */
    private void setDialogs() {
        mContextBottomDialog = (ContextBottomDialog) requireFragmentManager().findFragmentByTag(ContextBottomDialog.TAG);
        if (mContextBottomDialog == null) {
            mContextBottomDialog = ContextBottomDialog.newInstance();
        }

        mActionBottomDialog = (ActionBottomDialog) requireFragmentManager().findFragmentByTag(ActionBottomDialog.TAG);
        if (mActionBottomDialog == null) {
            mActionBottomDialog = ActionBottomDialog.newInstance();
        }

        mMoveCopyDialog = (MoveCopyDialog) requireFragmentManager().findFragmentByTag(MoveCopyDialog.TAG);
        if (mMoveCopyDialog != null) {
            mMoveCopyDialog.setOnClick(this);
        }

        if (getUserVisibleHint()) {
            mContextBottomDialog.setOnClickListener(this);
            mActionBottomDialog.setOnClickListener(this);
        }
    }

    private void showContextDialog(@NonNull ContextBottomDialog.State state) {
        mContextBottomDialog.setState(state);
        mContextBottomDialog.setOnClickListener(this);
        mContextBottomDialog.show(requireFragmentManager(), ContextBottomDialog.TAG);
    }

    private void setContextDialogExternalLinkSwitch(final boolean isCheck, final String message) {
        if (mContextBottomDialog != null) {
            mContextBottomDialog.setItemSharedState(isCheck);
            mContextBottomDialog.showMessage(message);
        }
    }

    void setContextDialogExternalLinkEnable(final boolean isEnable) {
        if (mContextBottomDialog != null) {
            mContextBottomDialog.setItemSharedEnable(isEnable);
        }
    }

    public void showActionDialog() {
        if (!isFastClick()) {
            getPresenter().onActionClick();
        }
    }

    protected void getArgs() {
        if (requireActivity().getIntent() != null) {
            final Intent intent = requireActivity().getIntent();
            final String action = intent.getAction();
            if (intent.getClipData() != null) {
                startUpload(intent, action);
            }
        }
    }

    public void getArgs(Intent intent) {
        final String action = intent.getAction();
        if (intent.getClipData() != null) {
            startUpload(intent, action);
        }
    }

    private void startUpload(Intent intent, String action) {
        if (intent.getClipData() != null) {
            final Uri uri = intent.getClipData().getItemAt(0).getUri();
            if (action != null && action.equals(Intent.ACTION_SEND) && uri != null) {
                if (PermissionUtils.requestReadPermission(this, PERMISSION_READ_UPLOAD)) {
                    getPresenter().uploadToMy(uri);
                    requireActivity().setIntent(null);
                }
            }
        }
    }

    /*
     * Parent ViewPager methods. Check instanceof for trash fragment
     * */
    private void setScrollViewPager(final boolean isScroll) {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MainPagerFragment) {
            ((MainPagerFragment) fragment).setScrollViewPager(isScroll);
        }
    }

    private void setVisibleTabs(final boolean isVisible) {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MainPagerFragment) {
            ((MainPagerFragment) fragment).setVisibleTabs(isVisible);
        }
    }

    protected void setToolbarState(final boolean isVisible) {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MainPagerFragment) {
            ((MainPagerFragment) fragment).setToolbarState(isVisible);
        }
    }

    protected void setExpandToolbar() {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MainPagerFragment) {
            ((MainPagerFragment) fragment).setExpandToolbar();
        }
    }

    protected void setVisibilityActionButton(final boolean isShow) {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MainPagerFragment) {
            ((MainPagerFragment) fragment).setVisibilityActionButton(isShow);
        }
    }

    protected boolean isActivePage() {
        final Fragment fragment = getParentFragment();
        if (fragment == null) {
            return true;
        } else if (fragment instanceof MainPagerFragment) {
            return ((MainPagerFragment) fragment).isActivePage(this);
        } else {
            return true;
        }

    }

    protected void setAccountEnable(boolean isEnable) {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MainPagerFragment) {
            ((MainPagerFragment) fragment).setAccountEnable(isEnable);
        }
    }

    protected void showEditors(Uri uri, @NonNull EditorsType type) {
        try {
            final Intent intent = new Intent();
            intent.setData(uri);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            switch (type) {
                case DOCS:
                    intent.setClassName(requireContext(), "lib.editors.gdocs.ui.activities.DocsActivity");
                    startActivityForResult(intent, REQUEST_DOCS);
                    break;
                case CELLS:
                    intent.setClassName(requireContext(), "lib.editors.gcells.ui.activities.CellsActivity");
                    startActivityForResult(intent, REQUEST_SHEETS);
                    break;
                case PRESENTATION:
                    intent.setClassName(requireContext(), "lib.editors.gslides.ui.activities.SlidesActivity");
                    startActivityForResult(intent, REQUEST_PRESENTATION);
                    break;
                case PDF:
                    intent.setClassName(requireContext(), "lib.editors.gbase.ui.activities.PdfActivity");
                    startActivityForResult(intent, REQUEST_PDF);
                    break;
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showToast("Not found");
        }

    }


    private void removeCommonDialog() {
        final Fragment fragment = requireFragmentManager().findFragmentByTag(CommonDialog.TAG);
        if (fragment != null) {
            requireFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    abstract protected DocsBasePresenter getPresenter();

}
