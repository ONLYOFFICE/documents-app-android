package app.editors.manager.mvp.presenters.share

import android.content.Intent
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.extensions.request
import app.documents.core.network.share.models.Share
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.GlideUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.share.ShareService
import app.documents.core.network.share.models.request.*
import app.editors.manager.app.appComponent
import app.editors.manager.app.shareApi
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
        private const val TAG_FOLDER_PATH = "products/files/#"
        private const val TAG_ROOM_PATH = "rooms/shared/filter?folder="
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
    private val shareApi: ShareService

    init {
        App.getApp().appComponent.inject(this)
        shareApi = context.shareApi
    }

    /*
     * Requests
     * */
    private suspend fun getShareFolder(id: String) {
        request(
            func = { shareApi.getShareFolder(id) },
            map = { it.response },
            onSuccess = ::getShareList,
            onError = ::fetchError
        )
    }

    private suspend fun getShareFile(id: String) {
        request(
            func = { shareApi.getShareFile(id) },
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
            func = { shareApi.setFileAccess(id, getRequestShare(isNotify, message, *shareList)) },
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
        if ((item as CloudFolder).isRoom) {
            shareRoom(id, isNotify, message, shareList)
        } else {
            request(
                func = {
                    shareApi.setFolderAccess(
                        id,
                        getRequestShare(isNotify, message, *shareList)
                    )
                },
                map = { it.response },
                onSuccess = ::getShareList, onError = ::fetchError
            )
        }
    }

    private fun shareRoom(
        id: String,
        isNotify: Boolean,
        message: String?,
        shareList: Array<out Share?>
    ) {
        presenterScope.launch {
            request(
                func = {
                    shareApi.shareRoom(
                        id = id,
                        body = RequestRoomShare(
                            invitations = shareList.map { share ->
                                Invitation(
                                    id = share?.sharedTo?.id,
                                    access = share?.access?.toInt() ?: 0
                                )
                            },
                            notify = isNotify,
                            message = message.orEmpty()
                        )
                    )
                },
                map = { it.response.members },
                onSuccess = ::getShareList
            ) { error ->
                if (error is HttpException && error.response()
                        ?.code() == ApiContract.HttpCodes.CLIENT_FORBIDDEN
                ) {
                    viewState.onError(context.getString(R.string.placeholder_access_denied))
                } else {
                    fetchError(error)
                }
            }
        }
    }

    private fun isShared(accessCode: Int): Boolean {
        val isShared =
            accessCode != ApiContract.ShareCode.RESTRICT && accessCode != ApiContract.ShareCode.NONE
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
            shareRights.add(
                RequestShareItem(
                    shareTo = idItem ?: "",
                    access = accessCode.toString()
                )
            )
        }
        return RequestShare(
            share = shareRights,
            isNotify = isNotify,
            sharingMessage = message ?: ""
        )
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
            val internalLink = if (item.isRoom) {
                networkSettings.getBaseUrl() + TAG_ROOM_PATH + item.id
            } else {
                networkSettings.getBaseUrl() + TAG_FOLDER_PATH + item.id
            }
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
                        shareApi.getExternalLink(item.id, request)
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
                        shareApi.setExternalLinkAccess(item.id, request)
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
                shareItem?.let {
                    setShareFolder(
                        item.id,
                        false,
                        null,
                        Share(item.getAdminCode(accessCode).toString(), it.sharedTo, it.isLocked, it.isOwner)
                    )
                }
            } else {
                shareItem?.let {
                    setShareFile(
                        item.id,
                        false,
                        null,
                        Share(accessCode.toString(), it.sharedTo, it.isLocked, it.isOwner)
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
    }

    /*
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
        val userList = shareList.filter { it.sharedTo.userName.isNotEmpty() }
            .map {
                ShareUi(
                    access = it.intAccess,
                    sharedTo = it.sharedTo,
                    isLocked = if (networkSettings.isDocSpace) {
                        !item.security.editAccess
                    } else {
                        it.isLocked
                    },
                    isOwner = it.isOwner,
                    isGuest = it.sharedTo.isVisitor,
                    isRoom = item is CloudFolder && item.isRoom
                )
            }
        val groupList = shareList.filter { it.sharedTo.name.isNotEmpty() }
            .map {
                ShareUi(
                    access = it.intAccess,
                    sharedTo = it.sharedTo,
                    isLocked = it.isLocked,
                    isOwner = it.isOwner,
                    isGuest = it.sharedTo.isVisitor,
                    isRoom = false
                )
            }
            .sortedWith(groupComparator())

        shareList.find { it.sharedTo.shareLink.isNotEmpty() }?.let {
            item.access = it.access
            item.shared = isShared(it.intAccess)
            externalLink = it.sharedTo.shareLink
        }

        commonList.clear()

        if (userList.isNotEmpty()) {
            checkUsers(userList)
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

        viewState.onActionButtonState(true)
        loadAvatars(commonList)
    }

    private fun checkUsers(userList: List<ShareUi>) {
        val pending = mutableListOf<ShareUi>()
        val members = mutableListOf<ShareUi>()

        userList.forEach {
            if (it.sharedTo.activationStatus == ApiContract.ActivationStatus.Pending) {
                pending.add(it)
            } else {
                members.add(it)
            }
        }
        if (members.isNotEmpty()) {
            commonList.add(ShareHeaderUi(context.getString(R.string.share_goal_user)))
            commonList.addAll(members.sortedByDescending { it.isOwner })
        }

        if (pending.isNotEmpty()) {
            commonList.add(ShareHeaderUi("Expect members"))
            commonList.addAll(pending)
        }
    }

    private fun loadAvatars(commonList: ArrayList<ViewType>) {
        val users = commonList.filterIsInstance<ShareUi>().filter { it.sharedTo.avatarSmall.isNotEmpty() }
        presenterScope.launch {
            users.request(
                func = { user -> GlideUtils.getAvatarFromUrl(context, user.sharedTo.avatarSmall) },
                map = { user, avatar -> user.also { user.avatar = avatar } },
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
        if (throwable is HttpException && throwable.response()
                ?.code() == ApiContract.HttpCodes.CLIENT_FORBIDDEN
        ) {
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

fun CloudFolder.getAdminCode(code: Int): Int {
    return if (isRoom && code == ApiContract.ShareCode.ROOM_ADMIN) {
         ApiContract.ShareCode.ROOM_ADMIN
    } else code
}