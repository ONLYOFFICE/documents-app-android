package app.editors.manager.mvp.models.models;


import java.io.Serializable;
import java.util.List;

import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;

public interface ExplorerStack extends Serializable, Cloneable {

    /*
    * Replace current explorer
    * */
    void setExplorer(Explorer explorer);

    /*
    * Add explorer to current with check
    * */
    void addExplorer(Explorer explorer);

    /*
    * Get explorer
    * */
    Explorer getExplorer();

    /*
    * Add files
    * */
    void addFile(File file);
    void addFileFirst(File file);
    void addFiles(List<File> files);
    List<File> getFiles();
    List<String> getSelectedFilesIds();
    List<File> getSelectedFiles();

    /*
    * Add folders
    * */
    void addFolder(Folder folder);
    void addFolderFirst(Folder folder);
    void addFolders(List<Folder> folder);
    List<Folder> getFolders();

    //Get download files
    List<String> getSelectedFoldersIds();
    List<Folder> getSelectedFolders();

    /*
    * Get by id folder/file
    * */
    Item getItemById(Item item);

    /*
    * Get id of root folder
    * */
    String getRootId();

    /*
    * Get full folders path
    * */
    List<String> getPath();

    /*
    * Get id of current folder
    * */
    String getCurrentId();

    /*
     * Get current folder access
     * */
    int getCurrentFolderAccess();

    /*
     * Get current folder owner id
     * */
    String getCurrentFolderOwnerId();

    /*
     * Get root folder type
     * */
    int getRootFolderType();

    /*
    * Get title of current folder
    * */
    String getCurrentTitle();

    /*
    * Get count items of current folder
    * */
    int getCountItems();

    /*
    * Remove folder/file
    * */
    boolean removeItemById(String id);

    /*
     * Select/deselect items
     * */
    int setSelectionAll(boolean isSelect);
    int countSelected();

    /*
    * Remove (un)select folders/files
    * */
    int removeSelected();
    int removeUnselected();

    /*
     * Count of selected items
     * */
    Item setItemSelectedById(Item item, boolean isSelect);
    int getSelectedItems();
    void setSelectedItems(int count);
    /*
    * Get/set list navigation position
    * */
    void setListPosition(int position);
    int getListPosition();

    /*
    * Get/set loading position
    * */
    void setLoadPosition(int position);
    int getLoadPosition();

    /*
    * Cloneable method
    * */
    ExplorerStack clone();
}