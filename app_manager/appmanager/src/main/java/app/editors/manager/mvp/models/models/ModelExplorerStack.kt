package app.editors.manager.mvp.models.models

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import java.util.LinkedList

class ModelExplorerStack {
    private val navigationStack: LinkedList<ExplorerStack> = LinkedList()
    private val filterStack: LinkedList<ExplorerStack> = LinkedList()
    private var currentListPosition = 0

    fun last(): Explorer? {
        return last?.explorer
    }

    fun previous(): Explorer? {
        return previous?.explorer
    }

    fun popToRoot(): Explorer? {
        return navigationStack.apply {
            val first = navigationStack.first
            clear()
            add(first)
        }.last?.explorer
    }

    fun addStack(explorer: Explorer?) {
        val explorerStack: ExplorerStack = ExplorerStackMap(explorer)

        // First check for filtering
        if (!filterStack.isEmpty() && explorer != null) {
            val currentId = filterStack.last.currentId
            if (currentId.equals(explorer.current.id, ignoreCase = true)) {
                filterStack[filterStack.size - 1] = explorerStack
            } else {
                filterStack.add(explorerStack)
            }
            return
        }

        // If filtering is empty check navigation stack
        if (!navigationStack.isEmpty() && explorer != null) {
            val currentId = navigationStack.last.currentId
            if (currentId.equals(explorer.current.id, ignoreCase = true)) {
                navigationStack[navigationStack.size - 1] = explorerStack
                return
            }
        }
        navigationStack.add(explorerStack)
    }

    fun addOnNext(explorer: Explorer?) {
        if (!filterStack.isEmpty()) {
            filterStack.last.addExplorer(explorer)
            return
        }
        if (!navigationStack.isEmpty()) {
            navigationStack.last.addExplorer(explorer)
            return
        }
        navigationStack.add(ExplorerStackMap(explorer))
    }

    fun refreshStack(explorer: Explorer) {
        checkSelected(explorer)
        if (!filterStack.isEmpty()) {
            filterStack.last.explorer = explorer
            return
        }
        if (!navigationStack.isEmpty()) {
            navigationStack.last.explorer = explorer
            return
        }
    }

    fun setFilter(explorer: Explorer?) {
        filterStack.clear()
        filterStack.add(ExplorerStackMap(explorer))
    }

    val isRoot: Boolean
        get() = if (!filterStack.isEmpty()) {
            false
        } else navigationStack.size < 2

    val isNavigationRoot: Boolean
        get() = navigationStack.size < 2

    val isStackEmpty: Boolean
        get() = navigationStack.isEmpty()

    val isStackFilter: Boolean
        get() = !filterStack.isEmpty()

    val totalCount: Int
        get()  = last?.countItems ?: 0

    val isListEmpty: Boolean
        get() = totalCount == 0

    fun setSelection(isSelect: Boolean): Int {
        return last?.setSelectionAll(isSelect) ?: 0
    }

    val selectedFoldersIds: List<String>
        get() = last?.selectedFoldersIds ?: emptyList()

    val selectedFilesIds: List<String>
        get() = last?.selectedFilesIds ?: emptyList()

    val selectedFiles: List<CloudFile>
        get() = last?.selectedFiles ?: emptyList()

    val selectedFolders: List<CloudFolder>
        get() = last?.selectedFolders ?: emptyList()

    fun addFile(file: CloudFile?) {
        val explorerStack = last
        explorerStack?.addFile(file)
    }

    fun addFileFirst(file: CloudFile?) {
        val explorerStack = last
        explorerStack?.addFileFirst(file)
    }

    fun addFolder(folder: CloudFolder?) {
        val explorerStack = last
        explorerStack?.addFolder(folder)
    }

    fun addFolderFirst(folder: CloudFolder?) {
        val explorerStack = last
        explorerStack?.addFolderFirst(folder)
    }

