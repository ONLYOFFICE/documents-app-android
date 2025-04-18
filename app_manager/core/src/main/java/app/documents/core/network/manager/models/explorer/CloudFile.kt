package app.documents.core.network.manager.models.explorer

import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import lib.toolkit.base.managers.utils.StringUtils.Extension
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import java.time.Duration
import java.time.Instant
import java.util.Date

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

    @SerializedName("locked")
    @Expose
    var isLocked = false

    @SerializedName("formFillingStatus")
    @Expose
    var formFillingStatusType = 0

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

    @SerializedName("availableExternalRights")
    @Expose
    var availableExternalRights: ExternalAccessRights? = null

    @SerializedName("isForm")
    @Expose
    var isForm: Boolean = false

    @SerializedName("expired")
    val expired: Date? = null

    val nextVersion: Int
        get() = ++version

    val clearExt: String
        get() = fileExst.replace(".", "")

    val isPdfForm: Boolean
        get() = getExtension(fileExst) == Extension.PDF && isForm

    val isFavorite: Boolean
        get() = (fileStatus and ApiContract.FileStatus.FAVORITE) != 0

    val isNew: Boolean
        get() = (fileStatus and ApiContract.FileStatus.IS_NEW) != 0

    val isEditing: Boolean
        get() = (fileStatus and ApiContract.FileStatus.IS_EDITING) != 0

    val isExpiringSoon: Boolean
        get() {
            if (expired == null) return false
            val totalDuration = Duration.between(expired.toInstant(), created.toInstant()).abs()
            val timePassed =  Duration.between(Instant.now(), created.toInstant()).abs()
            return timePassed.toMillis() >= totalDuration.toMillis() * 0.9
        }

    val formFillingStatus: ApiContract.FormFillingStatus
        get() = ApiContract.FormFillingStatus.fromType(formFillingStatusType)

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
    get() = access in listOf(Access.Editor, Access.Read) && !isDenySharing