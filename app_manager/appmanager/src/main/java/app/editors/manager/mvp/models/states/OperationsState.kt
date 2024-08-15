package app.editors.manager.mvp.models.states

import app.documents.core.network.manager.models.explorer.Explorer
import java.util.LinkedList
import java.util.TreeMap
import javax.inject.Inject

/*
* TODO replace with mapping to SQLite in perspective
 * */
class OperationsState @Inject constructor() {

    enum class OperationType {
        NONE, MOVE, COPY, INSERT, RESTORE, DELETE, PICK_PDF_FORM
    }

    class Operation(val operationType: OperationType, val explorer: Explorer)

    private val operations: TreeMap<Int, LinkedList<Operation>> = TreeMap()

    private fun getOperations(section: Int): MutableList<Operation> {
        var operations = operations[section]
        if (operations == null) {
            operations = LinkedList()
            this.operations[section] = operations
        }
        return operations
    }

    fun getOperations(section: Int, folderId: String?): List<Operation> {
        val operations = getOperations(section)
        val sectionOperations: MutableList<Operation> = ArrayList()
        for (item in operations) {
            if (item.explorer.destFolderId.equals(folderId, ignoreCase = true)) {
                sectionOperations.add(item)
            }
        }
        operations.removeAll(sectionOperations)
        return sectionOperations
    }

    fun insert(section: Int, explorer: Explorer) {
        getOperations(section).add(Operation(OperationType.INSERT, explorer))
    }

}