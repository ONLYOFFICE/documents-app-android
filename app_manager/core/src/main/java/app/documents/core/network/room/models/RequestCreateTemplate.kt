package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateTemplate(
    val roomId: String,
    val title: String? = null,
    val tags: Array<String>? = null,
    val quota: Long? = null,
    val public: Boolean? = null,
    val copylogo: Boolean? = null,
    val color: String? = null,
    val logo: RequestSetLogo? = null,
    val share: Array<String>? = null,
    val groups: Array<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestCreateTemplate

        if (quota != other.quota) return false
        if (public != other.public) return false
        if (copylogo != other.copylogo) return false
        if (roomId != other.roomId) return false
        if (title != other.title) return false
        if (tags != null) {
            if (other.tags == null) return false
            if (!tags.contentEquals(other.tags)) return false
        } else if (other.tags != null) return false
        if (color != other.color) return false
        if (logo != other.logo) return false
        if (share != null) {
            if (other.share == null) return false
            if (!share.contentEquals(other.share)) return false
        } else if (other.share != null) return false
        if (groups != null) {
            if (other.groups == null) return false
            if (!groups.contentEquals(other.groups)) return false
        } else if (other.groups != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = quota?.hashCode() ?: 0
        result = 31 * result + (public?.hashCode() ?: 0)
        result = 31 * result + (copylogo?.hashCode() ?: 0)
        result = 31 * result + roomId.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + (logo?.hashCode() ?: 0)
        result = 31 * result + (share?.contentHashCode() ?: 0)
        result = 31 * result + (groups?.contentHashCode() ?: 0)
        return result
    }
}