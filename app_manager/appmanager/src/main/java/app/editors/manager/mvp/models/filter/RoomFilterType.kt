package app.editors.manager.mvp.models.filter

import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.ui.views.custom.ChipItem

enum class RoomFilterType(val title: Int, val filterVal: Int) : ChipItem {
    Custom(R.string.rooms_filter_type_custom, ApiContract.RoomType.CUSTOM_ROOM),
    FillingForms(R.string.rooms_filter_type_filling_forms, ApiContract.RoomType.FILLING_FORM_ROOM),
    Collaboration(R.string.rooms_filter_type_collaboration, ApiContract.RoomType.EDITING_ROOM),
    Review(R.string.rooms_filter_type_review, ApiContract.RoomType.REVIEW_ROOM),
    ViewOnly(R.string.rooms_filter_type_view_only, ApiContract.RoomType.READ_ONLY_ROOM),
    None(-1, -1);

    override val chipTitle: Int = title
    override val withOption: Boolean = false
    override var option: Any? = null

    companion object {

        val allTypes: List<RoomFilterType>
            get() = values().toMutableList().dropLast(1)

    }
}