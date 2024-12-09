package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ExternalAccessRights {
    @SerializedName("Editing")
    @Expose
    var editing: Boolean = false

    @SerializedName("CustomFilter")
    @Expose
    var customFilter: Boolean = false

    @SerializedName("Review")
    @Expose
    var review: Boolean = false

    @SerializedName("Comment")
    @Expose
    var comment: Boolean = false

    @SerializedName("Read")
    @Expose
    var read: Boolean = false

    @SerializedName("Restrict")
    @Expose
    var restrict: Boolean = false

    @SerializedName("None")
    @Expose
    var none: Boolean = false
}
