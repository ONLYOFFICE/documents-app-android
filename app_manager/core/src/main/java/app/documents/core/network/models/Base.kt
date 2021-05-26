package app.documents.core.network.models

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
open class Base (val count: Int? = 0,
                 val status: String = "",
                 val statusCode: String = ""){

    /*
    * Comparators
    * */
    abstract class AbstractSort<Type>(isSortAsc: Boolean) : Comparator<Type> {
        protected var mSortOrder = SORT_ORDER_ASC

        companion object {
            const val SORT_ORDER_ASC = 1
            const val SORT_ORDER_DESC = -1
        }

        init {
            mSortOrder = if (isSortAsc) SORT_ORDER_ASC else SORT_ORDER_DESC
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