package app.editors.manager.mvp.models.filter

import app.editors.manager.managers.utils.Storage
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val type: FilterType = FilterType.None,
    val author: FilterAuthor = FilterAuthor(),
    val excludeSubfolder: Boolean = false,
    val roomType: RoomFilterType = RoomFilterType.None,
    val tags: List<RoomFilterTag> = listOf(),
    val provider: Storage? = null,
    val location: Int = 0
)