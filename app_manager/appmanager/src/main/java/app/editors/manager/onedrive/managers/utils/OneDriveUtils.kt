package app.editors.manager.onedrive.managers.utils

import app.documents.core.network.ApiContract
import com.google.android.gms.common.api.Api

object OneDriveUtils {

    private const val KEY_SORT = "orderby"
    const val ONEDRIVE_PORTAL = "onedrive.live.com"
    const val ONEDRIVE_STORAGE = "OneDrive"
    private const val VAL_SORT_NAME = "name"
    private const val VAL_SORT_SIZE = "size"
    private const val VAL_SORT_UPDATED = "lastModifiedDateTime"
    private const val VAL_SORT_ASC = "asc"
    private const val VAL_SORT_DESC = "desc"

    const val KEY_CONFLICT_BEHAVIOR = "@microsoft.graph.conflictBehavior"
    const val VAL_CONFLICT_BEHAVIOR_RENAME = "rename"
    const val VAL_CONFLICT_BEHAVIOR_REPLACE = "replace"
    const val VAL_CONFLICT_BEHAVIOR_FAIL = "fail"

    fun getSortBy(filter: MutableMap<String, String>?): Map<String, String> {
        val resultMap = mutableMapOf(KEY_SORT to "")
        when(filter?.get(ApiContract.Parameters.ARG_SORT_BY)) {
            ApiContract.Parameters.VAL_SORT_BY_TITLE -> resultMap[KEY_SORT] = VAL_SORT_NAME + " ${getSortOrder(filter)}"
            ApiContract.Parameters.VAL_SORT_BY_SIZE -> resultMap[KEY_SORT] = VAL_SORT_SIZE + " ${getSortOrder(filter)}"
            ApiContract.Parameters.VAL_SORT_BY_UPDATED -> resultMap[KEY_SORT] = VAL_SORT_UPDATED + " ${getSortOrder(filter)}"
        }
        return resultMap
    }

    private fun getSortOrder(filter: MutableMap<String, String>?): String {
        return when(filter?.get(ApiContract.Parameters.ARG_SORT_ORDER)) {
            ApiContract.Parameters.VAL_SORT_ORDER_ASC -> VAL_SORT_ASC
            ApiContract.Parameters.VAL_SORT_ORDER_DESC -> VAL_SORT_DESC
            else -> VAL_SORT_ASC
        }
    }

}