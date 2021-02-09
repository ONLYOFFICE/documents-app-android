package app.editors.manager.managers.providers;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.Current;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.explorer.Operation;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.models.request.RequestExternal;
import app.editors.manager.mvp.models.request.RequestFavorites;
import app.editors.manager.mvp.models.response.ResponseExternal;
import app.editors.manager.mvp.models.response.ResponseOperation;
import io.reactivex.Observable;
import lib.toolkit.base.managers.tools.LocalContentTools;
import lib.toolkit.base.managers.utils.StringUtils;

public class LocalFileProvider implements BaseFileProvider {

    private LocalContentTools mLocalContentTools;

    public LocalFileProvider(LocalContentTools mLocalContentTools) {
        this.mLocalContentTools = mLocalContentTools;
    }

    @Override
    public Observable<Explorer> getFiles(String id, @Nullable Map<String, String> filter) {
        return Observable.just(mLocalContentTools.createRootDir())
                .map(file -> {
                    if (file.exists()) {
                        return mLocalContentTools.getFiles(new java.io.File(id));
                    } else {
                        throw ProviderError.throwErrorCreate();
                    }
                })
                .flatMap(files -> getExplore(files, new java.io.File(id)))
                .map(explorer -> sortExplorer(explorer, filter))
                .flatMap(explorer -> {
                    if (filter != null && filter.containsKey("filterValue")) {
                        return getFilterExplorer(explorer, filter.get("filterValue"));
                    }
                    return Observable.just(explorer);
                });
    }

    @Override
    public Observable<File> createFile(String folderId, RequestCreate body) {
        final java.io.File parentFile = new java.io.File(folderId);
        final String name = body.getTitle();
        return Observable.just(mLocalContentTools.createFile(name, parentFile, App.getLocale()))
                .map(createFile -> {
                    if (createFile.exists()) {
                        File file = new File();
                        file.setId(folderId + "/" + createFile.getName());
                        file.setTitle(createFile.getName());
                        file.setFolderId(folderId);
                        file.setPureContentLength(createFile.length());
                        file.setFileExst(StringUtils.getExtensionFromPath(name));
                        file.setCreated(new Date());
                        file.setJustCreated(true);
                        return file;
                    } else {
                        throw ProviderError.throwErrorCreate();
                    }
                });
    }

    @Override
    public Observable<Folder> createFolder(String folderId, RequestCreate body) {
        final java.io.File parentFile = new java.io.File(folderId);
        final String name = body.getTitle();
        return Observable.just(mLocalContentTools.createFolder(name, parentFile))
                .map(isCreate -> {
                    if (isCreate) {
                        Folder folder = new Folder();
                        folder.setId(folderId + "/" + name);
                        folder.setTitle(name);
                        folder.setParentId(folderId);
                        folder.setCreated(new Date());
                        folder.setJustCreated(true);
                        return folder;
                    } else {
                        throw ProviderError.throwErrorCreate();
                    }
                });
    }

    @Override
    public Observable<Item> rename(Item item, String newName, @Nullable Integer version) {
        java.io.File oldFile = new java.io.File(item.getId());
        return Observable.just(mLocalContentTools.renameFile(oldFile, newName))
                .map(isRename -> {
                    if (isRename) {
                        item.setId(item.getId().replace(item.getTitle(), "") + newName);
                        if (item instanceof File) {
                            item.setTitle(newName + ((File) item).getFileExst());
                        } else {
                            item.setTitle(newName);
                        }
                        return item;
                    } else {
                        throw new Exception("Error rename");
                    }
                });
    }

    @Override
    public Observable<List<Operation>> delete(List<Item> items, @Nullable Folder from) {
        return Observable.fromIterable(items)
                .map(item -> mLocalContentTools.deleteFile(new java.io.File(item.getId())))
                .toList()
                .toObservable()
                .map(booleans -> {
                    if (!booleans.isEmpty()) {
                        Operation operation = new Operation();
                        operation.setProgress(100);
                        return Collections.singletonList(operation);
                    } else {
                        throw new Exception("Error delete");
                    }
                });
    }

    @Override
    public Observable<List<Operation>> transfer(List<Item> items, @Nullable Folder to, int conflict, boolean isMove, boolean isOverwrite) {
        // Stub to local
        return null;
    }

    @Override
    public Observable<File> fileInfo(Item item) {
        return Observable.just((File) item);
    }

    @Override
    public ResponseOperation getStatusOperation() {
        // Stub to local
        return null;
    }

    @Override
    public Observable<Integer> download(List<Item> items) {
        // Stub to local
        return null;
    }

    @Override
    public Observable<Integer> upload(String folderId, List<Uri> uris) {
        // Stub to local
        return null;
    }

    @Override
    public Observable<ResponseExternal> share(String id, RequestExternal requestExternal) {
        // Stub to local
        return null;
    }

