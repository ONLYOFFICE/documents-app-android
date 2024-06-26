package app.editors.manager.mvp.models.filter

import app.editors.manager.managers.utils.Storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Filter(
    var type: FilterType = FilterType.None,
    var author: FilterAuthor = FilterAuthor(),
    var excludeSubfolder: Boolean = false,
    var roomType: RoomFilterType = RoomFilterType.None,
    var tags: List<RoomFilterTag> = listOf(),
    var provider: Storage? = null
) {


    companion object {

        fun toJson(filter: Filter): String {
            return Json.encodeToString(filter)
        }

        fun toObject(json: String?): Filter {
            return Json.decodeFromString(json ?: return Filter())
        }
    }
}