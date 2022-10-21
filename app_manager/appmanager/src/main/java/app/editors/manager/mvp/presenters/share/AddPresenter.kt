package app.editors.manager.mvp.presenters.share

import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.network.models.share.request.RequestShare
import app.documents.core.network.models.share.request.RequestShareItem
import app.documents.core.share.ShareService
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.app.getShareApi
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.UserUi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.share.AddView
import app.editors.manager.ui.fragments.share.AddFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.InjectViewState
import java.util.concurrent.TimeUnit

@InjectViewState
class AddPresenter(
    val item: Item,
    val type: AddFragment.Type
) : BasePresenter<AddView>() {

    companion object {
        val TAG: String = AddPresenter::class.java.simpleName
    }

    private val shareStack: ModelShareStack = ModelShareStack.getInstance()
    private var isCommon: Boolean = false
    private var searchValue: String? = null
    private var publishSearch: PublishSubject<String>? = null
    private var disposable: Disposable? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    private val account: CloudAccount =
        context.appComponent.accountOnline ?: throw RuntimeException("Account can't be null")
    private val shareApi: ShareService = context.getShareApi()

    override fun onDestroy() {
        super.onDestroy()
        disposable = null
    }


    private fun getUsers() {
        isCommon = false
        disposable = shareApi.getUsers()
            .subscribeOn(Schedulers.io())
            .map { response ->
                response.response.filter { it.id != account.id && it.id != item.createdBy?.id }.map {
                    UserUi(it.id, it.department, it.displayName, it.avatarMedium)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                shareStack.addUsers(response)
                viewState.onGetUsers(userListItems)
                loadAvatars()
            }, { error ->
                fetchError(error)
            })
    }

    private fun getGroups() {
        isCommon = false
        disposable = shareApi.getGroups()
            .subscribeOn(Schedulers.io())
            .map { response -> response.response.map { GroupUi(it.id, it.name, it.manager ?: "null") } }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                shareStack.addGroups(response.toMutableList().also {
                    it.add(GroupUi(GroupUi.GROUP_ADMIN_ID, "", ""))
                    it.add(GroupUi(GroupUi.GROUP_EVERYONE_ID, "", ""))
                })
                viewState.onGetGroups(groupListItems)
            }, { error ->
                fetchError(error)
            })
    }

    fun getCommons() {
        isCommon = true
        disposable = Observable.zip(shareApi.getUsers(), shareApi.getGroups()) { users, groups ->
            shareStack.addGroups(groups.response.map { GroupUi(it.id, it.name, it.manager ?: "null") })
            shareStack.addUsers(users.response.filter { it.id != account.id }.map {
                UserUi(it.id, it.department, it.displayName, it.avatarMedium)
            })
            return@zip true
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewState.onGetCommon(commonList)
                loadAvatars()
            }, { error ->
                fetchError(error)
            })
    }

    fun getFilter(searchValue: String) {
        isCommon = true

        disposable = Observable.zip(
            shareApi.getUsers(getOptions(searchValue)),
            shareApi.getGroups(getOptions(searchValue, true))
        ) { users, groups ->
            shareStack.clearModel()
            shareStack.addGroups(groups.response.map { GroupUi(it.id, it.name, it.manager ?: "null") })
            shareStack.addUsers(users.response.filter { it.id != account.id }.map {
                UserUi(it.id, it.department, it.displayName, it.avatarMedium)
            })
            return@zip true
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewState.onGetCommon(commonList)
                loadAvatars()
            }, { error ->
                fetchError(error)
            })
    }

    private fun loadAvatars() {
        disposable = Observable.fromIterable(shareStack.userSet)
            .subscribeOn(Schedulers.io())
            .map { user ->
                user.avatar = GlideUtils.loadAvatar(user.avatarUrl)
                user
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewState.onUpdateAvatar(it)
            }, { error ->
                fetchError(error)
            })
    }

    private fun shareFileTo(id: String) {
        requestShare?.let { request ->
            disposable = shareApi.setFileAccess(id, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    shareStack.resetChecked()
                    viewState.onSuccessAdd()
                }) { error ->
                    fetchError(error)
                }
        }

    }

    private fun shareFolderTo(id: String) {
        requestShare?.let { request ->
//            if ((item as CloudFolder).isRoom) {
//                disposable = shareApi.shareRoom(id, request)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({
//                        shareStack.resetChecked()
//                        viewState.onSuccessAdd()
//                    }) { error ->
//                        fetchError(error)
//                    }
//            } else {
                disposable = shareApi.setFolderAccess(id, request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        shareStack.resetChecked()
                        viewState.onSuccessAdd()
                    }) { error ->
                        fetchError(error)
                    }
//            }
        }
    }

    private val requestShare: RequestShare?
        get() {
            val shareItems: MutableList<RequestShareItem> = ArrayList()

            // Get users access list
            for (item in shareStack.userSet) {
                if (item.isSelected) {
                    val idItem = item.id
                    val accessCode = shareStack.accessCode
                    shareItems.add(RequestShareItem(shareTo = idItem, access = accessCode.toString()))
                }
            }

            // Get groups access list
            for (item in shareStack.groupSet) {
                if (item.isSelected) {
                    val idItem = item.id
                    val accessCode = shareStack.accessCode
                    shareItems.add(RequestShareItem(shareTo = idItem, access = accessCode.toString()))
                }
            }
            if (shareItems.isNotEmpty()) {
                val message = shareStack.message
                return RequestShare(
                    share = shareItems,
                    isNotify = !message.isNullOrEmpty(),
                    sharingMessage = message ?: ""
                )
            }
            return null
        }

    private val commonList: List<ViewType>
        get() {
            val commonList: MutableList<ViewType> = ArrayList()

            // Users
            if (!shareStack.isUserEmpty) {
                commonList.add(ShareHeaderUi(context.getString(R.string.share_add_common_header_users)))
                commonList.addAll(userListItems)
            }

            // Groups
            if (!shareStack.isGroupEmpty) {
                commonList.add(ShareHeaderUi(context.getString(R.string.share_add_common_header_groups)))
                commonList.addAll(groupListItems)
            }
            return commonList
        }

    private fun getOptions(value: String, isGroup: Boolean = false): Map<String, String> =
        mapOf(
            ApiContract.Parameters.ARG_FILTER_VALUE to value,
            ApiContract.Parameters.ARG_FILTER_BY to if (isGroup) ApiContract.Parameters.VAL_SORT_BY_NAME else ApiContract.Parameters.VAL_SORT_BY_DISPLAY_NAME,
            ApiContract.Parameters.ARG_FILTER_OP to ApiContract.Parameters.VAL_FILTER_OP_CONTAINS
        )

    private fun groupComparator() = Comparator<GroupUi> { a, b ->
        val list = listOf(GroupUi.GROUP_ADMIN_ID, GroupUi.GROUP_EVERYONE_ID)
        when {
            a.id in list -> -1
            b.id in list -> 1
            else -> a.name.lowercase().compareTo(b.name.lowercase())
        }
    }

    val shared: Unit
        get() {
            if (type == AddFragment.Type.USERS) {
                getUsers()
            } else {
                getGroups()
            }
        }

    fun shareItem() {
        when (item) {
            is CloudFolder -> shareFolderTo(item.id)
            is CloudFile -> shareFileTo(item.id)
        }
    }

    fun startSearch() {
        publishSearch = PublishSubject.create<String>().apply {
            disposable = subscribeOn(Schedulers.io())
                .debounce(350, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .doOnNext { value ->
                    getFilter(value)
                }.subscribe()
        }
    }

    /*
    * Update states
    * */
    fun updateTypeSharedListState() {
        when (type) {
            AddFragment.Type.USERS -> viewState.onGetUsers(userListItems)
            AddFragment.Type.GROUPS -> viewState.onGetGroups(groupListItems)
            else -> {}
        }
    }

    fun updateCommonSharedListState() {
        viewState.onGetCommon(commonList)
    }

    fun updateSearchState() {
        viewState.onSearchValue(searchValue)
    }

    /*
    * Getters/Setters
    * */

    private val userListItems: List<UserUi>
        get() = shareStack.userSet.toMutableList().sortedBy { it.displayName }

    private val groupListItems: List<GroupUi>
        get() = shareStack.groupSet.toMutableList().sortedWith(groupComparator())

    val countChecked: Int
        get() = shareStack.countChecked

    fun isSelectedAll(type: AddFragment.Type): Boolean {
        return if (shareStack.userSet.size > 0 || shareStack.groupSet.size > 0) {
            when (type) {
                AddFragment.Type.USERS ->
                    shareStack.userSet.filter { it.isSelected }.size == shareStack.userSet.size
                AddFragment.Type.GROUPS ->
                    shareStack.groupSet.filter { it.isSelected }.size == shareStack.groupSet.size
                else -> {
                    false
                }
            }
        } else {
            false
        }
    }

    fun isSelected(type: AddFragment.Type): Boolean {
        return when (type) {
            AddFragment.Type.USERS -> shareStack.userSet.any { it.isSelected }
            AddFragment.Type.GROUPS -> shareStack.groupSet.any { it.isSelected }
            else -> { false }
        }
    }

    fun resetChecked() {
        shareStack.resetChecked()
    }

    var accessCode: Int
        get() = shareStack.accessCode
        set(accessCode) {
            shareStack.accessCode = accessCode
        }

    fun setMessage(message: String?) {
        shareStack.message = message
    }

    fun setSearchValue(searchValue: String?) {
        this.searchValue = searchValue

        searchValue?.let { value ->
            publishSearch?.onNext(value)
        }
    }
}