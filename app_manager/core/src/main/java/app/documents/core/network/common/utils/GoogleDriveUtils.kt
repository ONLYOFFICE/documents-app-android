package app.documents.core.network.common.utils

import app.documents.core.BuildConfig
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.Storage
import lib.toolkit.base.managers.utils.StringUtils

object GoogleDriveUtils {
    const val GOOGLE_DRIVE_NEXT_PAGE_TOKEN = "pageToken"
    const val GOOGLE_DRIVE_PAGE_SIZE = "pageSize"
    const val GOOGLE_DRIVE_FOLDER = "folder"
    const val GOOGLE_DRIVE_FIELDS = "fields"
    const val GOOGLE_DRIVE_SORT = "orderBy"
    const val GOOGLE_DRIVE_QUERY = "q"
    const val GOOGLE_DRIVE_FIELDS_VALUES = "files/id, files/name, files/mimeType, files/description, files/parents, " +
            "files/webViewLink, files/webContentLink, files/modifiedTime, files/createdTime, " +
            "files/capabilities/canDelete, files/size, nextPageToken"

    private const val VAL_SORT_ASC = ""
    private const val VAL_SORT_DESC = " desc"
    private const val VAL_SORT_NAME = "name"
    private const val VAL_SORT_UPDATED = "modifiedTime"
    const val MOVE_ADD_PARENTS = "addParents"
    const val MOVE_REMOVE_PARENTS = "removeParents"


    const val FOLDER_MIMETYPE = "application/vnd.google-apps.folder"
    private const val DOC_MIMETYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    private const val SLIDES_MIMETYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    private const val CELLS_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    const val DEFAULT_PAGE_SIZE = 25

    val storage: Storage get() = Storage(
        ApiContract.Storage.GOOGLEDRIVE,
        BuildConfig.GOOGLE_COM_CLIENT_ID,
        BuildConfig.GOOGLE_COM_REDIRECT_URL
    )

    val webId: String get() = BuildConfig.GOOGLE_WEB_ID

    fun getSortBy(filter: Map<String, String>?): String {
        return "$GOOGLE_DRIVE_FOLDER, " + when(filter?.get(ApiContract.Parameters.ARG_SORT_BY)) {
            ApiContract.Parameters.VAL_SORT_BY_TITLE -> VAL_SORT_NAME + getSortOrder(filter)
            ApiContract.Parameters.VAL_SORT_BY_UPDATED -> VAL_SORT_UPDATED + getSortOrder(filter)
            else -> VAL_SORT_NAME + getSortOrder(filter)
        }
    }

    private fun getSortOrder(filter: Map<String, String>?): String {
        return when(filter?.get(ApiContract.Parameters.ARG_SORT_ORDER)) {
            ApiContract.Parameters.VAL_SORT_ORDER_ASC -> VAL_SORT_ASC
            ApiContract.Parameters.VAL_SORT_ORDER_DESC -> VAL_SORT_DESC
            else -> VAL_SORT_ASC
        }
    }

    fun getFileMimeType(ext: String): String {
        return when (StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC -> {
                DOC_MIMETYPE
            }
            StringUtils.Extension.SHEET -> {
                CELLS_MIMETYPE
            }
            StringUtils.Extension.PRESENTATION -> {
                SLIDES_MIMETYPE
            }
            else -> FOLDER_MIMETYPE
        }
    }

}