    fun removeItemById(id: String?): Boolean {
        val explorerStack = last
        return explorerStack?.removeItemById(id) ?: false
    }

    fun removeSelected(): Int {
        val explorerStack = last
        return explorerStack?.removeSelected() ?: 0
    }

    fun removeUnselected(): Int {
        val explorerStack = last
        return explorerStack?.removeUnselected() ?: 0
    }

    fun getItemById(item: Item?): Item? {
        val explorerStack = last
        return explorerStack?.getItemById(item)
    }

    fun setSelectById(item: Item?, isSelect: Boolean): Item? {
        val explorerStack = last
        return explorerStack?.setItemSelectedById(item, isSelect)
    }

    val countSelectedItems: Int
        get() = last?.selectedItems ?: 0

    var listPosition: Int
        get() = currentListPosition
        set(position) {
            val explorerStack = last
            if (explorerStack != null) {
                currentListPosition = position
                explorerStack.listPosition = position
            }
        }

    val currentTitle: String
        get() = last?.currentTitle ?: ""

    val rootId: String?
        get() = last?.rootId

    val rootFolderType: Int
        get() = last?.rootFolderType ?: ApiContract.SectionType.UNKNOWN

    val currentFolderAccess: Int
        get() = last?.currentFolderAccess ?: ApiContract.SectionType.UNKNOWN

    val currentFolderOwnerId: String?
        get() = last?.currentFolderOwnerId

    val currentId: String?
        get() = last?.currentId

    val path: List<String>
        get()  = last?.path ?: emptyList()

    val loadPosition: Int
        get() = last?.loadPosition ?: -1

    private val last: ExplorerStack?
        get() {
            if (!filterStack.isEmpty()) {
                val explorerStack = filterStack.last
                currentListPosition = explorerStack.listPosition
                return explorerStack
            }
            if (!navigationStack.isEmpty()) {
                val explorerStack = navigationStack.last
                currentListPosition = explorerStack.listPosition
                return explorerStack
            }
            return null
        }

    private val previous: ExplorerStack?
        get() {
            if (!filterStack.isEmpty()) {
                filterStack.removeLast()
                return last
            }
            if (!navigationStack.isEmpty()) {
                navigationStack.removeLast()
                if (!navigationStack.isEmpty()) {
                    return last
                }
            }
            return null
        }

    fun clone(): ExplorerStack? {
        if (!filterStack.isEmpty()) {
            return filterStack.last.clone()
        }
        return if (!navigationStack.isEmpty()) {
            navigationStack.last.clone()
        } else null
    }

    fun clear() {
        filterStack.clear()
        navigationStack.clear()
    }

    private fun checkSelected(explorer: Explorer) {
        val selectedFiles: MutableList<String> = ArrayList()
        val selectedFolders: MutableList<String> = ArrayList()
        if (!filterStack.isEmpty()) {
            selectedFiles.addAll(filterStack.last.selectedFilesIds)
            selectedFolders.addAll(filterStack.last.selectedFoldersIds)
        } else if (!navigationStack.isEmpty()) {
            selectedFiles.addAll(navigationStack.last.selectedFilesIds)
            selectedFolders.addAll(navigationStack.last.selectedFoldersIds)
        }
        if (!filterStack.isEmpty() && filterStack.last.selectedItems > 0) {
            for (file in explorer.files) {
                if (selectedFiles.contains(file.id)) {
                    file.isSelected = true
                }
            }
            for (folder in explorer.folders) {
                if (selectedFolders.contains(folder.id)) {
                    folder.isSelected = true
                }
            }
            return
        }
        if (!navigationStack.isEmpty() && navigationStack.last.selectedItems > 0) {
            for (file in explorer.files) {
                if (selectedFiles.contains(file.id)) {
                    file.isSelected = true
                }
            }
            for (folder in explorer.folders) {
                if (selectedFolders.contains(folder.id)) {
                    folder.isSelected = true
                }
            }
        }
    }

}