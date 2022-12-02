package app.documents.core.network.manager.models.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    @SerializedName("type")
    @Expose
    var type: String = "",

    @SerializedName("value")
    @Expose
    var value: String = ""
)