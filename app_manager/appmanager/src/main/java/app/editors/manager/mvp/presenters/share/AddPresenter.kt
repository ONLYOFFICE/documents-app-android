package app.editors.manager.mvp.presenters.share

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.extensions.checkStatusCode
import app.documents.core.network.common.extensions.request
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.share.ShareService
import app.documents.core.network.share.models.request.RequestShare
import app.documents.core.network.share.models.request.RequestShareItem
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.UserUi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.share.AddView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.InjectViewState
import moxy.presenterScope
import java.io.Serializable

@InjectViewState
class AddPresenter(
    val item: Item,
    val type: Type
) : BasePresenter<AddView>() {

    sealed class Type : Serializable {
        data object Users : Type()
        data object Groups : Type()
        data object Common : Type()
    }

    companion object {
        val TAG: String = AddPresenter::class.java.simpleName
    }

    private val shareStack: ModelShareStack = ModelShareStack.getInstance()
    private var searchValue: String? = null
    private var job: Job? = null

    private val userListItems: List<UserUi>
        get() = shareStack.userSet.sortedBy { it.displayName }

    private val groupListItems: List<GroupUi>
        get() = shareStack.groupSet.sortedWith(groupComparator())

    val countChecked: Int
        get() = shareStack.countChecked

    val isSelectedAll: Boolean
        get() = when (type) {
            Type.Users -> shareStack.userSet.any { !it.isSelected }
            Type.Groups -> shareStack.groupSet.any { !it.isSelected }
            else -> false
        }

    val isSelected: Boolean
        get() = when (type) {
            Type.Users -> shareStack.userSet.any { it.isSelected }
            Type.Groups -> shareStack.groupSet.any { it.isSelected }
            else -> false
        }

    var accessCode: Int
        get() = shareStack.accessCode
        set(accessCode) {
            shareStack.accessCode = accessCode
        }

    init {
        App.getApp().appComponent.inject(this)
    }

    private val account: CloudAccount =
        context.appComponent.accountOnline ?: throw RuntimeException("Account can't be null")

    private val shareApi: ShareService = context.shareApi

    override fun onDestroy() {
        super.onDestroy()
        job = null
    }

    private suspend fun getUsers(searchValue: String = ""): List<UserUi> {

        val invitedUsersId = if ((item as? CloudFolder)?.isRoom == true) {
            context.roomProvider.getRoomUsers(item.id).map { it.sharedTo.id }
        } else {
            emptyList()
        }

        return shareApi.getUsers(getOptions(searchValue))
            .checkStatusCode(::fetchError)
            .response
            .filter { user ->
                user.id != account.id
                        && user.id != item.createdBy.id
                        && user.displayName.isNotEmpty()
                        && !invitedUsersId.contains(user.id)
                        && user.activationStatus == 1
            }
            .map { user ->
                UserUi(
                    id = user.id,
                    department = user.department,
                    displayName = user.displayName,
                    avatarUrl = user.avatar,
                    status = user.activationStatus
                )
            }.sortedBy { it.status }
    }

    private suspend fun getGroups(searchValue: String = ""): List<GroupUi> {
        return shareApi.getGroups(getOptions(searchValue, true))
            .checkStatusCode(::fetchError)
            .response
            .map {
                GroupUi(
                    id = it.id,
                    name = it.name,
                    manager = it.manager.orEmpty()
                )
            }
            .toMutableList()
            .also { list ->
                list.add(GroupUi(GroupUi.GROUP_ADMIN_ID, "", ""))
                list.add(GroupUi(GroupUi.GROUP_EVERYONE_ID, "", ""))
            }

    }

    private suspend fun shareFileTo(id: String) {
        requestShare?.let { request ->
            request(
                func = { shareApi.setFileAccess(id, request) },
                onSuccess = {
                    shareStack.resetChecked()
                    viewState.onSuccessAdd()
                },
                onError = ::fetchError
            )
        }
    }

    private suspend fun shareFolderTo(id: String) {
        requestShare?.let { request ->
            request(
                func = { shareApi.setFolderAccess(id, request) },
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

    fun fetchSharedList(searchValue: String = "") {
        presenterScope.launch {
            when (type) {
                Type.Common -> {
                    shareStack.clearModel()
                    shareStack.addGroups(getGroups(searchValue))
                    shareStack.addUsers(getUsers(searchValue))
                    viewState.onGetCommon(commonList)
                }
                Type.Groups -> {
                    shareStack.addGroups(getGroups(searchValue))
                    viewState.onGetGroups(groupListItems)
                }
                Type.Users -> {
                    shareStack.clearModel()
                    shareStack.addUsers(getUsers(searchValue))
                    viewState.onGetUsers(userListItems)
                }
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

    fun updateTypeSharedListState() {
        when (type) {
            Type.Common -> viewState.onGetCommon(commonList)
            Type.Groups -> viewState.onGetGroups(groupListItems)
            Type.Users -> viewState.onGetUsers(userListItems)
        }
    }

    fun updateSearchState() {
        viewState.onSearchValue(searchValue)
    }

    fun resetChecked() {
        shareStack.resetChecked()
    }

    fun setMessage(message: String?) {
        shareStack.message = message
    }

    fun setSearchValue(searchValue: String?) {
        this.searchValue = searchValue
        job?.cancel()
        job = presenterScope.launch {
            delay(350L)
            fetchSharedList(searchValue.orEmpty())
        }
    }
}