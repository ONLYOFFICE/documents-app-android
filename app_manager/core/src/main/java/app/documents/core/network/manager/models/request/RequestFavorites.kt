package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestFavorites(
    @SerializedName("fileIds")
    @Expose
    var fileIds: List<String> = emptyList()
)