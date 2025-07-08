package app.documents.core.model.login

import app.documents.core.model.cloud.UserType
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
    val avatarMax: String = "",
    val avatar: String = "",
    val isOnline: Boolean = false,
    val isAdmin: Boolean = false,
    val isRoomAdmin: Boolean = false,
    val isGuest: Boolean = false,
    val isLDAP: Boolean = false,
    val listAdminModules: List<String> = emptyList(),
    val isOwner: Boolean = false,
    val cultureName: String = "",
    val isSSO: Boolean = false,
    val avatarSmall: String = "",
    val hasAvatar: Boolean = false,
    val profileUrl: String = "",
    val mobilePhone: String = "",
    val quotaLimit: Long = 0,
    val usedSpace: Long = 0,
    override val shared: Boolean = false
) : Member {

    val type: UserType
        get() = when {
            isOwner -> UserType.Owner
            isAdmin -> UserType.Admin
            isRoomAdmin -> UserType.RoomAdmin
            isGuest -> UserType.Guest
            else -> UserType.User
        }

    val avatarUrl: String
        get() = avatarMax.takeIf { it.isNotBlank() }
            ?: avatar.takeIf { it.isNotBlank() }
            ?: avatarMedium
}