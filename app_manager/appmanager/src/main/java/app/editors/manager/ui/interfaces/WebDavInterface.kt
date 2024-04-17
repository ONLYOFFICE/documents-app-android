package app.editors.manager.ui.interfaces

import app.documents.core.network.manager.models.explorer.CloudFolder


interface WebDavInterface {
    val isMySection: Boolean
    val isRoomStorage: Boolean
    val title: String?
    val providerId: Int

    fun finishWithResult(folder: CloudFolder?)
}