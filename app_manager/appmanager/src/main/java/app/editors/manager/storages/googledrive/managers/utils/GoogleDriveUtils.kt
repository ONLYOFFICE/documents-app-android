package app.editors.manager.storages.googledrive.managers.utils

import app.documents.core.network.ApiContract
import lib.toolkit.base.managers.utils.StringUtils

object GoogleDriveUtils {
    const val GOOGLE_DRIVE_NEXT_PAGE_TOKEN = "pageToken"
    const val GOOGLE_DRIVE_FIELDS = "fields"
    const val GOOGLE_DRIVE_FIELDS_VALUES = "nextPageToken, files/id, files/name, files/mimeType, files/description, files/parents, files/webViewLink, files/webContentLink, files/modifiedTime, files/createdTime, files/capabilities/canDelete, files/size"
    const val GOOGLE_DRIVE_SORT = "orderBy"
    const val GOOGLE_DRIVE_QUERY = "q"
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

    fun getSortBy(filter: Map<String, String>?): String {
        return when(filter?.get(ApiContract.Parameters.ARG_SORT_BY)) {
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