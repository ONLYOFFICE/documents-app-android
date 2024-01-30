package app.documents.core.network.manager.models.explorer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.reflect.Type

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
    var pathParts: List<PathPart> = ArrayList(),

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

internal class PathPartTypeAdapter : JsonDeserializer<PathPart> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PathPart {
        return if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            PathPart(id = jsonObject["id"].asString, title = jsonObject["title"].asString)
        } else {
            PathPart(id = json.asString)
        }
    }
}