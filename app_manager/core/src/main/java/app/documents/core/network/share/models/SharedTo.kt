package app.documents.core.network.share.models

import app.documents.core.model.login.Group
import kotlinx.serialization.Serializable
import lib.toolkit.base.managers.utils.StringUtils

@Serializable
data class SharedTo(
    val id: String = "",
    val userName: String = "",
    val isVisitor: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val status: Int = 0,
    val activationStatus: Int = 0,
    val terminated: String? = "",
    val department: String = "",
    val workFrom: String = "",
    val displayName: String = "",
    val mobilePhone: String = "",
    val groups: List<Group> = emptyList(),
    val avatarMedium: String = "",
    val avatar: String = "",
    val isOnline: Boolean = false,
    val isAdmin: Boolean = false,
    val isRoomAdmin: Boolean = false,
    val isLDAP: Boolean = false,
    val listAdminModules: List<String> = emptyList(),
    val isOwner: Boolean = false,
    val isSSO: Boolean = false,
    val avatarSmall: String = "",
    val profileUrl: String = "",
    val name: String = "",
    val manager: String? = "",
    val shareLink: String = ""
) {

    val nameHtml: String
        get() = StringUtils.getHtmlString(name)

    val displayNameHtml: String
        get() = StringUtils.getHtmlString(displayName)

}