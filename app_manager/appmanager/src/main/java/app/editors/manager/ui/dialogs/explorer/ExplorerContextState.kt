package app.editors.manager.ui.dialogs.explorer

import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import java.io.Serializable

data class ExplorerContextState(
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

}