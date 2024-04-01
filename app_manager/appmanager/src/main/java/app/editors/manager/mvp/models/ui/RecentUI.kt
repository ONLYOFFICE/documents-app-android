package app.editors.manager.mvp.models.ui

import app.documents.core.model.cloud.Recent
import app.editors.manager.R
import lib.toolkit.base.ui.adapters.holder.ViewType

data class RecentUI(
    val id: Int,
    val idFile: String = "",
    val path: String = "",
    val name: String = "",
    val date: Long = 0,
    val size: Long = 0,
    val ownerId: String? = null,
    val source: String? = null
) : ViewType {

    override val viewType: Int
        get() = R.layout.list_explorer_files
}

fun RecentUI.toRecent(): Recent {
    return Recent(
        id = id,
        fileId = idFile,
        path = path,
        name = name,
        date = date,
        size = size,
        ownerId = ownerId,
        source = source
    )
}

fun Recent.toRecentUI(): RecentUI {
    return RecentUI(
        id = id,
        idFile = fileId,
        path = path,
        name = name,
        date = date,
        size = size,
        ownerId = ownerId,
        source = source
    )
}