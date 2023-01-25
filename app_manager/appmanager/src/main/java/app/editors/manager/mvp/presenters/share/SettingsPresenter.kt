package app.editors.manager.mvp.presenters.share

import android.content.Intent
import app.documents.core.network.ApiContract
import app.documents.core.network.models.share.Share
import app.documents.core.network.models.share.request.*
import app.documents.core.share.ShareService
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.app.getShareApi
import app.editors.manager.managers.utils.FirebaseUtils
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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.InjectViewState
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
    private val sharedService: ShareService

    private var disposable: Disposable? = null

    init {
        App.getApp().appComponent.inject(this)
        sharedService = context.getShareApi()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

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
                if (it.response.isNotEmpty()) {
                    externalLink = it.response[0].sharedTo.shareLink
                }
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
        id: String,
        isNotify: Boolean,
        message: String?,
        vararg shareList: Share?
    ) {
        val requestShare = getRequestShare(isNotify, message, *shareList)
        if ((item as CloudFolder).isRoom) {
            shareRoom(id, isNotify, message, shareList)
        } else {
            disposable = sharedService.setFolderAccess(id, requestShare)
                .subscribeOn(Schedulers.io())
                .map { it.response }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    getShareList(it)
                }, { fetchError(it) })
        }
    }

    private fun shareRoom(
        id: String,
        isNotify: Boolean,
        message: String?,
        shareList: Array<out Share?>
    ) {
        disposable = sharedService.shareRoom(
            id = id,
            body = RequestRoomShare(
                invitations = shareList.map { share ->
                    Invitation(
                        id = share?.sharedTo?.id,
                        access = share?.access?.toInt() ?: 0
                    )
                },
                notify = isNotify,
                message = message ?: ""
            )
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                getShareList(it.response.members)
            }, { error ->
                if (error is HttpException && error.response()?.code() == ApiContract.HttpCodes.CLIENT_FORBIDDEN) {
                    viewState.onError(context.getString(R.string.placeholder_access_denied))
                } else {
                    fetchError(error)
                }
            })
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

    val shared: Unit
        get() {
            if (item is CloudFolder) {
                getShareFolder(item.id)
            } else if (item is CloudFile) {
                getShareFile(item.id)
            }
        }

    val internalLink: Unit
        get() {
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
        val code = ApiContract.ShareType.getCode(share)
        if (!isPersonalAccount) {
            disposable = sharedService
                .getExternalLink(item.id ?: "", RequestExternal(share = share ?: ""))
                .subscribeOn(Schedulers.io())
                .map { it.body()?.response }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    externalLink = it
                    item.access = code.toString()
                    item.shared = isShared(code)
                    viewState.onExternalAccess(code, true)
                }
        } else {
            disposable = sharedService
                .setExternalLinkAccess(item.id ?: "", RequestExternalAccess(code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    item.access = code.toString()
                    item.shared = isShared(code)
                    viewState.onExternalAccess(code, true)
                }, { error -> fetchError(error) }
                )
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
        if (item is CloudFolder) {
            shareItem?.let {
                setShareFolder(
                    item.id,
                    false,
                    null,
                    Share(accessCode.toString(), it.sharedTo, it.isLocked, it.isOwner)
                )
            }
        } else {
            shareItem?.let {
                setShareFile(
                    item.id ?: "",
                    false,
                    null,
                    Share(accessCode.toString(), it.sharedTo, it.isLocked, it.isOwner)
                )
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

    private fun getShareList(shareList: List<Share>) {
        isAccessDenied = false
        val userList = shareList.filter { it.sharedTo.userName.isNotEmpty() }
            .map {
                ShareUi(
                    access = it.intAccess,
                    sharedTo = it.sharedTo,
                    isLocked = if (networkSettings.isDocSpace) {
                        !item.isCanShare
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

        viewState.onActionButtonState(
            if (context.appComponent.networkSettings.isDocSpace) {
                item.isCanShare
            } else {
                true
            }
        )

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
                FirebaseUtils.addCrash(error)
                // Stub
            })
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