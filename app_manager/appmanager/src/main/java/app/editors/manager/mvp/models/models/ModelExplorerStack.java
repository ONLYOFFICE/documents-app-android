package app.editors.manager.mvp.models.models;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import app.editors.manager.app.Api;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;

public class ModelExplorerStack {

    private LinkedList<ExplorerStack> mNavigationStack;
    private LinkedList<ExplorerStack> mFilterStack;
    private int mListPosition;

    public ModelExplorerStack() {
        mNavigationStack = new LinkedList<>();
        mFilterStack = new LinkedList<>();
    }

    @Nullable
    public Explorer last() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getExplorer();
        }

        return null;
    }

    @Nullable
    public Explorer previous() {
        final ExplorerStack explorerStack = getPrevious();
        if (explorerStack != null) {
            return explorerStack.getExplorer();
        }

        return null;
    }

    public void addStack(final Explorer explorer) {
        final ExplorerStack explorerStack = new ExplorerStackMap(explorer);

        // First check for filtering
        if (!mFilterStack.isEmpty()) {
            final String currentId = mFilterStack.getLast().getCurrentId();
            if (currentId.equalsIgnoreCase(explorer.getCurrent().getId())) {
                mFilterStack.set(mFilterStack.size() - 1, explorerStack);
            } else {
                mFilterStack.add(explorerStack);
            }
            return;
        }

        // If filtering is empty check navigation stack
        if (!mNavigationStack.isEmpty()) {
            final String currentId = mNavigationStack.getLast().getCurrentId();
            if (currentId.equalsIgnoreCase(explorer.getCurrent().getId())) {
                mNavigationStack.set(mNavigationStack.size() - 1, explorerStack);
                return;
            }
        }

        mNavigationStack.add(explorerStack);
    }

    public void addOnNext(final Explorer explorer) {
        if (!mFilterStack.isEmpty()) {
            mFilterStack.getLast().addExplorer(explorer);
            return;
        }

        if (!mNavigationStack.isEmpty()) {
            mNavigationStack.getLast().addExplorer(explorer);
            return;
        }

        mNavigationStack.add(new ExplorerStackMap(explorer));
    }

    public void refreshStack(final Explorer explorer) {
        checkSelected(explorer);
        if (!mFilterStack.isEmpty()) {
            mFilterStack.getLast().setExplorer(explorer);
            return;
        }

        if (!mNavigationStack.isEmpty()) {
            mNavigationStack.getLast().setExplorer(explorer);
            return;
        }
    }

    public void setFilter(final Explorer explorer) {
        mFilterStack.clear();
        mFilterStack.add(new ExplorerStackMap(explorer));
    }

    public boolean isRoot() {
        if (!mFilterStack.isEmpty()) {
            return false;
        }

        return mNavigationStack.size() < 2;
    }

    public boolean isNavigationRoot() {
        return mNavigationStack.size() < 2;
    }

    public boolean isStackEmpty() {
        return mNavigationStack.isEmpty();
    }

    public boolean isStackFilter() {
        return !mFilterStack.isEmpty();
    }

    public int getTotalCount() {
        int count = 0;
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            count = explorerStack.getCountItems();
        }

        return count;
    }

    public boolean isListEmpty() {
        return getTotalCount() == 0;
    }

    public void setListPosition(final int position) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            mListPosition = position;
            explorerStack.setListPosition(position);
        }
    }

    public int setSelection(final boolean isSelect) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.setSelectionAll(isSelect);
        }
        return 0;
    }

    @Nullable
    public List<String> getSelectedFoldersIds() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getSelectedFoldersIds();
        }
        return null;
    }

    @Nullable
    public List<String> getSelectedFilesIds() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getSelectedFilesIds();
        }
        return null;
    }

    public List<File> getSelectedFiles() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getSelectedFiles();
        }
        return new ArrayList<>();
    }

    public List<Folder> getSelectedFolders() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getSelectedFolders();
        }
        return new ArrayList<>();
    }

    public void addFile(final File file) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            explorerStack.addFile(file);
        }
    }

    public void addFileFirst(final File file) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            explorerStack.addFileFirst(file);
        }
    }

    public void addFolder(final Folder folder) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            explorerStack.addFolder(folder);
        }
    }

    public void addFolderFirst(final Folder folder) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            explorerStack.addFolderFirst(folder);
        }
    }

    public boolean removeItemById(final String id) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.removeItemById(id);
        }

        return false;
    }

    public int removeSelected() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.removeSelected();
        }

        return 0;
    }

    public int removeUnselected() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.removeUnselected();
        }

        return 0;
    }

    @Nullable
    public Item getItemById(final Item item) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getItemById(item);
        }

        return null;
    }

    @Nullable
    public Item setSelectById(final Item item, final boolean isSelect) {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.setItemSelectedById(item, isSelect);
        }

        return null;
    }

    public int getCountSelectedItems() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getSelectedItems();
        }

        return 0;
    }

    public int getListPosition() {
        return mListPosition;
    }

    @Nullable
    public String getCurrentTitle() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getCurrentTitle();
        }

        return null;
    }

    @Nullable
    public String getRootId() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getRootId();
        }

        return null;
    }

    public int getRootFolderType() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getRootFolderType();
        }

        return Api.SectionType.UNKNOWN;
    }

    public int getCurrentFolderAccess() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getCurrentFolderAccess();
        }

        return Api.SectionType.UNKNOWN;
    }

    @Nullable
    public String getCurrentFolderOwnerId() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getCurrentFolderOwnerId();
        }

        return null;
    }

    @Nullable
    public String getCurrentId() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getCurrentId();
        }

        return null;
    }

    @Nullable
    public List<String> getPath() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getPath();
        }

        return null;
    }

    public int getLoadPosition() {
        final ExplorerStack explorerStack = getLast();
        if (explorerStack != null) {
            return explorerStack.getLoadPosition();
        }

        return -1;
    }

    @Nullable
    private ExplorerStack getLast() {
        if (!mFilterStack.isEmpty()) {
            final ExplorerStack explorerStack = mFilterStack.getLast();
            mListPosition = explorerStack.getListPosition();
            return explorerStack;
        }

        if (!mNavigationStack.isEmpty()) {
            final ExplorerStack explorerStack = mNavigationStack.getLast();
            mListPosition = explorerStack.getListPosition();
            return explorerStack;
        }

        return null;
    }

    @Nullable
    private ExplorerStack getPrevious() {
        if (!mFilterStack.isEmpty()) {
            mFilterStack.removeLast();
            return getLast();
        }

        if (!mNavigationStack.isEmpty()) {
            mNavigationStack.removeLast();
            if (!mNavigationStack.isEmpty()) {
                return getLast();
            }
        }

        return null;
    }

    @Nullable
    public ExplorerStack clone() {
        if (!mFilterStack.isEmpty()) {
            return mFilterStack.getLast().clone();
        }

        if (!mNavigationStack.isEmpty()) {
            return mNavigationStack.getLast().clone();
        }

        return null;
    }

    public void clear() {
        mFilterStack.clear();
        mNavigationStack.clear();
    }

    private void checkSelected(Explorer explorer) {
        final List<String> selectedFiles = new ArrayList<>();
        final List<String> selectedFolders = new ArrayList<>();

        if (!mFilterStack.isEmpty()) {
            selectedFiles.addAll(mFilterStack.getLast().getSelectedFilesIds());
            selectedFolders.addAll(mFilterStack.getLast().getSelectedFoldersIds());
        } else if (!mNavigationStack.isEmpty()) {
            selectedFiles.addAll(mNavigationStack.getLast().getSelectedFilesIds());
            selectedFolders.addAll(mNavigationStack.getLast().getSelectedFoldersIds());
        }

        if (!mFilterStack.isEmpty() && mFilterStack.getLast().getSelectedItems() > 0) {
            for (File file: explorer.getFiles()) {
                if (selectedFiles.contains(file.getId())) {
                    file.setSelected(true);
                }
            }
            for (Folder folder: explorer.getFolders()) {
                if (selectedFolders.contains(folder.getId())) {
                    folder.setSelected(true);
                }
            }
            return;
        }

        if (!mNavigationStack.isEmpty() && mNavigationStack.getLast().getSelectedItems() > 0) {
            for (File file: explorer.getFiles()) {
                if (selectedFiles.contains(file.getId())) {
                    file.setSelected(true);
                }
            }
            for (Folder folder: explorer.getFolders()) {
                if (selectedFolders.contains(folder.getId())) {
                    folder.setSelected(true);
                }
            }
        }
    }

}
