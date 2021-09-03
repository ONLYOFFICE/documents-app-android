package app.editors.manager.mvp.models.models

import java.io.Serializable

data class State (
    var title: String = "",
    var info: String = "",
    var iconResId: Int = 0,
    var isFolder: Boolean = false,
    var isShared: Boolean = false,
    var isCanShare: Boolean = false,
    var isDocs: Boolean = false,
    var isStorage: Boolean = false,
    var isItemEditable: Boolean = false,
    var isContextEditable: Boolean = false,
    var isDeleteShare: Boolean = false,
    var isPdf: Boolean = false,
    var isLocal: Boolean = false,
    var isRecent: Boolean = false,
    var isWebDav: Boolean = false,
    var isTrash: Boolean = false,
    var isFavorite: Boolean = false,
    var isOneDrive: Boolean = false
) : Serializable