    @Override
    public Observable<List<Operation>> terminate() {
        // Stub to local
        return null;
    }

    @Override
    public Observable<Base> addToFavorites(RequestFavorites fileId) {
         return null;//stub
    }

    @Override
    public Observable<Base> deleteFromFavorites(RequestFavorites requestFavorites) {
        return null;
    }


    public boolean transfer(String path, Item clickedItem, boolean isCopy) throws Exception {
        if (path != null) {
            java.io.File file = new java.io.File(clickedItem.getId());
            java.io.File parentFile = new java.io.File(Uri.parse(path).getPath());
            if (parentFile.equals(file) || parentFile.getAbsolutePath().contains(file.getAbsolutePath())) {
                throw ProviderError.throwExistException();
            }
            return mLocalContentTools.moveFiles(file, parentFile, isCopy);
        } else {
            throw ProviderError.throwUnsupportedException();
        }
    }

    private Observable<Explorer> getExplore(List<java.io.File> localFiles, java.io.File parent) {
        Explorer explorer = new Explorer();
        List<File> files = new ArrayList<>();
        List<Folder> folders = new ArrayList<>();
        for (java.io.File convertFile : localFiles) {
            if (convertFile.isDirectory()) {
                Folder folder = new Folder();
                folder.setParentId(convertFile.getParentFile().getAbsolutePath());
                folder.setId(convertFile.getAbsolutePath());
                folder.setTitle(convertFile.getName());
                folder.setUpdated(new Date(convertFile.lastModified()));
                folders.add(folder);
            } else {
                File file = new File();
                file.setId(convertFile.getAbsolutePath());
                file.setTitle(convertFile.getName());
                file.setFileExst(StringUtils.getExtensionFromPath(convertFile.getName()));
                file.setPureContentLength(convertFile.length());
                file.setFolderId(convertFile.getParentFile().getAbsolutePath());
                file.setWebUrl(convertFile.getAbsolutePath());
                file.setUpdated(new Date(convertFile.lastModified()));
                files.add(file);
            }
        }
        Current current = new Current();
        current.setId(parent.getAbsolutePath());
        current.setTitle(parent.getName());
        current.setFilesCount(String.valueOf(files.size()));
        current.setFoldersCount(String.valueOf(folders.size()));

        explorer.setFiles(files);
        explorer.setFolders(folders);
        explorer.setCurrent(current);
        return Observable.just(explorer);
    }

    private Observable<Explorer> getFilterExplorer(Explorer explorer, String value) {
        List<File> files = explorer.getFiles();
        List<Folder> folders = explorer.getFolders();

        Observable<File> startFile = Observable.fromIterable(files)
                .filter(file -> file.getTitle().toLowerCase().contains(value.toLowerCase()));

        Observable<Folder> startFolder = Observable.fromIterable(folders)
                .filter(folder -> folder.getTitle().toLowerCase().contains(value.toLowerCase()));

        return Observable.concat(startFile, startFolder)
                .distinct()
                .toList()
                .map(items -> {
                    files.clear();
                    folders.clear();
                    for (Item item : items) {
                        if (item instanceof File) {
                            files.add((File) item);
                        } else if (item instanceof Folder) {
                            folders.add((Folder) item);
                        }
                    }
                    explorer.setFiles(files);
                    explorer.setFolders(folders);
                    return explorer;
                }).toObservable();
    }

    private Explorer sortExplorer(Explorer explorer, @Nullable Map<String, String> filter) {
        List<Folder> folders = explorer.getFolders();
        List<File> files = explorer.getFiles();

        if (filter != null) {
            final String sort = filter.get(Api.Parameters.ARG_SORT_BY);
            final String order = filter.get(Api.Parameters.ARG_SORT_ORDER);
            if (sort != null && order != null) {
                switch (sort) {
                    case Api.Parameters.VAL_SORT_BY_UPDATED:
                        Collections.sort(folders, (o1, o2) -> o1.getUpdated().compareTo(o2.getUpdated()));
                        break;
                    case Api.Parameters.VAL_SORT_BY_TITLE:
                        Collections.sort(folders, (o1, o2) -> o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase()));
                        Collections.sort(files, (o1, o2) -> o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase()));
                        break;
                    case Api.Parameters.VAL_SORT_BY_SIZE:
                        Collections.sort(files, (o1, o2) -> Long.compare(o1.getPureContentLength(), o2.getPureContentLength()));
                        break;
                    case Api.Parameters.VAL_SORT_BY_TYPE:
                        Collections.sort(files, (o1, o2) -> o1.getFileExst().compareTo(o2.getFileExst()));
                        break;
                }

                if (order.equals(Api.Parameters.VAL_SORT_ORDER_DESC)) {
                    Collections.reverse(folders);
                    Collections.reverse(files);
                }
            }
        }

        explorer.setFolders(folders);
        explorer.setFiles(files);
        return explorer;
    }
}
