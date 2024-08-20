package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class User(
    override val id: String = "",
    val userName: String = "",
    val isVisitor: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val email: String? = "",
    val birthday: String = "",
    val sex: String = "",
    val status: Int = 0,
    val activationStatus: Int = 0,
    val terminated: String? = "",
    val department: String = "",
    val workFrom: String = "",
    val location: String = "",
    val notes: String = "",
    val displayName: String = "",
    val title: String = "",
    val contacts: List<Contact> = emptyList(),
    val groups: List<Group> = emptyList(),
    val avatarMedium: String = "",
    val avatar: String = "",
    val isOnline: Boolean = false,
    val isAdmin: Boolean = false,
    val isLDAP: Boolean = false,
    val listAdminModules: List<String> = emptyList(),
    val isOwner: Boolean = false,
    val cultureName: String = "",
    val isSSO: Boolean = false,
    val avatarSmall: String = "",
    val profileUrl: String = "",
    val mobilePhone: String = "",
    override val shared: Boolean = false
) : Member