package app.editors.manager.mvp.models.list

import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.Item

object RecentViaLink : Item(), Entity {
    private fun readResolve(): Any = RecentViaLink
}