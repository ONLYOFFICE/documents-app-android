package app.editors.manager.ui.fragments.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.presenters.main.DocsBasePresenter;
import app.editors.manager.mvp.presenters.main.DocsOnDevicePresenter;
import app.editors.manager.mvp.views.main.DocsOnDeviceView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.MediaActivity;
import app.editors.manager.ui.dialogs.ActionBottomDialog;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import lib.toolkit.base.managers.tools.LocalContentTools;
import lib.toolkit.base.managers.utils.ActivitiesUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.activities.base.BaseActivity;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

public class DocsOnDeviceFragment extends DocsBaseFragment implements DocsOnDeviceView {

    public static final String TAG = DocsOnDeviceFragment.class.getSimpleName();

    private static final String TAG_STORAGE_ACCESS = "TAG_STORAGE_ACCESS";

    public static DocsOnDeviceFragment newInstance() {
        return new DocsOnDeviceFragment();
    }

    @InjectPresenter
    DocsOnDevicePresenter mOnDevicePresenter;

    private MainActivity mMainActivity;
    private Operation mOperation;

    enum Operation {
        COPY, MOVE
    }

    private PreferenceTool mPreferenceTool;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMainActivity = (MainActivity) context;
            mPreferenceTool = App.getApp().getAppComponent().getPreference();
        } catch (ClassCastException e) {
            throw new RuntimeException(MainPagerFragment.class.getSimpleName() + " - must implement - " +
                    MainActivity.class.getSimpleName());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BaseActivity.REQUEST_ACTIVITY_CAMERA: {
                    mOnDevicePresenter.refresh();
                    break;
                }
                case BaseActivity.REQUEST_SELECT_FOLDER: {
                    if (mOperation != null && data != null && data.getData() != null) {
                        if (mOperation == Operation.MOVE) {
                            mOnDevicePresenter.moveFile(data.getData(), false);
                        } else if (mOperation == Operation.COPY) {
                            mOnDevicePresenter.moveFile(data.getData(), true);
                        }
                    }
                    break;
                }
                case REQUEST_OPEN_FILE: {
                    if (data != null && data.getData() != null) {
                        mOnDevicePresenter.openFromChooser(data.getData());
                    }
                    break;
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case BaseActivity.REQUEST_ACTIVITY_CAMERA:
                    mOnDevicePresenter.deletePhoto();
                    break;
                case REQUEST_STORAGE_ACCESS:
                    mPreferenceTool.setShowStorageAccess(false);
                    mOnDevicePresenter.recreateStack();
                    mOnDevicePresenter.getItemsById(LocalContentTools.Companion.getDir(requireContext()));
                    break;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoto();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkStorage();
        init();
    }

    @Override
    public void onStateMenuDefault(@NonNull String sortBy, boolean isAsc) {
        super.onStateMenuDefault(sortBy, isAsc);
        if (mMenu != null) {
            mOpenItem.setVisible(true);
            mMenu.findItem(R.id.toolbar_sort_item_owner).setVisible(false);
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
                mOnDevicePresenter.sortBy(Api.Parameters.VAL_SORT_BY_UPDATED);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_title:
                mOnDevicePresenter.sortBy(Api.Parameters.VAL_SORT_BY_TITLE);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_type:
                mOnDevicePresenter.sortBy(Api.Parameters.VAL_SORT_BY_TYPE);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_size:
                mOnDevicePresenter.sortBy(Api.Parameters.VAL_SORT_BY_SIZE);
                item.setChecked(true);
                break;

            // Sort order
            case R.id.toolbar_sort_item_asc:
                mOnDevicePresenter.orderBy(Api.Parameters.VAL_SORT_ORDER_ASC);
                item.setChecked(true);
                break;
            case R.id.toolbar_sort_item_desc:
                mOnDevicePresenter.orderBy(Api.Parameters.VAL_SORT_ORDER_DESC);
                item.setChecked(true);
                break;
            case R.id.toolbar_main_item_select:
                mOnDevicePresenter.setSelection(true);
                break;
            case R.id.toolbar_main_item_select_all:
                mOnDevicePresenter.setSelectionAll();
                break;

            // Selection menu
            case R.id.toolbar_selection_delete:
                mOnDevicePresenter.delete();
                break;
            case R.id.toolbar_selection_move:
                mOperation = Operation.MOVE;
                mOnDevicePresenter.checkSelectedFiles();
                break;
            case R.id.toolbar_selection_copy:
                mOperation = Operation.COPY;
                mOnDevicePresenter.checkSelectedFiles();
                break;
            case R.id.toolbar_selection_deselect:
                mOnDevicePresenter.deselectAll();
                break;
            case R.id.toolbar_selection_select_all:
                mOnDevicePresenter.selectAll();
                break;
            case R.id.toolbar_item_open:
                showSingleFragmentFilePicker();
                break;
        }
        return true;
    }

    @Override
    protected boolean onSwipeRefresh() {
        if (!super.onSwipeRefresh()) {
            mOnDevicePresenter.getItemsById(LocalContentTools.Companion.getDir(requireContext()));
            return true;
        }

        return false;
    }

    @Override
    public void onStateUpdateRoot(boolean isRoot) {
        mMainActivity.setAppBarStates(false);
        mMainActivity.setNavigationButton(isRoot);
        mMainActivity.setOffToolbarAccount();
    }

    @Override
    public void onStateMenuSelection() {
        if (mMenu != null && mMenuInflater != null && getContext() != null) {
            mMenuInflater.inflate(R.menu.docs_select, mMenu);
            mDeleteItem = mMenu.findItem(R.id.toolbar_selection_delete).setVisible(true);
            mMoveItem = mMenu.findItem(R.id.toolbar_selection_move).setVisible(true);
            mCopyItem = mMenu.findItem(R.id.toolbar_selection_copy).setVisible(true);
            mDownloadItem = mMenu.findItem(R.id.toolbar_selection_download).setVisible(false);
            UiUtils.setMenuItemTint(getContext(), mDeleteItem, R.color.colorWhite);
            mMainActivity.setNavigationButton(false);
        }
    }

    @Override
    public void onStateEmptyBackStack() {
        super.onStateEmptyBackStack();
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(true);
        }
        mOnDevicePresenter.getItemsById(LocalContentTools.Companion.getDir(requireContext()));
    }

    @Override
    public void onStateUpdateFilter(boolean isFilter, @Nullable String value) {
        super.onStateUpdateFilter(isFilter, value);
        if (isFilter) {
            mMainActivity.setNavigationButton(false);
        }
    }

    @Override
    protected void onListEnd() {
        // Stub to local
    }

    @Override
    public void onActionBarTitle(String title) {
        setActionBarTitle(title);
    }

    @Override
    public void onActionButtonClick(ActionBottomDialog.Buttons buttons) {
        super.onActionButtonClick(buttons);
        if (buttons == ActionBottomDialog.Buttons.PHOTO) {
            if (checkCameraPermission()) {
                makePhoto();
            }
        }
    }

    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        if (tag != null) {
            if (value != null) {
                value = value.trim();
            }
            switch (tag) {
                case TAG_STORAGE_ACCESS:
                    requestManage();
                    break;
                case DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_SELECTED:
                    mOnDevicePresenter.deleteItems();
                    break;
                case DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME:
                    if (value != null) {
                        mOnDevicePresenter.rename(value);
                    }
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_SHEET:
                    mOnDevicePresenter.createDocs(value + "." + Api.Extension.XLSX.toLowerCase());
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_PRESENTATION:
                    mOnDevicePresenter.createDocs(value + "." + Api.Extension.PPTX.toLowerCase());
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_DOC:
                    mOnDevicePresenter.createDocs(value + "." + Api.Extension.DOCX.toLowerCase());
                    break;
                case DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER:
                    if (value != null) {
                        mOnDevicePresenter.createFolder(value);
                    }
                    break;
                case DocsBasePresenter.TAG_DIALOG_DELETE_CONTEXT:
                    mOnDevicePresenter.deleteFile();
                    break;
            }
        }
        hideDialog();
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        if (tag != null && tag.equals(TAG_STORAGE_ACCESS)) {
            mPreferenceTool.setShowStorageAccess(false);
            mOnDevicePresenter.recreateStack();
            mOnDevicePresenter.getItemsById(LocalContentTools.Companion.getDir(requireContext()));
        }
    }

    @Override
    public void onContextButtonClick(ContextBottomDialog.Buttons buttons) {
        switch (buttons) {
            case DOWNLOAD:
                mOnDevicePresenter.upload();
                break;
            case DELETE:
                mOnDevicePresenter.showDeleteDialog();
                break;
            case COPY:
                mOperation = Operation.COPY;
                showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER);
                break;
            case MOVE:
                mOperation = Operation.MOVE;
                showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER);
                break;
            case RENAME:
                showEditDialogRename(getString(R.string.dialogs_edit_rename_title), mOnDevicePresenter.getItemTitle(),
                        getString(R.string.dialogs_edit_hint), DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                        getString(R.string.dialogs_edit_accept_rename), getString(R.string.dialogs_common_cancel_button));
                break;
        }
        mContextBottomDialog.dismiss();
    }

    @Override
    protected boolean isActivePage() {
        return isAdded();
    }

    @Override
    public void onActionDialog() {
        mActionBottomDialog.setOnClickListener(this);
        mActionBottomDialog.setLocal(true);
        mActionBottomDialog.show(requireFragmentManager(), ActionBottomDialog.TAG);
    }

    @Override
    public void onRemoveItem(Item item) {
        mExplorerAdapter.removeItem(item);
        mExplorerAdapter.checkHeaders();
    }

    @Override
    public void onRemoveItems(List<Item> items) {
        mExplorerAdapter.removeItems(new ArrayList<>(items));
        mExplorerAdapter.checkHeaders();
    }

    @Override
    public void onShowFolderChooser() {
        showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER);
    }

    @Override
    public void onShowCamera(Uri photoUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        this.startActivityForResult(intent, BaseActivity.REQUEST_ACTIVITY_CAMERA);
    }

    @Override
    public void onShowDocs(Uri uri) {
        showEditors(uri, EditorsType.DOCS);
    }

    @Override
    public void onShowCells(Uri uri) {
        showEditors(uri, EditorsType.CELLS);
    }

    @Override
    public void onShowSlides(Uri uri) {
        showEditors(uri, EditorsType.PRESENTATION);
    }

    @Override
    public void onShowPdf(Uri uri) {
        showEditors(uri, EditorsType.PDF);
    }

    @Override
    public void onOpenMedia(Explorer mediaFiles) {
        MediaActivity.show(this, mediaFiles, false);
    }

    @Override
    protected Boolean isWebDav() {
        return false;
    }

    @Override
    protected DocsBasePresenter getPresenter() {
        return mOnDevicePresenter;
    }

    @Override
    protected void setVisibilityActionButton(final boolean isShow) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setActionButtonShow(isShow);
        }
    }

    private void init() {
        mOnDevicePresenter.checkBackStack();
    }

    private void makePhoto() {
        mOnDevicePresenter.createPhoto();
    }

    private void showSingleFragmentFilePicker() {
        try {
            ActivitiesUtils.showSingleFilePicker(this, REQUEST_OPEN_FILE);
        } catch (ActivityNotFoundException e) {
            onError(e.getMessage());
        }
    }

    private void checkStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !Environment.isExternalStorageManager() &&
                App.getApp().getAppComponent().getPreference().isShowStorageAccess()) {
            showQuestionDialog(getString(R.string.app_manage_files_title),
                    getString(R.string.app_manage_files_description),
                    getString(R.string.dialogs_common_ok_button),
                    getString(R.string.dialogs_common_cancel_button),
                    TAG_STORAGE_ACCESS);
        }
    }

    private void requestManage() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + requireContext().getPackageName()));
                startActivityForResult(intent, REQUEST_STORAGE_ACCESS);
            } catch (ActivityNotFoundException e) {
                showSnackBar("Not found");
                mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.ACCESS);
            }
        }
    }
}
