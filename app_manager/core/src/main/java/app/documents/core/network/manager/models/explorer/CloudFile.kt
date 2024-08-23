package app.documents.core.network.manager.models.explorer

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
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
    var fileStatus = 0

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

    @SerializedName("encrypted")
    @Expose
    var encrypted = false

    val nextVersion: Int
        get() = ++version

    val clearExt: String
        get() = fileExst.replace(".", "")

    val isFavorite: Boolean
        get() = (fileStatus and ApiContract.FileStatus.FAVORITE) != 0

    val isNew: Boolean
        get() = (fileStatus and ApiContract.FileStatus.IS_NEW) != 0

    val isEditing: Boolean
        get() = (fileStatus and ApiContract.FileStatus.IS_EDITING) != 0

    private fun String.toIntOrZero(): Int {
        return if (isNotEmpty()) toInt() else 0
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

val CloudFile.allowShare: Boolean
    get() = run {
        if (isDenySharing) return@run false
        if (intAccess in listOf(
                ApiContract.ShareCode.RESTRICT,
                ApiContract.ShareCode.VARIES,
                ApiContract.ShareCode.REVIEW,
                ApiContract.ShareCode.COMMENT,
                ApiContract.ShareCode.FILL_FORMS
            )
        ) {
            return false
        }
        return@run true
    }