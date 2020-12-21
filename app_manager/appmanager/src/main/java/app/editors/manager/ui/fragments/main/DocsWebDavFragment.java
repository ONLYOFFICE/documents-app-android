package app.editors.manager.ui.fragments.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.main.DocsBasePresenter;
import app.editors.manager.mvp.presenters.main.DocsWebDavPresenter;
import app.editors.manager.mvp.views.main.DocsWebDavView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.dialogs.ActionBottomDialog;
import lib.toolkit.base.managers.utils.TimeUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.activities.base.BaseActivity;
import moxy.presenter.InjectPresenter;

public class DocsWebDavFragment extends DocsBaseFragment implements DocsWebDavView {

    public static final String TAG = DocsWebDavFragment.class.getSimpleName();

    protected static final String KEY_PROVIDER = "KEY_PROVIDER";

    protected WebDavApi.Providers mProvider;

    @InjectPresenter
    public DocsWebDavPresenter mWebDavPresenter;

    public static DocsWebDavFragment newInstance(WebDavApi.Providers provider) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_PROVIDER, provider);
        DocsWebDavFragment fragment = new DocsWebDavFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(KEY_PROVIDER)) {
            mProvider = (WebDavApi.Providers) getArguments().getSerializable(KEY_PROVIDER);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            if (isActivePage() && (requestCode == BaseActivity.REQUEST_ACTIVITY_MEDIA ||
                    requestCode == REQUEST_PDF)) {
                mWebDavPresenter.deleteTempFile();
            }
        } else if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BaseActivity.REQUEST_ACTIVITY_OPERATION: {
                    mWebDavPresenter.checkBackStack();
                    break;
                }
                case BaseActivity.REQUEST_ACTIVITY_FILE_PICKER: {
                    mWebDavPresenter.upload(data.getData(), data.getClipData());
                    break;
                }
                case BaseActivity.REQUEST_ACTIVITY_CAMERA: {
                    mWebDavPresenter.upload(mCameraUri, null);
                    break;
                }
                case REQUEST_PRESENTATION:
                case REQUEST_PDF:
                case REQUEST_DOCS:
                case REQUEST_SHEETS: {
                    if (data.getData() != null) {
                        if (data.getBooleanExtra("EXTRA_IS_MODIFIED", false)) {
                            mWebDavPresenter.upload(data.getData(), null);
                        }
                    }
                    break;
                }
            }
        }

    }

    @Override
    public void onActionButtonClick(ActionBottomDialog.Buttons buttons) {
        super.onActionButtonClick(buttons);
        if (buttons == ActionBottomDialog.Buttons.PHOTO) {
            if (checkCameraPermission()) {
                showCameraActivity(TimeUtils.getFileTimeStamp());
            }
        }
    }

    @Override
    protected boolean onSwipeRefresh() {
        if (!super.onSwipeRefresh() && mWebDavPresenter != null) {
            loadFiles();
            return true;
        }
        return false;
    }

    @Override
    public void onStateEmptyBackStack() {
        super.onStateEmptyBackStack();
        loadFiles();
        mSwipeRefresh.setRefreshing(true);
    }

    private void loadFiles() {
        AccountsSqlData sqlData = App.getApp().getAppComponent().getAccountsSql().getAccountOnline();
        switch (mProvider) {
            case NextCloud:
            case OwnCloud:
            case WebDav:
                mWebDavPresenter.getItemsById(sqlData.getWebDavPath());
                break;
            case Yandex:
                mWebDavPresenter.getItemsById(WebDavApi.Providers.Yandex.getPath());
                break;
        }
    }

    private void init() {
        mWebDavPresenter.checkBackStack();
        getArgs();
    }

    @Override
    public void onStateMenuDefault(@NonNull String sortBy, boolean isAsc) {
        super.onStateMenuDefault(sortBy, isAsc);
        mSortItem.setVisible(false);
    }

    @Override
    public void onStateMenuSelection() {
        if (mMenu != null && mMenuInflater != null && getContext() != null) {
            mMenuInflater.inflate(R.menu.docs_select, mMenu);
            mDeleteItem = mMenu.findItem(R.id.toolbar_selection_delete).setVisible(true);
            mMoveItem = mMenu.findItem(R.id.toolbar_selection_move).setVisible(true);
            mCopyItem = mMenu.findItem(R.id.toolbar_selection_copy).setVisible(true);
            mDownloadItem = mMenu.findItem(R.id.toolbar_selection_download).setVisible(true);
            UiUtils.setMenuItemTint(getContext(), mDeleteItem, R.color.colorWhite);
            ((MainActivity) requireActivity()).setToolbarAccount(false);
        }
    }

    @Override
    protected void onListEnd() {

    }

    @Override
    public void onActionBarTitle(String title) {
        setActionBarTitle(title);
    }

    @Override
    public void onActionDialog() {
        mActionBottomDialog.setLocal(true);
        mActionBottomDialog.setWebDav(true);
        mActionBottomDialog.setOnClickListener(this);
        mActionBottomDialog.show(requireFragmentManager(), ActionBottomDialog.TAG);
    }

    @Override
    protected void setToolbarState(final boolean isVisible) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setAppBarStates(false);
            ((MainActivity) requireActivity()).setNavigationButton(isVisible);
            ((MainActivity) requireActivity()).setToolbarAccount(isVisible);
        }
    }

    @Override
    protected void setExpandToolbar() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).expandToolBar();
        }
    }

    @Override
    protected void setVisibilityActionButton(final boolean isShow) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setActionButtonShow(isShow);
        }

    }

    @Override
    protected boolean isActivePage() {
        return true;
    }

    @Override
    protected DocsBasePresenter getPresenter() {
        return mWebDavPresenter;
    }

    @Override
    protected Boolean isWebDav() {
        return true;
    }
}
