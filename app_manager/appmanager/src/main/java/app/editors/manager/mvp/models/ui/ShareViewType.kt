package app.editors.manager.mvp.models.ui

import lib.toolkit.base.ui.adapters.holder.ViewType

interface ShareViewType : ViewType {

    val itemId: String
    val itemName: String
}