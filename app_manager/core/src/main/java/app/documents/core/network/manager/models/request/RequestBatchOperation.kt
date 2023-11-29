package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestBatchOperation(
    @SerializedName("destFolderId")
    @Expose
    var destFolderId: String = "",

    @SerializedName("conflictResolveType")
    @Expose
    var conflictResolveType: Int = 2,

    @SerializedName("content")
    @Expose
    var content: Boolean = true,

) : RequestBatchBase()