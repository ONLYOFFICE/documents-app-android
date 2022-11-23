/*
 * Created by Michael Efremov on 05.10.20 16:45
 */
package app.documents.core.network.manager.models.base

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Download {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("operation")
    @Expose
    var operation: Int? = null

    @SerializedName("progress")
    @Expose
    var progress: Int? = null

    @SerializedName("error")
    @Expose
    var error: String? = null

    @SerializedName("processed")
    @Expose
    var processed: String? = null

    @SerializedName("finished")
    @Expose
    var finished: Boolean? = null

    @SerializedName("url")
    @Expose
    var url: String? = null
}