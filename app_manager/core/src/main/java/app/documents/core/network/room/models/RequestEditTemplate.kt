package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestEditTemplate(
    val title: String? = null,

    val quota: Long? = null,

    val tags: Array<String>? = null,

    val logo: RequestSetLogo? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestEditTemplate

        if (quota != other.quota) return false
        if (title != other.title) return false
        if (tags != null) {
            if (other.tags == null) return false
            if (!tags.contentEquals(other.tags)) return false
        } else if (other.tags != null) return false
        if (logo != other.logo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = quota?.hashCode() ?: 0
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        result = 31 * result + (logo?.hashCode() ?: 0)
        return result
    }
}
