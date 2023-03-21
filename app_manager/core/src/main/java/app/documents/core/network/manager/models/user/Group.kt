package app.documents.core.network.manager.models.user

import app.documents.core.network.manager.models.base.ItemProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("id")
    @Expose
    var id: String = "",

    @SerializedName("name")
    @Expose
    var name: String = "",

    @SerializedName("manager")
    @Expose
    var manager: String = ""
) : ItemProperties()