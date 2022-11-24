package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Explorer(
    @SerializedName("files")
    @Expose
    var files: MutableList<CloudFile> = ArrayList(),

    @SerializedName("folders")
    @Expose
    var folders: MutableList<CloudFolder> = ArrayList(),

    @SerializedName("current")
    @Expose
    var current: Current = Current(),

    @SerializedName("pathParts")
    @Expose
    var pathParts: List<String> = ArrayList(),

    @SerializedName("startIndex")
    @Expose
    var startIndex: String = "",

    @SerializedName("count")
    @Expose
    var count: Int = 0,

    @SerializedName("total")
    @Expose
    var total: Int = 0,

    var destFolderId: String = "",
    var filterType: String = ""
) : Serializable {

    val itemsCount: Int get() = folders.size + files.size

    fun add(exp: Explorer): Explorer {
        folders.addAll(exp.folders)
        files.addAll(exp.files)
        current = exp.current
        return this
    }

    fun clone(): Explorer {
        return copy()
    }
}