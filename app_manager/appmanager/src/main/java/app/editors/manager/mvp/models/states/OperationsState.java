package app.editors.manager.mvp.models.states;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import app.editors.manager.mvp.models.explorer.Explorer;

/*
* TODO replace with mapping to SQLite in perspective
 * */
public class OperationsState {

    public enum OperationType {
        NONE, MOVE, COPY, INSERT
    }

    public static class Operation {
        public final OperationType mOperationType;
        public final Explorer mExplorer;

        public Operation(OperationType operationType, Explorer explorer) {
            mOperationType = operationType;
            mExplorer = explorer;
        }
    }

    protected final TreeMap<Integer, LinkedList<Operation>> mOperations;

    public OperationsState() {
        mOperations = new TreeMap<>();
    }

    public List<Operation> getOperations(final int section) {
        LinkedList<Operation> operations = mOperations.get(section);
        if (operations == null) {
            operations = new LinkedList<>();
            mOperations.put(section, operations);
        }

        return operations;
    }

    public List<Operation> getOperations(final int section, @Nullable final String folderId) {
        final List<Operation> operations = getOperations(section);
        final List<Operation> sectionOperations = new ArrayList<>();

        for (Operation item : operations) {
            if (item.mExplorer.getDestFolderId().equalsIgnoreCase(folderId)) {
                sectionOperations.add(item);
            }
        }

        operations.removeAll(sectionOperations);
        return sectionOperations;
    }

    public void insert(final int section, @NonNull Explorer explorer) {
        getOperations(section).add(new Operation(OperationType.INSERT, explorer));
    }

}
