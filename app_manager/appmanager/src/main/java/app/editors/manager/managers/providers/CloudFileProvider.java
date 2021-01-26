package app.editors.manager.managers.providers;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import app.editors.manager.app.Api;
import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.explorer.Operation;
import app.editors.manager.mvp.models.request.RequestBatchBase;
import app.editors.manager.mvp.models.request.RequestBatchOperation;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.models.request.RequestExternal;
import app.editors.manager.mvp.models.request.RequestFavorites;
import app.editors.manager.mvp.models.request.RequestRenameFile;
import app.editors.manager.mvp.models.request.RequestTitle;
import app.editors.manager.mvp.models.response.ResponseExternal;
import app.editors.manager.mvp.models.response.ResponseOperation;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class CloudFileProvider implements BaseFileProvider {

    public enum Section {
        My("@my"),
        Common("@common"),
        Shared("@share"),
        Projects("@projects"),
        Trash("@trash"),
        Favorites("@favorites");

        String mPath;

        Section(String path) {
            this.mPath = path;
        }

        public String getPath() {
            return mPath;
        }
    }

    private String mToken;
    private Api mApi;

    public CloudFileProvider(String mToken, Api mApi) {
        this.mToken = mToken;
        this.mApi = mApi;
    }

    @Override
    public Observable<Explorer> getFiles(String id, @Nullable Map<String, String> filter) {
        return Observable.fromCallable(() -> mApi.getItemById(mToken, id, filter).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseExplorerResponse -> {
                    if (responseExplorerResponse.isSuccessful() && responseExplorerResponse.body() != null) {
                        return responseExplorerResponse.body().getResponse();
                    } else {
                        throw new HttpException(responseExplorerResponse);
                    }
                });
    }

    @Override
    public Observable<File> createFile(String folderId, RequestCreate body) {
        return Observable.fromCallable(() -> mApi.createDocs(mToken, folderId, body).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseCreateFile -> {
                    if (responseCreateFile.isSuccessful() && responseCreateFile.body() != null) {
                        return responseCreateFile.body().getResponse();
                    } else {
                        throw new HttpException(responseCreateFile);
                    }
                });
    }

    @Override
    public Observable<Folder> createFolder(String folderId, RequestCreate body) {
        return Observable.fromCallable(() -> mApi.createFolder(mToken, folderId, body).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseCreateFolder -> {
                    if (responseCreateFolder.isSuccessful() && responseCreateFolder.body() != null) {
                        return responseCreateFolder.body().getResponse();
                    } else {
                        throw new HttpException(responseCreateFolder);
                    }
                });
    }

    @Override
    public Observable<Item> rename(Item item, String newName, @Nullable Integer version) {
        if (version == null) {
            return folderRename(item.getId(), newName);
        } else {
            return fileRename(item.getId(), newName, version);
        }
    }

    private Observable<Item> fileRename(String id, String newName, Integer version) {
        RequestRenameFile requestRenameFile = new RequestRenameFile();
        requestRenameFile.setLastVersion(version);
        requestRenameFile.setTitle(newName);
        return Observable.fromCallable(() -> mApi.renameFile(mToken, id, requestRenameFile).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseFile -> {
                    if (responseFile.isSuccessful() && responseFile.body() != null) {
                        return responseFile.body().getResponse();
                    } else if (responseFile.code() == 400) {
                        throw ProviderError.throwForbiddenError();
                    } else {
                        throw new HttpException(responseFile);
                    }
                });
    }

    private Observable<Item> folderRename(String id, String newName) {
        RequestTitle requestTitle = new RequestTitle();
        requestTitle.setTitle(newName);
        return Observable.fromCallable(() -> mApi.renameFolder(mToken, id, requestTitle).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseFolder -> {
                    if (responseFolder.isSuccessful() && responseFolder.body() != null) {
                        return responseFolder.body().getResponse();
                    } else if (responseFolder.code() == 400) {
                        throw ProviderError.throwForbiddenError();
                    } else {
                        throw new HttpException(responseFolder);
                    }
                });
    }

    @Override
    public Observable<List<Operation>> delete(List<Item> items, @Nullable Folder from) {
        List<String> filesId = new ArrayList<>();
        List<String> foldersId = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof File) {
                filesId.add(item.getId());
            } else if (item instanceof Folder) {
                if (item.getProviderItem()) {
                    removeStorage(item.getId());
                } else {
                    foldersId.add(item.getId());
                }
            }
        }
        RequestBatchBase request = new RequestBatchBase();
        request.setDeleteAfter(false);
        request.setFileIds(filesId);
        request.setFolderIds(foldersId);
        return Observable.fromCallable(() -> mApi.deleteBatch(mToken, request).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseOperationResponse -> {
                    if (responseOperationResponse.isSuccessful() && responseOperationResponse.body() != null) {
                        return responseOperationResponse.body().getResponse();
                    } else {
                        throw new HttpException(responseOperationResponse);
                    }
                });
    }

    private void removeStorage(String id) {
        Observable.fromCallable(() -> mApi.deleteStorage(mToken, id.substring(id.indexOf('-') + 1)).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> {
                    if (responseBody.isSuccessful()) {
                        Operation operation = new Operation();
                        operation.setProgress(100);
                        return Collections.singletonList(operation);
                    } else {
                        throw new HttpException(responseBody);
                    }
                }).subscribe();
    }

    @Override
    public Observable<Integer> download(List<Item> items) {
        return null;
    }

    @Override
    public Observable<Integer> upload(String folderId, List<Uri> uri) {
        return null;
    }

    @Override
    public Observable<List<Operation>> transfer(List<Item> items, @Nullable Folder to, int conflict, boolean isMove, boolean isOverwrite) {
        List<String> filesId = new ArrayList<>();
        List<String> foldersId = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof File) {
                filesId.add(item.getId());
            } else if (item instanceof Folder) {
                foldersId.add(item.getId());
            }
        }

        RequestBatchOperation batchOperation = new RequestBatchOperation();
        batchOperation.setFileIds(filesId);
        batchOperation.setFolderIds(foldersId);
        batchOperation.setDeleteAfter(false);
        batchOperation.setDestFolderId(to.getId());
        batchOperation.setConflictResolveType(conflict);

        if (isMove) {
            return moveItems(batchOperation);
        } else {
            return copyFiles(batchOperation);
        }
    }

    private Observable<List<Operation>> moveItems(RequestBatchOperation body) {
        return Observable.fromCallable(() -> mApi.move(mToken, body).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseOperation -> {
                    if (responseOperation.isSuccessful() && responseOperation.body() != null) {
                        return responseOperation.body().getResponse();
                    } else {
                        throw new HttpException(responseOperation);
                    }
                });
    }

    private Observable<List<Operation>> copyFiles(RequestBatchOperation body) {
        return Observable.fromCallable(() -> mApi.copy(mToken, body).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseOperation -> {
                    if (responseOperation.isSuccessful() && responseOperation.body() != null) {
                        return responseOperation.body().getResponse();
                    } else {
                        throw new HttpException(responseOperation);
                    }
                });
    }

    @Override
    public Observable<File> fileInfo(Item item) {
        return Observable.fromCallable(() -> mApi.getFileInfo(mToken, item.getId()).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseFile -> {
                    if (responseFile.isSuccessful() && responseFile.body() != null) {
                        return responseFile.body().getResponse();
                    } else {
                        throw new HttpException(responseFile);
                    }
                });
    }

    @Override
    public ResponseOperation getStatusOperation() {
        return mApi.status(mToken)
                .blockingGet();
    }

    @Override
    public Observable<ResponseExternal> share(String id, RequestExternal requestExternal) {
        return Observable.fromCallable(() -> mApi.getExternalLink(mToken, id, requestExternal).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseExternal -> {
                    if (responseExternal.isSuccessful()) {
                        return responseExternal.body();
                    } else {
                        throw new HttpException(responseExternal);
                    }
                });
    }

    @Override
    public Observable<List<Operation>> terminate() {
        return Observable.fromCallable(() -> mApi.terminate(mToken).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(operationResponse -> {
                    if (operationResponse.isSuccessful() && operationResponse.body() != null) {
                        return operationResponse.body().getResponse();
                    } else {
                        throw new HttpException(operationResponse);
                    }
                });
    }

    @Override
    public Observable<Base> addToFavorites(RequestFavorites requestFavorites) {
        return Observable.fromCallable(() -> mApi.addToFavorites(mToken, requestFavorites).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(baseResponse -> {
                    return baseResponse.body();
                });
    }

    @Override
    public Observable<Base> deleteFromFavorites(RequestFavorites requestFavorites) {
        return Observable.fromCallable(() -> mApi.deleteFromFavorites(mToken, requestFavorites).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(baseResponse -> {
                    return baseResponse.body();
                });
    }

    public Observable<List<Operation>> clearTrash() {
        return Observable.fromCallable(() -> mApi.emptyTrash(mToken).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(operationResponse -> {
                    if (operationResponse.isSuccessful() && operationResponse.body() != null) {
                        return operationResponse.body().getResponse();
                    } else {
                        throw new HttpException(operationResponse);
                    }
                });
    }

}
