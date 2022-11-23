package app.documents.core.network.manager.models.explorer

import app.documents.core.network.manager.models.filter.CoreFilterType
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Explorer : Cloneable, Serializable {

    @SerializedName("files")
    @Expose
    var files: MutableList<CloudFile> = ArrayList()

    @SerializedName("folders")
    @Expose
    var folders: MutableList<CloudFolder> = ArrayList()

    @SerializedName("current")
    @Expose
    var current: Current? = Current()

    @SerializedName("pathParts")
    @Expose
    var pathParts: List<String> = ArrayList()

    @SerializedName("startIndex")
    @Expose
    var startIndex = ""

    @SerializedName("count")
    @Expose
    var count = 0

    @SerializedName("total")
    @Expose
    var total = 0

    var destFolderId = ""

    var filterType = CoreFilterType.None

    val itemsCount: Int get() = folders.size + files.size

    public override fun clone(): Explorer {
        return (super.clone() as Explorer).also { explorer ->
            explorer.current = current!!.clone()
            explorer.files = ArrayList(files)
            explorer.folders = ArrayList(folders)
            explorer.pathParts = ArrayList(pathParts)
        }
    }

    fun add(exp: Explorer): Explorer {
        folders.addAll(exp.folders)
        files.addAll(exp.files)
        current = exp.current
        return this
    }
}