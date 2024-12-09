package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Lifetime {

    @SerializedName("deletePermanently")
    @Expose
    var deletePermanently: Boolean = false

    @SerializedName("period")
    @Expose
    var period: Int = 0

    @SerializedName("value")
    @Expose
    var value: String = ""
}