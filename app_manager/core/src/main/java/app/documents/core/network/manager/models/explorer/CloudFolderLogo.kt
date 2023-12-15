package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CloudFolderLogo(
    @SerializedName("original")
    @Expose val original: String? = null,
    @SerializedName("large")
    @Expose val large: String? = null,
    @SerializedName("medium")
    @Expose val medium: String? = null,
    @SerializedName("small")
    @Expose val small: String? = null,
    @SerializedName("color")
    @Expose val color: String? = null
) : Serializable