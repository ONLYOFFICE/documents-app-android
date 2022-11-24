package app.documents.core.network.manager.models.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
class Contact {
    @SerializedName("type")
    @Expose
    var type = ""

    @SerializedName("value")
    @Expose
    var value = ""
}