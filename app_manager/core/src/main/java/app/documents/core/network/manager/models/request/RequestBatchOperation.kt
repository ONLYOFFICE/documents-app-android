package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestBatchOperation(
    @SerializedName("destFolderId")
    @Expose
    var destFolderId: String? = null,

    @SerializedName("conflictResolveType")
    @Expose
    var conflictResolveType: Int = 0
) : RequestBatchBase()