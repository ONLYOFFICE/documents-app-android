package app.editors.manager.mvp.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Security: Serializable {

    @SerializedName("Read")
    @Expose
    var read: Boolean = false
    @SerializedName("Create")
    @Expose
    var create: Boolean = false
    @SerializedName("Delete")
    @Expose
    var delete: Boolean = false
    @SerializedName("EditRoom")
    @Expose
    var editRoom: Boolean = false
    @SerializedName("Rename")
    @Expose
    var rename: Boolean = false
    @SerializedName("CopyTo")
    @Expose
    var copyTo: Boolean = false
    @SerializedName("Copy")
    @Expose
    var copy: Boolean = false
    @SerializedName("MoveTo")
    @Expose
    var moveTo: Boolean = false
    @SerializedName("Move")
    @Expose
    var move: Boolean = false
    @SerializedName("Pin")
    @Expose
    var pin: Boolean = false
    @SerializedName("EditAccess")
    @Expose
    var editAccess: Boolean = false
    @SerializedName("Duplicate")
    @Expose
    var duplicate: Boolean = false
}
