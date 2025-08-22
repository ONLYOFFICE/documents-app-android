package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import app.documents.core.model.login.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lib.toolkit.base.managers.utils.FormRole

class StartFillingViewModel(formRoles: List<Pair<FormRole, User?>>) : ViewModel() {

    private val _rolesWithUsers: MutableStateFlow<List<Pair<FormRole, User?>>> =
        MutableStateFlow(formRoles)
    val rolesWithUsers: StateFlow<List<Pair<FormRole, User?>>> = _rolesWithUsers.asStateFlow()

    fun setUser(index: Int, user: User) {
        updateRole(index, user)
    }

    fun deleteUser(index: Int) {
        updateRole(index, null)
    }

    private fun updateRole(index: Int, user: User?) {
        _rolesWithUsers.update {
            it.toMutableList()
                .apply { this[index] = it[index].first to user }
        }
    }
}