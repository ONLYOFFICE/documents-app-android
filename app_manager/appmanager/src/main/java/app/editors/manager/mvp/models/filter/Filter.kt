package app.editors.manager.mvp.models.filter

import com.google.gson.Gson

data class Filter(
    var type: FilterType = FilterType.None,
    var author: FilterAuthor = FilterAuthor(),
    var excludeSubfolder: Boolean = false,
    var roomType: RoomFilterType = RoomFilterType.None,
    var tag: RoomFilterTag? = null
) {

    companion object {
        fun toJson(filter: Filter): String {
            val gson = Gson()
            return gson.toJson(filter)
        }

        fun toObject(json: String?): Filter {
            val gson = Gson()
            return gson.fromJson(json, Filter::class.java)
        }
    }
}