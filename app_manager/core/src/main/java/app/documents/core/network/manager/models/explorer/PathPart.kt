package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PathPart(

    @SerializedName("id")
    @Expose
    val id: String = "",

    @SerializedName("title")
    @Expose
    val title: String = ""
): Serializable