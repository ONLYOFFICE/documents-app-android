package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateRoomFromTemplate(
    val templateId: String,
    val title: String,
    val tags: Array<String>? = null,
    val quota: Long? = null,
    val copylogo: Boolean? = null,
    val color: String? = null,
    val logo: RequestSetLogo? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestCreateRoomFromTemplate

        if (quota != other.quota) return false
        if (copylogo != other.copylogo) return false
        if (templateId != other.templateId) return false
        if (title != other.title) return false
        if (tags != null) {
            if (other.tags == null) return false
            if (!tags.contentEquals(other.tags)) return false
        } else if (other.tags != null) return false
        if (color != other.color) return false
        if (logo != other.logo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = quota?.hashCode() ?: 0
        result = 31 * result + (copylogo?.hashCode() ?: 0)
        result = 31 * result + templateId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + (logo?.hashCode() ?: 0)
        return result
    }
}
