package app.documents.core.network.manager.models.explorer

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class CloudFolder : Item(), Serializable {

    @SerializedName("parentId")
    @Expose
    var parentId = ""

    @SerializedName("filesCount")
    @Expose
    var filesCount = 0

    @SerializedName("foldersCount")
    @Expose
    var foldersCount = 0

    @SerializedName("providerKey")
    @Expose
    var providerKey = ""

    @SerializedName("pinned")
    @Expose
    var pinned = false

    @SerializedName("roomType")
    @Expose
    var roomType = -1

    @SerializedName("private")
    @Expose
    var isPrivate = false

    @SerializedName("new")
    @Expose
    var newCount = 0

    @SerializedName("tags")
    @Expose
    var tags = emptyArray<String>()

    @SerializedName("logo")
    @Expose
    val logo: CloudFolderLogo? = null

    fun setFolder(folder: CloudFolder) {
        setItem(folder)
        parentId = folder.parentId
        filesCount = folder.filesCount
        foldersCount = folder.foldersCount
        providerKey = folder.providerKey
    }

    private val isParentRoom: Boolean
        get() = rootFolderType.toInt() >= ApiContract.SectionType.CLOUD_VIRTUAL_ROOM

    val isRoom: Boolean
        get() = isParentRoom && roomType >= 1

    override fun clone(): CloudFolder {
        return super.clone() as CloudFolder
    }

    class SortFolderSize(isSortAsc: Boolean) : BaseResponse.AbstractSort<CloudFolder>(isSortAsc) {
        override fun compare(o1: CloudFolder, o2: CloudFolder): Int {
            return mSortOrder * o1.filesCount.compareTo(o2.filesCount)
        }
    }
}