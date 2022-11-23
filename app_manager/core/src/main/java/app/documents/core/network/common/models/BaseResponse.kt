package app.documents.core.network.common.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.lang.Error
import java.util.Comparator

open class BaseResponse {

    @SerializedName(KEY_COUNT)
    @Expose
    var count = 0

    @SerializedName(KEY_STATUS)
    @Expose
    var status = ""

    @SerializedName(KEY_STATUS_CODE)
    @Expose
    var statusCode = ""

    @SerializedName(KEY_ERROR)
    @Expose
    var error = Error()

    abstract class AbstractSort<Type>(isSortAsc: Boolean) : Comparator<Type> {
        protected var mSortOrder = SORT_ORDER_ASC

        init {
            mSortOrder = if (isSortAsc) SORT_ORDER_ASC else SORT_ORDER_DESC
        }

        companion object {
            const val SORT_ORDER_ASC = 1
            const val SORT_ORDER_DESC = -1
        }
    }

    companion object {
        const val KEY_COUNT = "count"
        const val KEY_STATUS = "status"
        const val KEY_STATUS_CODE = "statusCode"
        const val KEY_ERROR = "error"
        const val KEY_RESPONSE = "response"
    }
}