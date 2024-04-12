package app.editors.manager.ui.interfaces

import app.documents.core.network.manager.models.explorer.CloudFolder


interface WebDavInterface {
    val isMySection: Boolean
    val isTitleRequired: Boolean

    fun finishWithResult(folder: CloudFolder?)
}