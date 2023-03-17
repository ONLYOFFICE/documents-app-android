package app.editors.manager.ui.interfaces

import app.documents.core.network.manager.models.explorer.CloudFolder


interface WebDavInterface {
    val isMySection: Boolean

    fun finishWithResult(folder: CloudFolder?)
    fun showConnectButton(isShow: Boolean)
    fun enableConnectButton(isEnable: Boolean)
    fun setOnConnectButtonClickListener(onClick: () -> Unit)
}