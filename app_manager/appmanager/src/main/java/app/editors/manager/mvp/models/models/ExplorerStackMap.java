package app.editors.manager.mvp.models.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;


public class ExplorerStackMap implements ExplorerStack {

    private static final int INDEX_ROOT = 0;

    private LinkedHashMap<String, Folder> mFolderMap;
    private LinkedHashMap<String, File> mFileMap;
    private Explorer mExplorer;
    private int mListPosition;
    private int mLoadPosition;
    private int mSelectedItems;

    public ExplorerStackMap(Explorer explorer) {
        setExplorer(explorer);
    }

    @Override
    public void setExplorer(Explorer explorer) {
        mFolderMap = new LinkedHashMap<>();
        mFileMap = new LinkedHashMap<>();
        mExplorer = explorer;
        mListPosition = 0;
        mLoadPosition = explorer.getItemsCount();
        addFolders(explorer.getFolders());
        addFiles(explorer.getFiles());
        countSelected();
    }

    @Override
    public void addExplorer(final Explorer explorer) {
        mExplorer = explorer;
        mLoadPosition += mExplorer.getItemsCount();
        addFolders(explorer.getFolders());
        addFiles(explorer.getFiles());
    }

    @Nullable
    @Override
    public Explorer getExplorer() {
        mExplorer.setFolders(getFolders());
        mExplorer.setFiles(getFiles());
        mExplorer.setCount(mExplorer.getItemsCount());
        return mExplorer;
    }

    @Override
    public void addFile(final File newFile) {
        final File oldFile = mFileMap.get(newFile.getId());
        if (oldFile != null) {
            newFile.setSelected(oldFile.isSelected());
            newFile.setJustCreated(oldFile.isJustCreated());
            newFile.setReadOnly(oldFile.isReadOnly());
        } else {
            mSelectedItems += newFile.isSelected() ? 1 : 0;
        }

        mFileMap.put(newFile.getId(), newFile);
    }

    @Override
    public void addFileFirst(final File newFile) {
        mSelectedItems += newFile.isSelected() ? 1 : 0;
        if (mFileMap.size() > 0) {
            mFileMap.remove(newFile.getId());
            final LinkedHashMap<String, File> fileMap = new LinkedHashMap<>();
            fileMap.put(newFile.getId(), newFile);
            fileMap.putAll(mFileMap);
            mFileMap = fileMap;
        } else {
            mFileMap.put(newFile.getId(), newFile);
        }
    }

    @Override
    public void addFiles(final List<File> files) {
        for (File file : files) {
            addFile(file);
        }
    }

    @Nullable
    @Override
    public List<File> getFiles() {
        return new ArrayList<>(mFileMap.values());
    }

    @Override
    public List<String> getSelectedFilesIds() {
        final List<String> listId = new ArrayList<>();
        for (File item : getFiles()) {
            if (item.isSelected()) {
                listId.add(item.getId());
            }
        }

        return listId;
    }

    @Override
    public List<File> getSelectedFiles() {
        final List<File> files = new ArrayList<>();
        for (File file : getFiles()) {
            if (file.isSelected()) {
                files.add(file);
            }
        }
        return files;
    }

    @Override
    public List<Folder> getSelectedFolders() {
        final List<Folder> folders = new ArrayList<>();
        for (Folder folder : getFolders()) {
            if (folder.isSelected()) {
                folders.add(folder);
            }
        }
        return folders;
    }

    @Override
    public void addFolder(final Folder newFolder) {
        final Folder oldFolder = mFolderMap.get(newFolder.getId());
        if (oldFolder != null) {
            newFolder.setSelected(oldFolder.isSelected());
            newFolder.setJustCreated(oldFolder.isJustCreated());
            newFolder.setReadOnly(oldFolder.isReadOnly());
        } else {
            mSelectedItems += newFolder.isSelected() ? 1 : 0;
        }

        mFolderMap.put(newFolder.getId(), newFolder);
    }

    @Override
    public void addFolderFirst(final Folder newFolder) {
        mSelectedItems += newFolder.isSelected() ? 1 : 0;
        if (mFolderMap.size() > 0) {
            mFolderMap.remove(newFolder.getId());
            final LinkedHashMap<String, Folder> fileMap = new LinkedHashMap<>();
            fileMap.put(newFolder.getId(), newFolder);
            fileMap.putAll(mFolderMap);
            mFolderMap = fileMap;
        } else {
            mFolderMap.put(newFolder.getId(), newFolder);
        }
    }

    @Override
    public void addFolders(final List<Folder> folders) {
        for (Folder folder : folders) {
            addFolder(folder);
        }
    }

    @Override
    public List<Folder> getFolders() {
        return new ArrayList<>(mFolderMap.values());
    }

    @Override
    public List<String> getSelectedFoldersIds() {
        final List<String> listId = new ArrayList<>();
        for (Folder item : getFolders()) {
            if (item.isSelected()) {
                listId.add(item.getId());
            }
        }

        return listId;
    }

