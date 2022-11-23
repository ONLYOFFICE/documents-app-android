package app.editors.manager.mvp.presenters.share

import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.request
import app.documents.core.network.common.requestZip
import app.documents.core.network.share.models.request.RequestShare
import app.documents.core.network.share.models.request.RequestShareItem
import app.documents.core.repositories.ShareRepository
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.app.getShareRepository
import app.editors.manager.managers.utils.GlideUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.UserUi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.share.AddView
import app.editors.manager.ui.fragments.share.AddFragment
import kotlinx.coroutines.*
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.InjectViewState
import moxy.presenterScope

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
    private var job: Job? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    private val account: CloudAccount =
        context.appComponent.accountOnline ?: throw RuntimeException("Account can't be null")

    private val shareRepository: ShareRepository = context.getShareRepository()

    override fun onDestroy() {
        super.onDestroy()
        job = null
    }

    private suspend fun getUsers() {
        isCommon = false
        request(
            func = shareRepository::getUsers,
            map = { response ->
                response.response.filter { it.id != account.id && it.id != item.createdBy?.id }.map { user ->
                    UserUi(
                        id = user.id,
                        department = user.department,
                        displayName = user.displayName.takeIf { name -> name.isEmpty() } ?: user.email ?: "",
                        avatarUrl = user.avatarMedium,
                        status = user.activationStatus)
                }.sortedBy { it.status } },
            onSuccess = { users ->
                shareStack.addUsers(users)
                viewState.onGetUsers(userListItems)
                loadAvatars()
            }, onError = ::fetchError
        )
    }

    private suspend fun getGroups() {
        isCommon = false
        request(
            func = shareRepository::getGroups,
            map = { response -> response.response.map { GroupUi(it.id, it.name, it.manager ?: "null") } },
            onSuccess = { response ->
                shareStack.addGroups(response.toMutableList().also {
                    it.add(GroupUi(GroupUi.GROUP_ADMIN_ID, "", ""))
                    it.add(GroupUi(GroupUi.GROUP_EVERYONE_ID, "", ""))
                })
                viewState.onGetGroups(groupListItems)
            }, onError = ::fetchError
        )
    }

    fun getCommons() {
        isCommon = true
        presenterScope.launch {
            requestZip(
                func1 = shareRepository::getUsers,
                func2 = shareRepository::getGroups,
                onSuccess = { users, groups ->
                    shareStack.addGroups(groups.response.map { GroupUi(it.id, it.name, it.manager ?: "null") })
                    shareStack.addUsers(users.response.filter { it.id != account.id }.map { user ->
                        UserUi(
                            id = user.id,
                            department = user.department,
                            displayName = user.displayName.takeIf { it.isEmpty() } ?: user.email ?: "",
                            avatarUrl = user.avatarMedium, status = user.activationStatus)
                    })
                    viewState.onGetCommon(commonList)
                    loadAvatars()
                }, onError = ::fetchError
            )
        }
    }

    fun getFilter(searchValue: String) {
        isCommon = true
        presenterScope.launch {
            requestZip(
                func1 = { shareRepository.getUsers(getOptions(searchValue)) },
                func2 = { shareRepository.getGroups(getOptions(searchValue, true)) },
                onSuccess = { users, groups ->
                    shareStack.clearModel()
                    shareStack.addGroups(groups.response.map { GroupUi(it.id, it.name, it.manager ?: "null") })
                    shareStack.addUsers(users.response.filter { it.id != account.id }.map { user ->
                        UserUi(
                            id = user.id,
                            department = user.department,
                            displayName = user.displayName.takeIf { it.isEmpty() } ?: user.email ?: "",
                            avatarUrl = user.avatarMedium,
                            status = user.activationStatus)
                    })
                    viewState.onGetCommon(commonList)
                    loadAvatars()
                }, onError = ::fetchError
            )
        }
    }

    private fun loadAvatars() {
        presenterScope.launch {
            shareStack.userSet.forEach { user ->
                val loadedAvatar = GlideUtils.loadAvatar(user.avatarUrl)
                withContext(Dispatchers.Main) {
                    val userUi = user.also { it.avatar = loadedAvatar }
                    viewState.onUpdateAvatar(userUi)
                }
            }
        }
    }

    private suspend fun shareFileTo(id: String) {
        requestShare?.let { request ->
            request(
                func = { shareRepository.setFileAccess(id, request) },
                onSuccess = {
                    shareStack.resetChecked()
                    viewState.onSuccessAdd()
                }, onError = ::fetchError
            )
        }
    }

    private suspend fun shareFolderTo(id: String) {
        requestShare?.let { request ->
            request(
                func = { shareRepository.setFolderAccess(id, request) },
                onSuccess = {
                    shareStack.resetChecked()
                    viewState.onSuccessAdd()
                }, onError = ::fetchError
            )
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
            presenterScope.launch {
                if (type == AddFragment.Type.USERS) {
                    getUsers()
                } else {
                    getGroups()
                }
            }
        }

    fun shareItem() {
        presenterScope.launch {
            when (item) {
                is CloudFolder -> shareFolderTo(item.id)
                is CloudFile -> shareFileTo(item.id)
            }
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
            else -> {
                false
            }
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
        job?.cancel()
        job = presenterScope.launch {
            delay(350L)
            withContext(Dispatchers.Main) {
                getFilter(searchValue.orEmpty())
            }
        }
    }
}