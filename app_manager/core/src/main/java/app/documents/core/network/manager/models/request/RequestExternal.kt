package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestExternal(
    @SerializedName("share")
    @Expose
    var share: String? = null
)