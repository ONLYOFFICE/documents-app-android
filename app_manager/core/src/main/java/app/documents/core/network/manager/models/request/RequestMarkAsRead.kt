package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestMarkAsRead(
    @SerializedName("fileIds")
    @Expose
    var filesIds: List<String> = listOf(),

    @SerializedName("folderIds")
    @Expose
    var foldersIds: List<String> = listOf(),

    @SerializedName("returnSingleOperation")
    @Expose
    var returnSingleOperation: Boolean = true

)