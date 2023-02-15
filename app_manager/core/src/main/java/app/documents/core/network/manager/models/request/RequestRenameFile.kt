package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestRenameFile(
    @SerializedName("title")
    @Expose
    var title: String? = null,

    @SerializedName("lastVersion")
    @Expose
    var lastVersion: Int = 0
)