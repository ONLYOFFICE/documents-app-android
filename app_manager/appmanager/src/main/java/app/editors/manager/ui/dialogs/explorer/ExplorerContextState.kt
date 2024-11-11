package app.editors.manager.ui.dialogs.explorer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import java.io.Serializable

data class ExplorerContextState(
    val headerIcon: ByteArray?,
    val item: Item,
    val sectionType: Int,
    val provider: PortalProvider = PortalProvider.Cloud.Workspace,
    val isSearching: Boolean = false,
    val isRoot: Boolean = false,
    val headerInfo: String? = null
) : Serializable {

    val section: ApiContract.Section
        get() = ApiContract.Section.getSection(sectionType)

    val access: ApiContract.Access
        get() = ApiContract.Access.get(item.intAccess)

    val pinned: Boolean
        get() = (item as? CloudFolder)?.pinned == true

    val isFolder: Boolean
        get() = item is CloudFolder

    val isStorageFolder: Boolean
        get() = item.providerItem

    val icon: Bitmap?
        get() = headerIcon?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExplorerContextState

        if (headerIcon != null) {
            if (other.headerIcon == null) return false
            if (!headerIcon.contentEquals(other.headerIcon)) return false
        } else if (other.headerIcon != null) return false
        if (item != other.item) return false
        if (sectionType != other.sectionType) return false
        if (provider != other.provider) return false
        if (isSearching != other.isSearching) return false
        if (isRoot != other.isRoot) return false
        if (headerInfo != other.headerInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headerIcon?.contentHashCode() ?: 0
        result = 31 * result + item.hashCode()
        result = 31 * result + sectionType
        result = 31 * result + provider.hashCode()
        result = 31 * result + isSearching.hashCode()
        result = 31 * result + isRoot.hashCode()
        result = 31 * result + (headerInfo?.hashCode() ?: 0)
        return result
    }

}