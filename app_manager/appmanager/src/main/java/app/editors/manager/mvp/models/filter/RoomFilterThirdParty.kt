package app.editors.manager.mvp.models.filter

import app.editors.manager.R
import app.editors.manager.ui.views.custom.ChipItem

enum class RoomFilterThirdParty(val title: Int, val filterVal: String) : ChipItem {
    Dropbox(R.string.storage_select_drop_box, ""),
    GoogleDrive(R.string.storage_select_google_drive, ""),
    OneDrive(R.string.storage_select_one_drive, ""),
    Box(R.string.storage_select_box, "");

    override val chipTitle: Int = title
    override val withOption: Boolean = false
    override var option: Any? = null

    companion object {

        val allTypes: List<RoomFilterThirdParty>
            get() = values().toList()

    }
}