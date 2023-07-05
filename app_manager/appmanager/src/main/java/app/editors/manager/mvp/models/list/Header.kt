package app.editors.manager.mvp.models.list

import app.documents.core.network.manager.models.base.Entity
import lib.toolkit.base.ui.adapters.holder.ViewType
import java.io.Serializable

data class Header(var title: String = "") : Entity, Serializable, ViewType {

    override val viewType: Int
        get() = lib.toolkit.base.R.layout.list_item_header
}