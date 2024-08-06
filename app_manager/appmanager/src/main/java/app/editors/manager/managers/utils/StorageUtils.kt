package app.editors.manager.managers.utils

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.contracts.StorageContract
import app.editors.manager.R
import kotlinx.serialization.Serializable
import lib.toolkit.base.managers.utils.StringUtils.getEncodedString
import java.util.TreeMap

@Serializable
sealed class Storage(
    val providerKey: String?,
    val icon: Int,
    val iconLarge: Int,
    val title: Int,
    val filterValue: String
) {

    override fun hashCode(): Int {
        return providerKey?.hashCode() ?: super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Storage

        return providerKey == other.providerKey
    }

    @Serializable
    data object Box : Storage(
        providerKey = ApiContract.Storage.BOXNET,
        icon = R.drawable.ic_storage_box,
        iconLarge = R.drawable.ic_storage_box_logo,
        title = R.string.storage_select_box,
        filterValue = "Box"
    )

    @Serializable
    data object Dropbox : Storage(
        providerKey = ApiContract.Storage.DROPBOX,
        icon = R.drawable.ic_storage_dropbox,
        iconLarge = R.drawable.ic_storage_dropbox_logo,
        title = R.string.storage_select_drop_box,
        filterValue = "DropBox"
    )

    @Serializable
    data object GoogleDrive : Storage(
        providerKey = ApiContract.Storage.GOOGLEDRIVE,
        icon = R.drawable.ic_storage_google,
        iconLarge = R.drawable.ic_storage_googledrive_logo,
        title = R.string.storage_select_google_drive,
        filterValue = "GoogleDrive"
    )

    @Serializable
    data object OneDrive : Storage(
        providerKey = ApiContract.Storage.ONEDRIVE,
        icon = R.drawable.ic_storage_onedrive,
        iconLarge = R.drawable.ic_storage_onedrive_logo,
        title = R.string.storage_select_one_drive,
        filterValue = "OneDrive"
    )

    @Serializable
    data object SharePoint : Storage(
        providerKey = ApiContract.Storage.SHAREPOINT,
        icon = R.drawable.ic_storage_sharepoint,
        iconLarge = R.drawable.ic_storage_sharepoint_logo,
        title = R.string.storage_select_share_point,
        filterValue = "SharePoint"
    )

    @Serializable
    data object Yandex : Storage(
        providerKey = ApiContract.Storage.YANDEX,
        icon = R.drawable.ic_storage_yandex,
        iconLarge = R.drawable.ic_storage_yandex_disk_logo,
        title = R.string.storage_select_yandex,
        filterValue = "Yandex"
    )

    @Serializable
    data object OwnCloud : Storage(
        providerKey = ApiContract.Storage.OWNCLOUD,
        icon = R.drawable.ic_storage_owncloud,
        iconLarge = R.drawable.ic_storage_owncloud_logo,
        title = R.string.storage_select_own_cloud,
        filterValue = "OwnCloud"
    )

    @Serializable
    data object Nextcloud : Storage(
        providerKey = ApiContract.Storage.NEXTCLOUD,
        icon = R.drawable.ic_storage_nextcloud,
        iconLarge = R.drawable.ic_storage_nextcloud_logo,
        title = R.string.storage_select_next_cloud,
        filterValue = "NextCloud"
    )

    @Serializable
    data object KDrive : Storage(
        providerKey = ApiContract.Storage.KDRIVE,
        icon = R.drawable.ic_storage_kdrive,
        iconLarge = R.drawable.ic_storage_kdrive_logo,
        title = R.string.storage_select_kdrive,
        filterValue = "kDrive"
    )

    @Serializable
    data object WebDav : Storage(
        providerKey = ApiContract.Storage.WEBDAV,
        icon = R.drawable.ic_storage_webdav,
        iconLarge = R.drawable.ic_storage_webdav_text_logo,
        title = R.string.storage_select_web_dav,
        filterValue = "WebDav"
    )

    companion object {

        fun get(providerKey: String?): Storage? {
            return when (providerKey) {
                ApiContract.Storage.BOXNET -> Box
                ApiContract.Storage.DROPBOX -> Dropbox
                ApiContract.Storage.GOOGLEDRIVE -> GoogleDrive
                ApiContract.Storage.ONEDRIVE -> OneDrive
                ApiContract.Storage.SHAREPOINT -> SharePoint
                ApiContract.Storage.YANDEX -> Yandex
                ApiContract.Storage.OWNCLOUD -> OwnCloud
                ApiContract.Storage.NEXTCLOUD -> Nextcloud
                ApiContract.Storage.KDRIVE -> KDrive
                ApiContract.Storage.WEBDAV -> WebDav
                else -> null
            }
        }
    }
}

