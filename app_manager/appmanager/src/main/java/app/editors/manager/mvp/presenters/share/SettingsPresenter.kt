package app.editors.manager.mvp.presenters.share

import android.content.Intent
import app.documents.core.network.ApiContract
import app.documents.core.network.models.share.Share
import app.documents.core.network.models.share.request.RequestExternal
import app.documents.core.network.models.share.request.RequestShare
import app.documents.core.network.models.share.request.RequestShareItem
import app.documents.core.share.ShareService
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.getShareApi
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.share.SettingsView
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.InjectViewState
import retrofit2.HttpException

@InjectViewState
class SettingsPresenter : BasePresenter<SettingsView>() {

    companion object {
        val TAG: String = SettingsPresenter::class.java.simpleName
        private const val TAG_FOLDER_PATH = "/products/files/#"
    }

    private var accessType: String? = null

    /*
     * Getters/Setters
     * */
    var item: Item? = null
    var externalLink: String? = null

    private var shareItem: ShareUi? = null
    var sharePosition = 0
        private set

    private var isAccessDenied: Boolean = false
    private var isRemove = false
    private var isShare = false
    private var isPopupShow = false
    private var sharedService: ShareService
    private val commonList: ArrayList<ViewType> = arrayListOf()

    private var disposable: Disposable? = null

    init {
        App.getApp().appComponent.inject(this)
        sharedService = getApi()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun getApi(): ShareService = context.getShareApi()

    /*
     * Requests
     * */
    private fun getShareFolder(id: String) {
        disposable = sharedService.getShareFolder(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                getShareList(it.response)
            }, { error ->
                fetchError(error)
            })
    }