    @Override
    public Item getItemById(Item item) {
        if (item instanceof Folder) {
            final Folder folder = mFolderMap.get(item.getId());
            if (folder != null) {
                return folder;
            }

        } else if (item instanceof File) {
            final File file = mFileMap.get(item.getId());
            if (file != null) {
                return file;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public String getRootId() {
        final List<String> path = getPath();
        if (path != null && !path.isEmpty()) {
            return path.get(INDEX_ROOT);
        }

        return null;
    }

    @Nullable
    @Override
    public List<String> getPath() {
        return mExplorer.getPathParts();
    }

    @Nullable
    @Override
    public String getCurrentId() {
        return mExplorer.getCurrent().getId();
    }

    @Override
    public int getCurrentFolderAccess() {
        return mExplorer.getCurrent().getAccess();
    }

    @Override
    public String getCurrentFolderOwnerId() {
        return mExplorer.getCurrent().getCreatedBy().getId();
    }

    @Override
    public int getRootFolderType() {
        return mExplorer.getCurrent().getRootFolderType();
    }

    @Nullable
    @Override
    public String getCurrentTitle() {
        return mExplorer.getCurrent().getTitle();
    }

    @Override
    public int getCountItems() {
        return mFolderMap.size() + mFileMap.size();
    }

    @Override
    public boolean removeItemById(final String id) {
        final Folder folder = mFolderMap.remove(id);
        if (folder != null) {
            mSelectedItems -= folder.isSelected() ? 1 : 0;
            return true;
        }

        final File file = mFileMap.remove(id);
        if (file != null) {
            mSelectedItems -= file.isSelected() ? 1 : 0;
            return true;
        }

        return false;
    }

    @Override
    public int setSelectionAll(boolean isSelect) {
        int count = 0;
        for (File item : mFileMap.values()) {
            item.setSelected(isSelect);
            ++count;
        }

        for (Folder folder : mFolderMap.values()) {
            folder.setSelected(isSelect);
            ++count;
        }

        return mSelectedItems = isSelect ? count : 0;
    }

    @Override
    public int countSelected() {
        int count = 0;
        for (File item : mFileMap.values()) {
            if (item.isSelected()) {
                ++count;
            }
        }

        for (Folder folder : mFolderMap.values()) {
            if (folder.isSelected()) {
                ++count;
            }
        }

        return mSelectedItems = count;
    }

    @Override
    public int removeSelected() {
        int count = 0;
        final Iterator<Map.Entry<String, Folder>> folderIterator = mFolderMap.entrySet().iterator();
        while (folderIterator.hasNext()) {
            if (folderIterator.next().getValue().isSelected()) {
                folderIterator.remove();
                ++count;
            }
        }

        final Iterator<Map.Entry<String, File>> fileIterator = mFileMap.entrySet().iterator();
        while (fileIterator.hasNext()) {
            if (fileIterator.next().getValue().isSelected()) {
                fileIterator.remove();
                ++count;
            }
        }

        mSelectedItems -= count;
        return count;
    }

    @Override
    public int removeUnselected() {
        int count = 0;
        final Iterator<Map.Entry<String, Folder>> folderIterator = mFolderMap.entrySet().iterator();
        while (folderIterator.hasNext()) {
            if (!folderIterator.next().getValue().isSelected()) {
                folderIterator.remove();
                ++count;
            }
        }

        final Iterator<Map.Entry<String, File>> fileIterator = mFileMap.entrySet().iterator();
        while (fileIterator.hasNext()) {
            if (!fileIterator.next().getValue().isSelected()) {
                fileIterator.remove();
                ++count;
            }
        }

        mSelectedItems -= count;
        return count;
    }

    @Nullable
    @Override
    public Item setItemSelectedById(Item item, boolean isSelect) {
        final Item it = getItemById(item);
        if (it != null) {
            it.setSelected(isSelect);
            mSelectedItems += isSelect ? 1 : -1;
        }
        return it;
    }

    @Override
    public int getSelectedItems() {
        return mSelectedItems;
    }

    @Override
    public void setSelectedItems(int count) {
        mSelectedItems = count;
    }

    @Override
    public void setListPosition(final int listPosition) {
        mListPosition = listPosition;
    }

    @Override
    public void setLoadPosition(final int loadPosition) {
        mLoadPosition = loadPosition;
    }

    @Override
    public int getListPosition() {
        return mListPosition;
    }

    @Override
    public int getLoadPosition() {
        return mLoadPosition;
    }

    @Override
    public int hashCode() {
        return getCurrentId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ExplorerStackMap)) {
            return false;
        }

        final ExplorerStackMap itemStack = (ExplorerStackMap) obj;
        return itemStack.getCurrentId().equals(getCurrentId());
    }

    @Override
    @Nullable
    public ExplorerStackMap clone() {
        try {
            final ExplorerStackMap explorerStackMap = (ExplorerStackMap) super.clone();
            explorerStackMap.setExplorer(mExplorer.clone());
            explorerStackMap.setListPosition(getListPosition());
            explorerStackMap.setLoadPosition(getLoadPosition());
            explorerStackMap.setSelectedItems(getSelectedItems());
            explorerStackMap.addFolders(getFolders());
            explorerStackMap.addFiles(getFiles());
            return explorerStackMap;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}