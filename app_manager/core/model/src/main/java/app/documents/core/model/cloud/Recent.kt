package app.documents.core.model.cloud

import java.util.Date

data class Recent(
    val id: Int = 0,
    val fileId: String = "",
    val path: String = "",
    val name: String = "",
    val date: Long = Date().time,
    val size: Long = 0,
    val ownerId: String? = null, // null if local
    val source: String? = null, // null if local
    val isWebdav: Boolean = false,
    val token: String? = null
) {

    val isLocal: Boolean
        get() = source == null
}