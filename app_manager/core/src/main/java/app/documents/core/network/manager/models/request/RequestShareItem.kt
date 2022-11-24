package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestShareItem(
    @SerializedName("ShareTo")
    @Expose
    var shareTo: String? = null,

    @SerializedName("Access")
    @Expose
    var access: String? = null
)