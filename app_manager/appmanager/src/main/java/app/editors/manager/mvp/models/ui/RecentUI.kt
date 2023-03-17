package app.editors.manager.mvp.models.ui

import app.documents.core.storage.recent.Recent
import app.editors.manager.R
import lib.toolkit.base.ui.adapters.holder.ViewType

data class RecentUI(
    val id: Int,
    val idFile: String?,
    val path: String?,
    val name: String,
    val date: Long,
    val isLocal: Boolean,
    val isWebDav: Boolean,
    val size: Long,
    val ownerId: String? = null,
    val source: String? = null
) : ViewType {

    override val viewType: Int
        get() = R.layout.list_explorer_files
}

fun RecentUI.toRecent(): Recent {
    return Recent(
        id = id,
        idFile = idFile,
        path = path,
        name = name,
        date = date,
        isLocal = isLocal,
        isWebDav = isWebDav,
        size = size,
        ownerId = ownerId,
        source = source
    )
}

fun Recent.toRecentUI(): RecentUI {
    return RecentUI(
        id = id,
        idFile = idFile,
        path = path,
        name = name,
        date = date,
        isLocal = isLocal,
        isWebDav = isWebDav,
        size = size,
        ownerId = ownerId,
        source = source
    )
}