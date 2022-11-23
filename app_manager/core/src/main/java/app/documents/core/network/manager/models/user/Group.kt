package app.documents.core.network.manager.models.user

import app.documents.core.network.manager.models.base.ItemProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Group : ItemProperties(), Serializable, Comparable<Group> {

    @SerializedName("id")
    @Expose
    var id = ""

    @SerializedName("name")
    @Expose
    var name = ""

    @SerializedName("manager")
    @Expose
    var manager = ""

    override operator fun compareTo(other: Group): Int {
        return name.compareTo(other.name)
    }

}