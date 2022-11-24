package app.documents.core.network.manager.models.user

import lib.toolkit.base.managers.utils.StringUtils.getHtmlString
import app.documents.core.network.manager.models.base.ItemProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

class User : ItemProperties(), Serializable, Comparable<User> {
    @SerializedName("id")
    @Expose
    var id: String = ""

    @SerializedName("userName")
    @Expose
    var userName = ""

    @SerializedName("isVisitor")
    @Expose
    var isVisitor = false

    @SerializedName("firstName")
    @Expose
    var firstName = ""

    @SerializedName("lastName")
    @Expose
    var lastName = ""

    @SerializedName("email")
    @Expose
    var email = ""

    @SerializedName("birthday")
    @Expose
    var birthday = ""

    @SerializedName("sex")
    @Expose
    var sex = ""

    @SerializedName("status")
    @Expose
    var status = ""

    @SerializedName("activationStatus")
    @Expose
    var activationStatus = ""

    @SerializedName("terminated")
    @Expose
    var terminated = ""

    @SerializedName("department")
    @Expose
    var department = ""

    @SerializedName("workFrom")
    @Expose
    var workFrom = ""

    @SerializedName("location")
    @Expose
    var location = ""

    @SerializedName("notes")
    @Expose
    var notes = ""

    @SerializedName("displayName")
    @Expose
    var displayName = ""

    @SerializedName("title")
    @Expose
    var title = ""

    @SerializedName("contacts")
    @Expose
    var contacts: List<Contact> = ArrayList()

    @SerializedName("groups")
    @Expose
    var groups: List<Group> = ArrayList()

    @SerializedName("avatarMedium")
    @Expose
    var avatarMedium = ""

    @SerializedName("avatar")
    @Expose
    var avatar = ""

    @SerializedName("isOnline")
    @Expose
    var isOnline = false

    @SerializedName("isAdmin")
    @Expose
    var isAdmin = false

    @SerializedName("isLDAP")
    @Expose
    var isLDAP = false

    @SerializedName("listAdminModules")
    @Expose
    var listAdminModules: List<String> = ArrayList()

    @SerializedName("isOwner")
    @Expose
    var isOwner = false

    @SerializedName("cultureName")
    @Expose
    var cultureName = ""

    @SerializedName("isSSO")
    @Expose
    var isSSO = false

    @SerializedName("avatarSmall")
    @Expose
    var avatarSmall = ""

    @SerializedName("profileUrl")
    @Expose
    var profileUrl = ""

    val displayNameHtml: String
        get() = getHtmlString(displayName)

    override operator fun compareTo(other: User): Int {
        var compare = displayName.compareTo(other.displayNameHtml)
        if (compare != 0) {
            return compare
        }
        compare = department.compareTo(other.department)
        return if (compare == 0) id!!.compareTo(other.id!!) else compare
    }
}