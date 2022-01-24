package app.editors.manager.mvp.models.list

import lib.toolkit.base.ui.adapters.holder.ViewType
import app.editors.manager.R
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.ui.adapters.holders.factory.TypeFactory
import java.io.Serializable

data class Header(var title: String = "") : Serializable, Entity, ViewType {

    override fun getType(factory: TypeFactory): Int {
        return factory.type(this)
    }

    override val viewType: Int
        get() = R.layout.list_explorer_header
}