    private fun getShareFile(id: String) {
        disposable = sharedService.getShareFile(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                getShareList(it.response)
            }, { error ->
                fetchError(error)
            })
    }

    private fun setShareFile(
        id: String, isNotify: Boolean, message: String?,
        vararg shareList: Share?
    ) {
        val requestShare = getRequestShare(isNotify, message, *shareList)
        disposable = sharedService.setFileAccess(id, requestShare)
            .subscribeOn(Schedulers.io())
            .map { it.response }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                getShareList(it)
            }, { fetchError(it) })
    }

    private fun setShareFolder(
        id: String, isNotify: Boolean,
        message: String?, vararg shareList: Share?
    ) {
        val requestShare = getRequestShare(isNotify, message, *shareList)
        disposable = sharedService.setFolderAccess(id, requestShare)
            .subscribeOn(Schedulers.io())
            .map { it.response }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                getShareList(it)
            }, { fetchError(it) })
    }

    private fun isShared(accessCode: Int): Boolean {
        val isShared = accessCode != ApiContract.ShareCode.RESTRICT && accessCode != ApiContract.ShareCode.NONE
        viewState.onResultState(isShared)
        return isShared
    }

    private fun getRequestShare(
        isNotify: Boolean, message: String?,
        vararg shareList: Share?
    ): RequestShare {
        val shareRights: MutableList<RequestShareItem> = ArrayList()

        shareList.forEach { share ->
            val idItem = share?.sharedTo?.id
            val accessCode = share?.access
            shareRights.add(RequestShareItem(shareTo = idItem ?: "", access = accessCode.toString()))
        }
        return RequestShare(share = shareRights, isNotify = isNotify, sharingMessage = message ?: "")
    }

    val shared: Unit
        get() {
            if (item is CloudFolder) {
                getShareFolder((item as CloudFolder).id)
            } else if (item is CloudFile) {
                getShareFile((item as CloudFile).id)
            }
        }
    val internalLink: Unit
        get() {
            if (item is CloudFolder) {
                val internalLink = networkSettings.getBaseUrl() + TAG_FOLDER_PATH + (item as CloudFolder).id
                viewState.onInternalLink(internalLink)
            } else if (item is CloudFile) {
                viewState.onInternalLink((item as CloudFile).webUrl)
            }
        }

    fun getExternalLink(share: String?) {
        accessType = share
        disposable = sharedService
            .getExternalLink(item?.id ?: "", RequestExternal(share = accessType ?: ""))
            .subscribeOn(Schedulers.io())
            .map { it.body()?.response }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                externalLink = it
                val code = ApiContract.ShareType.getCode(accessType)
                item?.access = code
                item?.shared = isShared(code)
                viewState.onExternalAccess(code, true)
            }
    }

    fun setItemAccess(accessCode: Int) {
        if (item != null) {
            if (accessCode == ApiContract.ShareCode.NONE) {
                shareItem?.let {
                    viewState.onRemove(ShareUi(it.access, it.sharedTo, it.isLocked, it.isOwner, it.sharedTo.isVisitor), sharePosition)
                }

                isRemove = true
            }
            if (item is CloudFolder) {
                shareItem?.let {
                    setShareFolder(
                        (item as CloudFolder).id,
                        false,
                        null,
                        Share(accessCode, it.sharedTo, it.isLocked, it.isOwner)
                    )
                }
            } else {
                shareItem?.let {
                    setShareFile(item?.id ?: "", false, null, Share(accessCode, it.sharedTo, it.isLocked, it.isOwner))
                }

            }
        }
    }

    /*
     * Update states
     * */
    fun updateSharedListState() {
        viewState.onGetShare(commonList, item!!.access)
        loadAvatars(commonList)
        if (isPopupShow) {
            isPopupShow = false
            shareItem?.isGuest?.let { viewState.onShowPopup(sharePosition, it) }
        }
    }

    fun updateSharedExternalState(isMessage: Boolean) {
        viewState.onExternalAccess(item!!.access, isMessage)
    }

    fun updateActionButtonState() {
        viewState.onActionButtonState(!isAccessDenied)
    }

    fun updatePlaceholderState() {
        when {
            isAccessDenied -> {
                viewState.onPlaceholderState(PlaceholderViews.Type.ACCESS)
            }
            commonList.isEmpty() -> {
                viewState.onPlaceholderState(PlaceholderViews.Type.SHARE)
            }
            else -> {
                viewState.onPlaceholderState(PlaceholderViews.Type.NONE)
            }
        }
    }

    fun updateHeaderState() {
        viewState.onItemType(item is CloudFolder)
    }

    fun setShared(share: ShareUi?, position: Int) {
        shareItem = share
        sharePosition = position
        isShare = true
    }

    fun addShareItems() {
        viewState.onAddShare(item!!)
    }

    fun sendLink(externalLink: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_TEXT, externalLink)
        viewState.onSendLink(intent)
    }

    fun setIsPopupShow(isPopupShow: Boolean) {
        this.isPopupShow = isPopupShow
    } /*
     * Callbacks for response
     * */

    private fun getShareList(shareList: List<Share>) {
        isAccessDenied = false
        val userList = shareList.filter {
            it.sharedTo.userName.isNotEmpty() && !it.isOwner
        }.map { ShareUi(it.access, it.sharedTo, it.isLocked, it.isLocked, it.sharedTo.isVisitor) }
        val groupList = shareList.filter {
            it.sharedTo.name.isNotEmpty()
        }.map { ShareUi(it.access, it.sharedTo, it.isLocked, it.isLocked, it.sharedTo.isVisitor) }

        shareList.find { it.sharedTo.shareLink.isNotEmpty() }?.let {
            item?.access = it.access
            item?.shared = isShared(it.access)
            externalLink = it.sharedTo.shareLink
        }

        commonList.clear()

        if (userList.isNotEmpty()) {
            commonList.add(ShareHeaderUi(context.getString(R.string.share_goal_user)))
            commonList.addAll(userList)
        }

        if (groupList.isNotEmpty()) {
            commonList.add(ShareHeaderUi(context.getString(R.string.share_goal_group)))
            commonList.addAll(groupList)
        }

        if (isRemove) {
            if (commonList.isEmpty()) {
                viewState.onPlaceholderState(PlaceholderViews.Type.SHARE)
            }
            isRemove = false
            isShare = false
            return
        }

        viewState.onActionButtonState(true)
        if (isShare && commonList.isNotEmpty()) {
            viewState.onGetShareItem(commonList[sharePosition], sharePosition, item?.access ?: -1)
            isShare = false
        } else {
            viewState.onGetShare(commonList, item?.access ?: -1)
        }

        if (commonList.isEmpty()) {
            viewState.onPlaceholderState(PlaceholderViews.Type.SHARE)
        } else {
            viewState.onPlaceholderState(PlaceholderViews.Type.NONE)
        }

        loadAvatars(commonList)
    }

    private fun loadAvatars(commonList: ArrayList<ViewType>) {
        disposable = Observable.fromIterable(commonList).subscribeOn(Schedulers.io())
            .filter { it is ShareUi && it.sharedTo.avatar.isNotEmpty() }
            .map { user ->
                user.also {
                    (it as ShareUi).avatar = GlideUtils
                        .loadAvatar((user as ShareUi).sharedTo.avatarMedium)
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewState.onUpdateAvatar(it as ShareUi)
            }, { error ->
                fetchError(error)
            })
    }

    override fun fetchError(throwable: Throwable) {
        if (throwable is HttpException && throwable.response()?.code() == ApiContract.HttpCodes.CLIENT_FORBIDDEN) {
            isAccessDenied = true
            viewState.onPlaceholderState(PlaceholderViews.Type.ACCESS)
            viewState.onPopupState(false)
            viewState.onButtonState(false)
            viewState.onActionButtonState(false)
        } else {
            super.fetchError(throwable)
        }
    }

}