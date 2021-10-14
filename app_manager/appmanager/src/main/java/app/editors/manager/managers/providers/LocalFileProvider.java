package app.editors.manager.managers.providers;

import android.net.Uri;

import androidx.annotation.Nullable;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import app.documents.core.network.ApiContract;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.CloudFile;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.mvp.models.explorer.Current;
import app.editors.manager.mvp.models.explorer.Explorer;
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
                        return mLocalContentTools.getFiles(new File(id));
                    } else {
                        throw ProviderError.throwErrorCreate();
                    }
                })
                .flatMap(files -> getExplore(files, new File(id)))
                .map(explorer -> sortExplorer(explorer, filter))
                .flatMap(explorer -> {
                    if (filter != null && filter.containsKey("filterValue")) {
                        return getFilterExplorer(explorer, filter.get("filterValue"));
                    }
                    return Observable.just(explorer);
                });
    }

    @Override
    public Observable<CloudFile> createFile(String folderId, RequestCreate body) {
        final File parentFile = new File(folderId);
        final String name = body.getTitle();
        try {
            final File localFile = mLocalContentTools.createFile(name, parentFile, App.getLocale());
            return Observable.just(localFile)
                    .map(createFile -> {
                        if (createFile.exists()) {
                            CloudFile file = new CloudFile();
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
        } catch (Throwable error) {
            return Observable.just(new CloudFile())
                    .map(  file ->  {
                        throw ProviderError.throwErrorCreate();
                    } );
        }
    }

    @Override
    public Observable<String> search(String query) {
        return null;
    }

    @Override
    public Observable<CloudFolder> createFolder(String folderId, RequestCreate body) {
        final File parentFile = new File(folderId);
        final String name = body.getTitle();
        return Observable.just(mLocalContentTools.createFolder(name, parentFile))
                .map(isCreate -> {
                    if (isCreate) {
                        CloudFolder folder = new CloudFolder();
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
        File oldFile = new File(item.getId());
        return Observable.just(mLocalContentTools.renameFile(oldFile, newName))
                .map(isRename -> {
                    if (isRename) {
                        item.setId(item.getId().replace(item.getTitle(), "") + newName);
                        if (item instanceof CloudFile) {
                            item.setTitle(newName + ((CloudFile) item).getFileExst());
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
    public Observable<List<Operation>> delete(List<Item> items, @Nullable CloudFolder from) {
        return Observable.fromIterable(items)
                .map(item -> mLocalContentTools.deleteFile(new File(item.getId())))
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
    public Observable<List<Operation>> transfer(List<Item> items, @Nullable CloudFolder to, int conflict, boolean isMove, boolean isOverwrite) {
        // Stub to local
        return null;
    }

    @Override
    public Observable<CloudFile> fileInfo(Item item) {
        ((CloudFile) item).setFileStatus(String.valueOf(ApiContract.FileStatus.NONE));
        return Observable.just((CloudFile) item);
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
            File file = new File(clickedItem.getId());
            File parentFile = new File(Uri.parse(path).getPath());
            if (parentFile.equals(file) || parentFile.getAbsolutePath().contains(file.getAbsolutePath())) {
                throw ProviderError.throwExistException();
            }
            return mLocalContentTools.moveFiles(file, parentFile, isCopy);
        } else {
            throw ProviderError.throwUnsupportedException();
        }
    }

    private Observable<Explorer> getExplore(List<File> localFiles, File parent) {
        return Observable.just(getExplorer(localFiles, parent));
    }

    private Explorer getExplorer(List<File> localFiles, File parent) {
        Explorer explorer = new Explorer();
        List<CloudFile> files = new ArrayList<>();
        List<CloudFolder> folders = new ArrayList<>();
        for (File convertFile : localFiles) {
            if (convertFile.isDirectory()) {
                CloudFolder folder = new CloudFolder();
                folder.setParentId(convertFile.getParentFile().getAbsolutePath());
                folder.setId(convertFile.getAbsolutePath());
                folder.setTitle(convertFile.getName());
                folder.setUpdated(new Date(convertFile.lastModified()));
                folders.add(folder);
            } else {
                CloudFile file = new CloudFile();
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
        return explorer;
    }

    private Observable<Explorer> getFilterExplorer(Explorer explorer, String value) {
        if(!value.isEmpty()) {
            return Observable.just(search(value, explorer.getCurrent().getId()));
        } else {
            return Observable.just(explorer);
        }
    }

    private Explorer search(String value, String id){
        File root = new File(id);
        File[] listFiles = root.listFiles();

        List<File> files = new ArrayList();
        Explorer resultExplorer = new Explorer();
        Explorer tempExplorer = new Explorer();

        for(File item: listFiles) {
            if (item.getName().toLowerCase().contains(value.toLowerCase())) {
                files.add(item);
                resultExplorer = getExplorer(files, new File(id));
            }
            if (item.isDirectory()) {
                tempExplorer = search(value, item.getAbsolutePath());
            }
            resultExplorer.add(tempExplorer);
        }
        return resultExplorer;
    }

    private Explorer sortExplorer(Explorer explorer, @Nullable Map<String, String> filter) {
        List<CloudFolder> folders = explorer.getFolders();
        List<CloudFile> files = explorer.getFiles();

        if (filter != null) {
            final String sort = filter.get(ApiContract.Parameters.ARG_SORT_BY);
            final String order = filter.get(ApiContract.Parameters.ARG_SORT_ORDER);
            if (sort != null && order != null) {
                switch (sort) {
                    case ApiContract.Parameters.VAL_SORT_BY_UPDATED:
                        Collections.sort(folders, (o1, o2) -> o1.getUpdated().compareTo(o2.getUpdated()));
                        break;
                    case ApiContract.Parameters.VAL_SORT_BY_TITLE:
                        Collections.sort(folders, (o1, o2) -> o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase()));
                        Collections.sort(files, (o1, o2) -> o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase()));
                        break;
                    case ApiContract.Parameters.VAL_SORT_BY_SIZE:
                        Collections.sort(files, (o1, o2) -> Long.compare(o1.getPureContentLength(), o2.getPureContentLength()));
                        break;
                    case ApiContract.Parameters.VAL_SORT_BY_TYPE:
                        Collections.sort(files, (o1, o2) -> o1.getFileExst().compareTo(o2.getFileExst()));
                        break;
                }

                if (order.equals(ApiContract.Parameters.VAL_SORT_ORDER_DESC)) {
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
