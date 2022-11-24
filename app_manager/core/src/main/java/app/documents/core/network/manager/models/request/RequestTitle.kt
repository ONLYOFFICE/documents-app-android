package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestTitle(
    @SerializedName("title")
    @Expose
    var title: String? = null
)