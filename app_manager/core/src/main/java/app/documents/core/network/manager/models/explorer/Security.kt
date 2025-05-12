package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Security : Serializable {

    @SerializedName("Read")
    @Expose
    var read: Boolean = false

    @SerializedName("Create")
    @Expose
    var create: Boolean = false

    @SerializedName("Delete")
    @Expose
    var delete: Boolean = false

    @SerializedName("CustomFilter")
    @Expose
    var customFilter: Boolean = false

    @SerializedName("EditRoom")
    @Expose
    var editRoom: Boolean = false

    @SerializedName("Rename")
    @Expose
    var rename: Boolean = false

    @SerializedName("Lock")
    @Expose
    var lock: Boolean = false

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

    @SerializedName("Edit")
    @Expose
    var edit: Boolean = false

    @SerializedName("ReadHistory")
    @Expose
    var readHistory: Boolean = false

    @SerializedName("EditHistory")
    @Expose
    var editHistory: Boolean = false

    @SerializedName("Duplicate")
    @Expose
    var duplicate: Boolean = false

    @SerializedName("Mute")
    @Expose
    var mute: Boolean = false

    @SerializedName("Download")
    @Expose
    var download: Boolean = false

    @SerializedName("CopySharedLink")
    @Expose
    var copySharedLink: Boolean = false

    @SerializedName("Reconnect")
    @Expose
    var reconnect: Boolean = false

    @SerializedName("CreateRoomFrom")
    @Expose
    var createRoomFrom: Boolean = false

    @SerializedName("CopyLink")
    @Expose
    var copyLink: Boolean = false

    @SerializedName("Embed")
    @Expose
    var embed: Boolean = false

    @SerializedName("ChangeOwner")
    @Expose
    var changeOwner: Boolean = false

    @SerializedName("IndexExport")
    @Expose
    var indexExport: Boolean = false

    @SerializedName("FillForms")
    @Expose
    var fillForms: Boolean = false
}