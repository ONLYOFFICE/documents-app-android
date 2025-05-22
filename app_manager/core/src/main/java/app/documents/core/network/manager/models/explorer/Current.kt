package app.documents.core.network.manager.models.explorer

import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract
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

    @SerializedName("inRoom")
    @Expose
    var inRoom: Boolean = false

    @SerializedName("access")
    @Expose
    private var _access = Access.None.type

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

    @SerializedName("indexing")
    @Expose
    var indexing: Boolean = false

    public override fun clone(): Current {
        return (super.clone() as Current).apply {
            createdBy = createdBy.clone()
            updatedBy = updatedBy.clone()
        }
    }

    val access: Access
        get() = runCatching {
            Access.get(_access.toInt())
        }.getOrElse {
            Access.get(_access)
        }

    val isTemplate: Boolean
        get() = ApiContract.SectionType.isTemplates(rootFolderType) && inRoom
}