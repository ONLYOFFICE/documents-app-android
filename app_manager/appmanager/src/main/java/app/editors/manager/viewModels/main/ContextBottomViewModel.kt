package app.editors.manager.ui.dialogs

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

class ContextBottomViewModelFactory(
    private val item: Item,
    private val section: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContextBottomViewModel(item, section) as T
    }

}

sealed class ContextItems {
    class Header(@DrawableRes val icon: Int, val title: String, val info: String) : ContextItems()
    object Rename : ContextItems()
    object Move : ContextItems()
    object Copy : ContextItems()
    object Download : ContextItems()
    object Upload : ContextItems()
    object Share : ContextItems()
    object InternalShare : ContextItems()
    object Archive : ContextItems()
    object Disconnect : ContextItems()
    class Pin(val isPinned: Boolean) : ContextItems()
    class Favorites(val isFavorite: Boolean) : ContextItems()
    object Restore : ContextItems()
    class Delete(@StringRes val title: Int? = null) : ContextItems()
}

class ContextBottomViewModel(
    private val item: Item,
    private val section: Int
) : ViewModel() {

    val state: StateFlow<List<ContextItems>> = MutableStateFlow(getState())

    private val account: CloudAccount?
        get() = runBlocking { App.getApp().accountOnline }

    private fun getState(): List<ContextItems> {
        val items = ArrayList<ContextItems>()
        items.add(
            ContextItems.Header(
                getIconContext(),
                item.title,
                TimeUtils.formatDate(item.updated)
            )
        )
        when {
            item is CloudFolder && item.isRoom -> {
                items.addAll(getRoomElements(item))
            }
            section == ApiContract.SectionType.CLOUD_RECENT -> {
                items.addAll(getRecentElements())
            }
            section == ApiContract.SectionType.CLOUD_TRASH -> {
                items.addAll(getTrashElements())
            }
            section == ApiContract.SectionType.DEVICE_DOCUMENTS -> {
                items.addAll(getDeviceElements())
            }
            section == ApiContract.SectionType.WEB_DAV -> {
                items.addAll(getWebDavElements())
            }
            else -> {
                items.addAll(getCloudElements())
            }

        }
        return items
    }

    private fun getCloudElements(): Collection<ContextItems> {
        val items = ArrayList<ContextItems>()
        items.add(ContextItems.Copy)
        if (account?.isPersonal() == true) {
            items.addAll(getPersonalElements())
            return items
        }
        if (item.isCanShare) {
            if (item.shared) {
                items.add(ContextItems.InternalShare)
            }
            items.add(ContextItems.Share)
        }
        if (item.isCanEdit) {
            items.add(ContextItems.Rename)
            items.add(ContextItems.Move)
        }
        if (item is CloudFile) {
            items.add(ContextItems.Favorites(item.favorite))
        }
        items.add(ContextItems.Rename)
        return items
    }

    private fun getPersonalElements(): Collection<ContextItems> {
        return listOf(

        )
    }

    private fun getWebDavElements(): Collection<ContextItems> {
        val items = ArrayList<ContextItems>()
        items.add(ContextItems.Move)
        items.add(ContextItems.Copy)
        items.add(ContextItems.Rename)
        if (item is CloudFile) {
            items.add(ContextItems.Download)

        }
        return items
    }



    private fun getDeviceElements(): Collection<ContextItems> {
        val items = ArrayList<ContextItems>()
        items.add(ContextItems.Move)
        items.add(ContextItems.Copy)
        items.add(ContextItems.Rename)
        account?.let {
            if (item !is CloudFolder || !it.isDropbox || !it.isGoogleDrive || !it.isOneDrive || !it.isVisitor) {
                items.add(ContextItems.Upload)
            }
        }
        items.add(ContextItems.Delete())
        return items
    }

    private fun getTrashElements(): Collection<ContextItems> {
        return arrayListOf(
            ContextItems.Restore,
            ContextItems.Delete()
        )
    }

    private fun getRecentElements(): List<ContextItems> {
       return arrayListOf(
           ContextItems.Delete(R.string.list_context_delete_recent)
       )
    }

    private fun getRoomElements(item: CloudFolder): List<ContextItems> {
        if (item.rootFolderType.toInt() == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM) {
            return arrayListOf(
                ContextItems.Restore,
                ContextItems.Delete()
            )
        } else {
            return arrayListOf(
                ContextItems.Rename,
                ContextItems.Pin(item.pinned),
                ContextItems.Archive
            )
        }
    }


    private fun getIconContext(): Int {
        if (item is CloudFolder) {
            return if (item.providerKey.isEmpty()) {
                if (item.shared) {
                    R.drawable.ic_type_folder_shared
                } else {
                    if (item.isRoom) {
                        ManagerUiUtils.getFolderIcon(item)
                    } else {
                        R.drawable.ic_type_folder
                    }
                }
            } else {
                StorageUtils.getStorageIcon(item.providerKey)
            }
        } else {
            return when (StringUtils.getExtension((item as CloudFile).fileExst)) {
                StringUtils.Extension.DOC -> R.drawable.ic_type_text_document
                StringUtils.Extension.SHEET -> R.drawable.ic_type_spreadsheet
                StringUtils.Extension.PRESENTATION -> R.drawable.ic_type_presentation
                StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> R.drawable.ic_type_image
                StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.PDF -> R.drawable.ic_type_pdf
                StringUtils.Extension.VIDEO_SUPPORT -> R.drawable.ic_type_video
                StringUtils.Extension.UNKNOWN -> R.drawable.ic_type_file
                else -> R.drawable.ic_type_folder
            }
        }
    }

}
