package app.documents.core.network.share.models

import kotlinx.serialization.Serializable
import lib.toolkit.base.managers.utils.TimeUtils

@Serializable
data class ExternalLinkSharedTo(
    val id: String,
    val title: String,
    val shareLink: String,
    val linkType: Int,
    val internal: Boolean? = null,
    val password: String?,
    val denyDownload: Boolean,
    val isExpired: Boolean,
    val primary: Boolean,
    val requestToken: String,
    val expirationDate: String?
) {

    private fun dateToLong(): Long {
        expirationDate?.let { date ->
            return TimeUtils.DEFAULT_FORMAT.parse(date)?.time ?: 0
        } ?: return 0
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + shareLink.hashCode()
        result = 31 * result + linkType
        result = 31 * result + (password?.hashCode() ?: 0)
        result = 31 * result + denyDownload.hashCode()
        result = 31 * result + isExpired.hashCode()
        result = 31 * result + primary.hashCode()
        result = 31 * result + requestToken.hashCode()
        result = 31 * result + dateToLong().toInt()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExternalLinkSharedTo

        if (id != other.id) return false
        if (title != other.title) return false
        if (shareLink != other.shareLink) return false
        if (linkType != other.linkType) return false
        if (password != other.password) return false
        if (denyDownload != other.denyDownload) return false
        if (isExpired != other.isExpired) return false
        if (primary != other.primary) return false
        if (requestToken != other.requestToken) return false
        return dateToLong().toInt() == other.dateToLong().toInt()
    }
}