package app.documents.core.network.manager.models.user

import app.documents.core.network.manager.models.base.ItemProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    @Expose
    var id: String = "",

    @SerializedName("userName")
    @Expose
    var userName: String = "",

    @SerializedName("isVisitor")
    @Expose
    var isVisitor: Boolean = false,

    @SerializedName("firstName")
    @Expose
    var firstName: String = "",

    @SerializedName("lastName")
    @Expose
    var lastName: String = "",

    @SerializedName("email")
    @Expose
    var email: String = "",

    @SerializedName("birthday")
    @Expose
    var birthday: String = "",

    @SerializedName("sex")
    @Expose
    var sex: String = "",

    @SerializedName("status")
    @Expose
    var status: String = "",

    @SerializedName("activationStatus")
    @Expose
    var activationStatus: String = "",

    @SerializedName("terminated")
    @Expose
    var terminated: String = "",

    @SerializedName("department")
    @Expose
    var department: String = "",

    @SerializedName("workFrom")
    @Expose
    var workFrom: String = "",

    @SerializedName("location")
    @Expose
    var location: String = "",

    @SerializedName("notes")
    @Expose
    var notes: String = "",

    @SerializedName("displayName")
    @Expose
    var displayName: String = "",

    @SerializedName("title")
    @Expose
    var title: String = "",

    @SerializedName("contacts")
    @Expose
    var contacts: List<Contact> = ArrayList(),

    @SerializedName("groups")
    @Expose
    var groups: List<Group> = ArrayList(),

    @SerializedName("avatarMedium")
    @Expose
    var avatarMedium: String = "",

    @SerializedName("avatar")
    @Expose
    var avatar: String = "",

    @SerializedName("isOnline")
    @Expose
    var isOnline: Boolean = false,

    @SerializedName("isAdmin")
    @Expose
    var isAdmin: Boolean = false,

    @SerializedName("isLDAP")
    @Expose
    var isLDAP: Boolean = false,

    @SerializedName("listAdminModules")
    @Expose
    var listAdminModules: List<String> = ArrayList(),

    @SerializedName("isOwner")
    @Expose
    var isOwner: Boolean = false,

    @SerializedName("cultureName")
    @Expose
    var cultureName: String = "",

    @SerializedName("isSSO")
    @Expose
    var isSSO: Boolean = false,

    @SerializedName("avatarSmall")
    @Expose
    var avatarSmall: String = "",

    @SerializedName("profileUrl")
    @Expose
    var profileUrl: String = ""
) : ItemProperties()