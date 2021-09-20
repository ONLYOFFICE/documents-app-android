package app.editors.manager.mvp.models.states

import app.editors.manager.mvp.models.explorer.Explorer
import java.util.*
import javax.inject.Inject

/*
* TODO replace with mapping to SQLite in perspective
 * */
class OperationsState @Inject constructor() {

    enum class OperationType {
        NONE, MOVE, COPY, INSERT
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