package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestDelete(
    @SerializedName("deleteAfter")
    @Expose
    var deleteAfter: Boolean = false
)