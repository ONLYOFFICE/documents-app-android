package app.editors.manager.mvp.models.models

import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.UserUi
import java.lang.ref.WeakReference
import java.util.TreeSet

class ModelShareStack {

    val userSet: MutableSet<UserUi> = TreeSet()
    val groupSet: MutableSet<GroupUi> = TreeSet()

    var accessCode: Int = ApiContract.ShareCode.READ
    var message: String? = null
    var isRefresh: Boolean = false

    val countChecked: Int
        get() {
            var count = 0
            for (item in userSet) {
                if (item.isSelected) {
                    ++count
                }
            }
            for (item in groupSet) {
                if (item.isSelected) {
                    ++count
                }
            }
            return count
        }

    fun resetChecked() {
        for (item in userSet) {
            item.isSelected = false
        }
        for (item in groupSet) {
            item.isSelected = false
        }
    }

    fun clearModel() {
        isRefresh = false
        userSet.clear()
        groupSet.clear()
    }

    fun removeById(id: String?): Boolean {
        val userSet = userSet.iterator()
        while (userSet.hasNext()) {
            val (id1) = userSet.next()
            if (id1.equals(id, ignoreCase = true)) {
                userSet.remove()
                return true
            }
        }
        val groupSet = groupSet.iterator()
        while (groupSet.hasNext()) {
            val (id1) = groupSet.next()
            if (id1.equals(id, ignoreCase = true)) {
                groupSet.remove()
                return true
            }
        }
        return false
    }

    val isUserEmpty: Boolean
        get() = userSet.isEmpty()

    val isGroupEmpty: Boolean
        get() = groupSet.isEmpty()

    fun addUser(user: UserUi) {
        userSet.add(user)
    }

    fun addUsers(userList: List<UserUi>) {
        userSet.addAll(userList)
    }

    fun addGroup(group: GroupUi) {
        groupSet.add(group)
    }

    fun addGroups(groupList: List<GroupUi>) {
        groupSet.addAll(groupList)
    }

    companion object {

        private var weakReference: WeakReference<ModelShareStack>? = null

        fun getInstance(): ModelShareStack {
            val modelShareStack: ModelShareStack
            if (weakReference == null || weakReference?.get() == null) {
                modelShareStack = ModelShareStack()
                weakReference = WeakReference(modelShareStack)
            } else {
                modelShareStack = weakReference?.get() ?: throw NullPointerException("ModelShareStack is null")
            }
            return modelShareStack
        }

    }
}