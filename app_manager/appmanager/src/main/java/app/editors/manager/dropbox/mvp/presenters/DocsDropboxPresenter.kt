package app.editors.manager.dropbox.mvp.presenters

import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.dropbox.managers.providers.DropboxFileProvider
import app.editors.manager.dropbox.mvp.views.DocsDropboxView
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.ui.views.custom.PlaceholderViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils

class DocsDropboxPresenter: DocsBasePresenter<DocsDropboxView>() {

    init {
        App.getApp().appComponent.inject(this)
        mModelExplorerStack = ModelExplorerStack()
        mFilteringValue = ""
        mPlaceholderType = PlaceholderViews.Type.NONE
        mIsContextClick = false
        mIsFilteringMode = false
        mIsSelectionMode = false
        mIsFoldersMode = false
        //uploadReceiver = UploadReceiver()
        //downloadReceiver = DownloadReceiver()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        //uploadReceiver.setOnUploadListener(this)
        //LocalBroadcastManager.getInstance(mContext).registerReceiver(uploadReceiver, uploadReceiver.filter)
        //downloadReceiver.setOnDownloadListener(this)
        //LocalBroadcastManager.getInstance(mContext).registerReceiver(downloadReceiver, downloadReceiver.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        //LocalBroadcastManager.getInstance(mContext).unregisterReceiver(uploadReceiver)
        //LocalBroadcastManager.getInstance(mContext).unregisterReceiver(downloadReceiver)
    }

    fun getProvider() {
        mFileProvider?.let {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                    withContext(Dispatchers.Main) {
                        getItemsById(null)
                    }

                }
            }
        } ?: run {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
                    AccountUtils.getAccount(mContext, cloudAccount.getAccountName())?.let {
                        mFileProvider = DropboxFileProvider()
                        withContext(Dispatchers.Main) {
                            getItemsById(null)
                        }
                    }
                } ?: run {
                    throw Error("Not accounts")
                }
            }
        }
    }


    override fun getNextList() {
        TODO("Not yet implemented")
    }

    override fun createDocs(title: String) {
        TODO("Not yet implemented")
    }

    override fun getFileInfo() {
        TODO("Not yet implemented")
    }

    override fun addRecent(file: CloudFile?) {
        TODO("Not yet implemented")
    }

    override fun updateViewsState() {
        if (mIsSelectionMode) {
            viewState.onStateUpdateSelection(true)
            viewState.onActionBarTitle(mModelExplorerStack.countSelectedItems.toString())
            viewState.onStateAdapterRoot(mModelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (mIsFilteringMode) {
            viewState.onActionBarTitle(mContext.getString(R.string.toolbar_menu_search_result))
            viewState.onStateUpdateFilter(true, mFilteringValue)
            viewState.onStateAdapterRoot(mModelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (!mModelExplorerStack.isRoot) {
            viewState.onStateAdapterRoot(false)
            viewState.onStateUpdateRoot(false)
            viewState.onStateActionButton(true)
            viewState.onActionBarTitle(if(currentTitle.isEmpty()) { mItemClicked?.title } else { currentTitle } )
        } else {
            if (mIsFoldersMode) {
                viewState.onActionBarTitle(mContext.getString(R.string.operation_title))
                viewState.onStateActionButton(false)
            } else {
                viewState.onActionBarTitle("")
                viewState.onStateActionButton(true)
            }
            viewState.onStateAdapterRoot(true)
            viewState.onStateUpdateRoot(true)
        }
    }

    override fun onContextClick(item: Item?, position: Int, isTrash: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onActionClick() {
        TODO("Not yet implemented")
    }
}