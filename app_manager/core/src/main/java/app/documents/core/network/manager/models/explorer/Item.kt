package app.documents.core.network.manager.models.explorer

import app.documents.core.model.cloud.Access
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.base.ItemProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

open class Item : ItemProperties(), Serializable {

    @SerializedName("id")
    @Expose
    var id = ""

    @SerializedName("title")
    @Expose
    var title = ""

    @SerializedName("access")
    @Expose
    private var _access = Access.None.type

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
    var security :Security? = null

    @SerializedName("canShare")
    @Expose
    var isCanShare = false

    @SerializedName("canEdit")
    @Expose
    var isCanEdit = false

    @SerializedName("etag")
    @Expose
    var etag = ""

    @SerializedName("order")
    @Expose
    var order: String = ""

    var index: Int
        get() = order.split(".").lastOrNull()?.toInt() ?: 0
        set(value) {
            val indices = order.split(".").toMutableList()
            indices[indices.lastIndex] = value.toString()
            order = if (indices.size > 1) indices.joinToString(".") else indices[0]
        }

    val access: Access
        get() = runCatching {
            Access.get(_access.toInt())
        }.getOrElse {
            Access.get(_access)
        }

    fun setItem(item: Item) {
        id = item.id
        title = item.title
        _access = item._access
        shared = item.shared
        rootFolderType = item.rootFolderType
        updatedBy = item.updatedBy
        updated = item.updated
        createdBy = item.createdBy
        created = item.created
        providerItem = item.providerItem
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

val Item?.isFavorite: Boolean
    get() = (this as? CloudFile)?.isFavorite == true