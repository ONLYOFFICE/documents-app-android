package app.documents.core.network.manager.models.base

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Settings {
    @SerializedName("communityServer")
    @Expose
    var communityServer: String? = null

    @SerializedName("documentServer")
    @Expose
    var documentServer: String? = null

    @SerializedName("mailServer")
    @Expose
    var mailServer: String? = null
}