object StorageUtils {

    fun getStorageUrl(providerKey: String?, clientId: String?, redirectUrl: String?): String? {
        if (clientId == null || redirectUrl == null) {
            return null
        }

        return when (providerKey) {
            ApiContract.Storage.BOXNET -> getBoxUrl(clientId, redirectUrl)
            ApiContract.Storage.DROPBOX -> getDropboxUrl(clientId, redirectUrl)
            ApiContract.Storage.GOOGLEDRIVE -> getGoogleDriveUrl(clientId, redirectUrl)
            ApiContract.Storage.ONEDRIVE -> getOneDriveUrl(clientId, redirectUrl)
            else -> null
        }
    }

    fun getStorageTitle(providerKey: String?): Int? {
        return Storage.get(providerKey)?.title
    }

    fun getStorageIcon(providerKey: String?): Int {
        return Storage.get(providerKey)?.icon ?: R.drawable.ic_type_folder
    }

    fun getStorageIconLarge(providerKey: String?): Int? {
        return Storage.get(providerKey)?.iconLarge
    }

    fun getStorageFilterValue(providerKey: String?): String {
        return Storage.get(providerKey)?.filterValue.orEmpty()
    }

    /*
     * Get Box instance for request token
     * */
    private fun getBoxUrl(clientId: String, redirectUrl: String): String {
        val uriMap = TreeMap<String, String>()
        uriMap[StorageContract.ARG_RESPONSE_TYPE] = StorageContract.Box.VALUE_RESPONSE_TYPE
        uriMap[StorageContract.ARG_CLIENT_ID] = clientId
        uriMap[StorageContract.ARG_REDIRECT_URI] = redirectUrl
        return getUrl(StorageContract.Box.AUTH_URL, uriMap)
    }

    /*
     * Get DropBox instance for request token
     * */
    private fun getDropboxUrl(clientId: String, redirectUrl: String): String {
        val uriMap = TreeMap<String, String>()
        uriMap[StorageContract.ARG_RESPONSE_TYPE] = StorageContract.DropBox.VALUE_RESPONSE_TYPE
        uriMap[StorageContract.ARG_CLIENT_ID] = clientId
        uriMap[StorageContract.ARG_REDIRECT_URI] = redirectUrl
        uriMap[StorageContract.ARG_TOKEN_ACCESS] = StorageContract.DropBox.VALUE_ACCESS_TYPE
        return getUrl(StorageContract.DropBox.AUTH_URL, uriMap)
    }

    /*
     * Get Google instance for request token
     * */
    private fun getGoogleDriveUrl(clientId: String, redirectUrl: String): String {
        val uriMap = TreeMap<String, String>()
        uriMap[StorageContract.ARG_APPROVAL_PROMPT] = StorageContract.Google.VALUE_APPROVAL_PROMPT
        uriMap[StorageContract.ARG_RESPONSE_TYPE] = StorageContract.Google.VALUE_RESPONSE_TYPE
        uriMap[StorageContract.ARG_ACCESS_TYPE] = StorageContract.Google.VALUE_ACCESS_TYPE
        uriMap[StorageContract.ARG_SCOPE] = StorageContract.Google.VALUE_SCOPE
        uriMap[StorageContract.ARG_CLIENT_ID] = clientId
        uriMap[StorageContract.ARG_REDIRECT_URI] = redirectUrl
        return getUrl(StorageContract.Google.AUTH_URL, uriMap)
    }

    /*
     * Get OneDrive instance for request token
     * */
    private fun getOneDriveUrl(clientId: String, redirectUrl: String): String {
        val uriMap = TreeMap<String, String>()
        uriMap[StorageContract.ARG_CLIENT_ID] = clientId
        uriMap[StorageContract.ARG_REDIRECT_URI] = redirectUrl
        uriMap[StorageContract.ARG_RESPONSE_TYPE] = StorageContract.OneDrive.VALUE_RESPONSE_TYPE
        uriMap[StorageContract.ARG_SCOPE] = StorageContract.OneDrive.VALUE_SCOPE
        return getUrl(StorageContract.OneDrive.AUTH_URL, uriMap)
    }

    private fun getUrl(
        requestUrl: String,
        requestArgs: TreeMap<String, String>
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(requestUrl)
        for ((key, value) in requestArgs) {
            stringBuilder.append(key)
                .append("=")
                .append(value)
                .append("&")
        }
        stringBuilder.deleteCharAt(stringBuilder.length - 1)
        return getEncodedString(stringBuilder.toString()).orEmpty()
    }
}
