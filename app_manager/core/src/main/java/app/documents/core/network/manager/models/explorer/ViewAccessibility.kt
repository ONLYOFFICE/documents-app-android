package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ViewAccessibility(
    @SerializedName("WebCustomFilterEditing")
    @Expose
    val webCustomFilterEditing: Boolean = false
)
