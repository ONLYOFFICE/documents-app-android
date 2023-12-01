package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class RequestBatchBase {
    @SerializedName("folderIds")
    @Expose
    var folderIds: List<String> = emptyList()

    @SerializedName("fileIds")
    @Expose
    var fileIds: List<String> = emptyList()

    @SerializedName("deleteAfter")
    @Expose
    var isDeleteAfter = false

    @SerializedName("immediately")
    @Expose
    var isImmediately = false
}