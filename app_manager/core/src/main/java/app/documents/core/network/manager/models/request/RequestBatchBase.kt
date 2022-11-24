package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class RequestBatchBase {
    @SerializedName("folderIds")
    @Expose
    var folderIds: List<String>? = null

    @SerializedName("fileIds")
    @Expose
    var fileIds: List<String>? = null

    @SerializedName("deleteAfter")
    @Expose
    var isDeleteAfter = false
}