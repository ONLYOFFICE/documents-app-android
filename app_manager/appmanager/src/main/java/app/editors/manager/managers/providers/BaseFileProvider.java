package app.editors.manager.managers.providers;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.CloudFile;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.explorer.Operation;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.models.request.RequestExternal;
import app.editors.manager.mvp.models.request.RequestFavorites;
import app.editors.manager.mvp.models.response.ResponseExternal;
import app.editors.manager.mvp.models.response.ResponseOperation;
import io.reactivex.Observable;

public interface BaseFileProvider {

    Observable<Explorer> getFiles(String id, @Nullable Map<String, String> filter);

    Observable<CloudFile> createFile(String folderId, RequestCreate body);

    Observable<CloudFolder> createFolder(String folderId, RequestCreate body);

    Observable<Item> rename(Item item, String newName, @Nullable Integer version);

    Observable<List<Operation>> delete(List<Item> items, @Nullable CloudFolder from);

    Observable<List<Operation>> transfer(List<Item> items, @Nullable CloudFolder to, int conflict, boolean isMove, boolean isOverwrite);

    Observable<CloudFile> fileInfo(Item item);

    ResponseOperation getStatusOperation();

    Observable<Integer> download(List<Item> items);

    Observable<Integer> upload(String folderId, List<Uri> uris);

    Observable<ResponseExternal> share(String id, RequestExternal requestExternal);

    Observable<List<Operation>> terminate();

    Observable<Base> addToFavorites(RequestFavorites requestFavorites);

    Observable<Base> deleteFromFavorites(RequestFavorites requestFavorites);

}
