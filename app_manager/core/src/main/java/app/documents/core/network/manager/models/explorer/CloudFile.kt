package app.documents.core.network.manager.models.explorer

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.base.ItemProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class CloudFile : Item() {

    @SerializedName("folderId")
    @Expose
    var folderId = ""

    @SerializedName("version")
    @Expose
    var version = 0

    @SerializedName("versionGroup")
    @Expose
    var versionGroup = ""

    @SerializedName("contentLength")
    @Expose
    var contentLength = ""

    @SerializedName("pureContentLength")
    @Expose
    var pureContentLength: Long = 0

    @SerializedName("fileStatus")
    @Expose
    var fileStatus = ""

    @SerializedName("viewUrl")
    @Expose
    var viewUrl = ""

    @SerializedName("webUrl")
    @Expose
    var webUrl = ""

    @SerializedName("fileType")
    @Expose
    var fileType = ""

    @SerializedName("fileExst")
    @Expose
    var fileExst = ""

    @SerializedName("comment")
    @Expose
    var comment = ""

    @SerializedName("canWebRestrictedEditing")
    @Expose
    var isCanWebRestrictedEditing = true

    @SerializedName("canFillForms")
    @Expose
    var isCanFillForms = true

    @SerializedName("denyDownload")
    @Expose
    var isDenyDownload = false

    @SerializedName("denySharing")
    @Expose
    var isDenySharing = false

    val nextVersion: Int
        get() = ++version

    fun setFile(file: CloudFile) {
        setItem(file)
        folderId = file.folderId
        version = file.version
        versionGroup = file.versionGroup
        contentLength = file.contentLength
        pureContentLength = file.pureContentLength
        fileStatus = file.fileStatus
        viewUrl = file.viewUrl
        webUrl = file.webUrl
        fileType = file.fileType
        fileExst = file.fileExst
        comment = file.comment
    }

    override fun clone(): CloudFile {
        return super.clone() as CloudFile
    }

    class SortFilesType(isSortAsc: Boolean) : BaseResponse.AbstractSort<CloudFile>(isSortAsc) {
        override fun compare(o1: CloudFile, o2: CloudFile): Int {
            return mSortOrder * o1.fileExst.compareTo(o2.fileExst)
        }
    }

    class SortFilesSize(isSortAsc: Boolean) : BaseResponse.AbstractSort<CloudFile>(isSortAsc) {
        override fun compare(o1: CloudFile, o2: CloudFile): Int {
            return mSortOrder * o1.contentLength.compareTo(o2.contentLength)
        }
    }
}