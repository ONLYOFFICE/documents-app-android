package app.editors.manager.mvp.models.list

import app.documents.core.network.manager.models.base.Entity
import lib.toolkit.base.ui.adapters.holder.ViewType
import app.editors.manager.R
import java.io.Serializable

data class Header(var title: String = "") : Entity, Serializable, ViewType {

    override val viewType: Int
        get() = R.layout.list_explorer_header
}