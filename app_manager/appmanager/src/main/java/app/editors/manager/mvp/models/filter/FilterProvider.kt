package app.editors.manager.mvp.models.filter

import app.editors.manager.managers.utils.Storage
import app.editors.manager.ui.views.custom.ChipItem

data class FilterProvider(val storage: Storage) : ChipItem {

    override val chipTitle: Int = storage.title
    override val withOption: Boolean = false
    override var option: String? = null
}