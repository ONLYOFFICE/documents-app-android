package app.documents.core.network.manager.models.explorer

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.contracts.ApiContract.ShareType.getCode
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Current : Cloneable, Serializable {

    @SerializedName("parentId")
    @Expose
    var parentId = ""

    @SerializedName("filesCount")
    @Expose
    var filesCount = ""

    @SerializedName("foldersCount")
    @Expose
    var foldersCount = ""

    @SerializedName("isShareable")
    @Expose
    var isShareable = false

    @SerializedName("id")
    @Expose
    var id = ""

    @SerializedName("title")
    @Expose
    var title = ""

    @SerializedName("access")
    @Expose
    var access = ApiContract.ShareType.NONE

    @SerializedName("shared")
    @Expose
    var shared = false

    @SerializedName("rootFolderType")
    @Expose
    var rootFolderType = ApiContract.SectionType.UNKNOWN

    @SerializedName("updatedBy")
    @Expose
    var updatedBy = UpdatedBy()

    @SerializedName("created")
    @Expose
    var created = ""

    @SerializedName("createdBy")
    @Expose
    var createdBy = CreatedBy()

    @SerializedName("updated")
    @Expose
    var updated = ""

    @SerializedName("providerItem")
    @Expose
    var providerItem = false

    @SerializedName("pinned")
    @Expose
    var pinned = false

    @SerializedName("roomType")
    @Expose
    var roomType = -1

    @SerializedName("canShare")
    @Expose
    var isCanShare = false

    @SerializedName("canEdit")
    @Expose
    var isCanEdit = false

    @SerializedName("security")
    @Expose
    var security: Security? = null

    @SerializedName("lifetime")
    @Expose
    var lifetime: Lifetime? = null

    public override fun clone(): Current {
        return (super.clone() as Current).apply {
            createdBy = createdBy.clone()
            updatedBy = updatedBy.clone()
        }
    }

    val intAccess: Int
        get() {
            val access = access
            return try {
                access.toInt()
            } catch (error: NumberFormatException) {
                getCode(access)
            }
        }
}