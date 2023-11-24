package app.documents.core.network.manager.models.explorer

import app.documents.core.network.common.contracts.ApiContract.ShareType.getCode
import app.documents.core.network.manager.models.base.ItemProperties
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.NumberFormatException
import java.util.*

open class Item : ItemProperties(), Serializable {

    @SerializedName("id")
    @Expose
    var id = ""

    @SerializedName("title")
    @Expose
    var title = ""

    @SerializedName("access")
    @Expose
    var access = ApiContract.ShareType.NONE

    @SerializedName("shared")
    @Expose
    var shared = false

    @SerializedName("rootFolderType")
    @Expose
    var rootFolderType = "-1"

    @SerializedName("updatedBy")
    @Expose
    var updatedBy = UpdatedBy()

    @SerializedName("created")
    @Expose
    var created = Date()

    @SerializedName("createdBy")
    @Expose
    var createdBy = CreatedBy()

    @SerializedName("updated")
    @Expose
    var updated = Date()

    @SerializedName("providerItem")
    @Expose
    var providerItem = false

    @SerializedName("security")
    @Expose
    var security = Security()

    @SerializedName("favorite")
    @Expose
    var favorite = false

    @SerializedName("canShare")
    @Expose
    var isCanShare = false

    @SerializedName("canEdit")
    @Expose
    var isCanEdit = false

    @SerializedName("etag")
    @Expose
    var etag = ""

    val intAccess: Int
        get() {
            val access = access
            return try {
                access.toInt()
            } catch (error: NumberFormatException) {
                getCode(access)
            }
        }

    fun setItem(item: Item) {
        id = item.id
        title = item.title
        access = item.access
        shared = item.shared
        rootFolderType = item.rootFolderType
        updatedBy = item.updatedBy
        updated = item.updated
        createdBy = item.createdBy
        created = item.created
        providerItem = item.providerItem
        favorite = item.favorite
    }

    class SortCreateDate(isSortAsc: Boolean) : BaseResponse.AbstractSort<Item>(isSortAsc) {
        override fun compare(o1: Item, o2: Item): Int {
            return mSortOrder * o1.created.compareTo(o2.created)
        }
    }

    class SortUpdateDate(isSortAsc: Boolean) : BaseResponse.AbstractSort<Item>(isSortAsc) {
        override fun compare(o1: Item, o2: Item): Int {
            return mSortOrder * o1.updated.compareTo(o2.updated)
        }
    }

    class SortTitle(isSortAsc: Boolean) : BaseResponse.AbstractSort<Item>(isSortAsc) {
        override fun compare(o1: Item, o2: Item): Int {
            return mSortOrder * o1.title.compareTo(o2.title)
        }
    }

    class SortOwner(isSortAsc: Boolean) : BaseResponse.AbstractSort<Item>(isSortAsc) {
        override fun compare(o1: Item, o2: Item): Int {
            return mSortOrder * o1.createdBy.displayName.compareTo(o2.createdBy.displayName)
        }
    }
}