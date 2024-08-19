package app.editors.manager.mvp.models.filter

import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.ui.views.custom.ChipItem
import kotlinx.serialization.Serializable

enum class RoomFilterType(val title: Int, val filterVal: Int) : ChipItem {
    Custom(R.string.rooms_filter_type_custom, ApiContract.RoomType.CUSTOM_ROOM),
    Collaboration(R.string.rooms_filter_type_collaboration, ApiContract.RoomType.COLLABORATION_ROOM),
    Public(R.string.rooms_filter_type_public, ApiContract.RoomType.PUBLIC_ROOM),
    FormFilling(R.string.rooms_filter_type_filling_forms, ApiContract.RoomType.FILL_FORMS_ROOM),
    None(-1, -1);

    override val chipTitle: Int = title
    override val withOption: Boolean = false
    override var option: String? = null

    companion object {

        val allTypes: List<RoomFilterType>
            get() = entries.toMutableList().dropLast(1)

    }
}

@Serializable
data class RoomFilterTag(val value: String) : ChipItem {

    override val chipTitle: Int = -1
    override val withOption: Boolean = false
    override var option: String? = null
    override val chipTitleString: String
        get() = value
}

fun List<RoomFilterTag>.joinToString(): String {
    return "[${map(RoomFilterTag::value).joinToString { "\"$it\"" }}]"
}