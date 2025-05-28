package app.editors.manager.mvp.models.list

import app.documents.core.network.manager.models.explorer.Item

object Templates : Item() {
    private fun readResolve(): Any = Templates
}