package app.editors.manager.mvp.presenters.share

import android.content.Intent
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.request
import app.documents.core.network.common.requestIterable
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.request.RequestExternal
import app.documents.core.network.share.models.request.RequestExternalAccess
import app.documents.core.network.share.models.request.RequestShare
import app.documents.core.network.share.models.request.RequestShareItem
import app.documents.core.repositories.ShareRepository
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.getShareRepository
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.share.SettingsView
import app.editors.manager.ui.views.custom.PlaceholderViews
import kotlinx.coroutines.*
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.InjectViewState
import moxy.presenterScope
import retrofit2.HttpException

@InjectViewState
class SettingsPresenter(
    val item: Item
) : BasePresenter<SettingsView>() {

    companion object {
        val TAG: String = SettingsPresenter::class.java.simpleName
        private const val TAG_FOLDER_PATH = "/products/files/#"
    }

    /*
     * Getters/Setters
     * */
    var externalLink: String? = null
    val isPersonalAccount: Boolean
        get() = App.getApp().appComponent.accountOnline?.isPersonal() ?: false

    private var shareItem: ShareUi? = null
    var sharePosition = 0
        private set

    private var isAccessDenied: Boolean = false
    private var isRemove = false
    private var isShare = false
    private var isPopupShow = false
    private val commonList: ArrayList<ViewType> = arrayListOf()
    private val shareRepository: ShareRepository

    init {
        App.getApp().appComponent.inject(this)
        shareRepository = context.getShareRepository()
    }

    /*
     * Requests
     * */
    private suspend fun getShareFolder(id: String) {
        request(
            func = { shareRepository.getShareFolder(id) },
            map = { it.response },
            onSuccess = ::getShareList,
            onError = ::fetchError
        )
    }

    private suspend fun getShareFile(id: String) {
        request(
            func = { shareRepository.getShareFile(id) },
            onSuccess = { response ->
                getShareList(response.response)
                if (response.response.isNotEmpty()) {
                    externalLink = response.response[0].sharedTo.shareLink
                }
            }, onError = ::fetchError
        )
    }

    private suspend fun setShareFile(
        id: String,
        isNotify: Boolean,
        message: String?,
        vararg shareList: Share?
    ) {
        request(
            func = { shareRepository.setFileAccess(id, getRequestShare(isNotify, message, *shareList)) },
            map = { it.response },
            onSuccess = ::getShareList,
            onError = ::fetchError
        )
    }

    private suspend fun setShareFolder(
        id: String,
        isNotify: Boolean,
        message: String?,
        vararg shareList: Share?
    ) {
        request(
            func = { shareRepository.setFolderAccess(id, getRequestShare(isNotify, message, *shareList)) },
            map = { it.response },
            onSuccess = ::getShareList, onError = ::fetchError
        )
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

    fun getShared() {
        presenterScope.launch {
            if (item is CloudFolder) {
                getShareFolder(item.id)
            } else if (item is CloudFile) {
                getShareFile(item.id)
            }
        }
    }

    fun getInternalLink() {
        if (item is CloudFolder) {
            val internalLink = networkSettings.getBaseUrl() + TAG_FOLDER_PATH + item.id
            viewState.onInternalLink(internalLink)
        } else if (item is CloudFile) {
            viewState.onInternalLink(item.webUrl)
        }
    }

    fun getExternalLink(share: String?) {
        presenterScope.launch {
            val code = ApiContract.ShareType.getCode(share)
            if (!isPersonalAccount) {
                request(
                    func = {
                        val request = RequestExternal(share = share ?: "")
                        shareRepository.getExternalLink(item.id ?: "", request)
                    }, onSuccess = { response ->
                        externalLink = response.response
                        item.access = code.toString()
                        item.shared = isShared(code)
                        viewState.onExternalAccess(code, true)
                    }, onError = ::fetchError
                )
            } else {
                request(
                    func = {
                        val request = RequestExternalAccess(code)
                        shareRepository.setExternalLinkAccess(item.id ?: "", request)
                    }, onSuccess = {
                        item.access = code.toString()
                        item.shared = isShared(code)
                        viewState.onExternalAccess(code, true)
                    }, onError = ::fetchError
                )
            }
        }
    }

    fun setItemAccess(accessCode: Int) {
        if (accessCode == ApiContract.ShareCode.NONE) {
            shareItem?.let {
                viewState.onRemove(
                    ShareUi(
                        access = it.access,
                        sharedTo = it.sharedTo,
                        isLocked = it.isLocked,
                        isOwner = it.isOwner,
                        isGuest = it.sharedTo.isVisitor,
                        isRoom = item is CloudFolder && item.isRoom
                    ), sharePosition
                )
            }

            isRemove = true
        }
        presenterScope.launch {
            if (item is CloudFolder) {
                shareItem?.let { shareUi ->
                    setShareFolder(
                        id = item.id,
                        isNotify = false,
                        message = null,
                        Share(
                            access = accessCode.toString(),
                            sharedTo = shareUi.sharedTo,
                            isLocked = shareUi.isLocked,
                            isOwner = shareUi.isOwner
                        )
                    )
                }
            } else {
                shareItem?.let {
                    setShareFile(
                        id = item.id,
                        isNotify = false,
                        message = null,
                        Share(
                            access = accessCode.toString(),
                            sharedTo = it.sharedTo,
                            isLocked = it.isLocked,
                            isOwner = it.isOwner
                        )
                    )
                }
            }
        }
    }

    /*
     * Update states
     * */
    fun updateSharedListState() {
        viewState.onGetShare(commonList, item.intAccess)
        loadAvatars(commonList)
        if (isPopupShow) {
            isPopupShow = false
            shareItem?.isGuest?.let { viewState.onShowPopup(sharePosition, it) }
        }
    }

    fun updateSharedExternalState(isMessage: Boolean) {
        viewState.onExternalAccess(item.intAccess, isMessage)
    }

    fun updateActionButtonState() {
        viewState.onActionButtonState(!isAccessDenied)
    }

    fun updatePlaceholderState() {
        when {
            isAccessDenied -> viewState.onPlaceholderState(PlaceholderViews.Type.ACCESS)
            commonList.isEmpty() -> viewState.onPlaceholderState(PlaceholderViews.Type.SHARE)
            else -> viewState.onPlaceholderState(PlaceholderViews.Type.NONE)
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
        viewState.onAddShare(item)
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

    private fun mapShareUi(share: Share, isGroup: Boolean): ShareUi {
        return if (!isGroup) {
            ShareUi(
                access = share.intAccess,
                sharedTo = share.sharedTo,
                isLocked = share.isLocked,
                isOwner = share.isOwner,
                isGuest = share.sharedTo.isVisitor,
                isRoom = item is CloudFolder && item.isRoom
            )
        } else {
            ShareUi(
                access = share.intAccess,
                sharedTo = share.sharedTo,
                isLocked = share.isLocked,
                isOwner = share.isOwner,
                isGuest = share.sharedTo.isVisitor,
                isRoom = false
            )
        }
    }

    private fun getShareList(shareList: List<Share>) {
        isAccessDenied = false
        val userList = shareList.filter { it.sharedTo.userName.isNotEmpty() }.map { mapShareUi(it, false) }
        val groupList = shareList.filter { it.sharedTo.name.isNotEmpty() }.map { mapShareUi(it, true) }
            .sortedWith(groupComparator())

        shareList.find { it.sharedTo.shareLink.isNotEmpty() }?.let {
            item.access = it.access
            item.shared = isShared(it.intAccess)
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
            viewState.onGetShareItem(commonList[sharePosition], sharePosition, item.intAccess)
            isShare = false
        } else {
            viewState.onGetShare(commonList, item.intAccess)
        }

        if (commonList.isEmpty()) {
            viewState.onPlaceholderState(PlaceholderViews.Type.SHARE)
        } else {
            viewState.onPlaceholderState(PlaceholderViews.Type.NONE)
        }

        loadAvatars(commonList)
    }

    private fun loadAvatars(commonList: ArrayList<ViewType>) {
        val users = commonList.filterIsInstance<ShareUi>().filter { it.sharedTo.avatar.isNotEmpty() }
        presenterScope.launch {
            requestIterable(
                iterable = users,
                map = { user ->
                    user.also {
                        user.avatar = GlideUtils.loadAvatar(user.sharedTo.avatarMedium)
                    }
                },
                onEach = viewState::onUpdateAvatar
            )
        }
    }

    private fun groupComparator() = Comparator<ShareUi> { a, b ->
        val list = listOf(GroupUi.GROUP_ADMIN_ID, GroupUi.GROUP_EVERYONE_ID)
        when {
            a.sharedTo.id in list -> -1
            b.sharedTo.id in list -> 1
            else -> a.sharedTo.displayName.compareTo(b.sharedTo.displayName)
        }
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