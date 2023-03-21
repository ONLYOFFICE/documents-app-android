package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

class Operation : Cloneable, Serializable {

    @SerializedName("id")
    @Expose
    var id = ""

    @SerializedName("operation")
    @Expose
    var operation = 0

    @SerializedName("progress")
    @Expose
    var progress = 0

    @SerializedName("error")
    @Expose
    var error: String? = null

    @SerializedName("processed")
    @Expose
    var processed = ""

    @SerializedName("finished")
    @Expose
    var finished = false

    @SerializedName("url")
    @Expose
    var url = ""

    @SerializedName("files")
    @Expose
    var files: List<CloudFile> = ArrayList()

    @SerializedName("folders")
    @Expose
    var folders: List<CloudFolder> = ArrayList()

    val itemsCount: Int get() = folders.size + files.size

    public override fun clone(): Operation {
        return (super.clone() as Operation).also { operation ->
            operation.files = ArrayList(files)
            operation.folders = ArrayList(folders)
        }
    }
}