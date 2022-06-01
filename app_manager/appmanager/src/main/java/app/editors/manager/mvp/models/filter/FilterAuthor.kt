package app.editors.manager.mvp.models.filter

import com.google.gson.Gson

data class FilterAuthor(
    val id: String = "",
    val name: String = "",
    val isGroup: Boolean = false
) {

    companion object {
        fun toJson(author: FilterAuthor): String {
            val gson = Gson()
            return gson.toJson(author)
        }

        fun toObject(json: String?): FilterAuthor {
            val gson = Gson()
            return gson.fromJson(json, FilterAuthor::class.java)
        }
    }
}