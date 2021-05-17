package app.editors.manager.managers.providers;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.Nullable;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import app.documents.core.webdav.WebDavModel;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.documents.core.webdav.WebDavApi;
import app.editors.manager.managers.retrofit.ProgressRequestBody;
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
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public
class WebDavFileProvider implements BaseFileProvider {

    private static final String TAG = WebDavFileProvider.class.getSimpleName();

    private static final int TOTAL_PROGRESS = 100;
    private static final String PATH_TEMPLATES = "templates/";
    private static final String PATH_DOWNLOAD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OnlyOffice";

    private WebDavApi mApi;
    private WebDavApi.Providers mProvider;
    private List<Item> mBatchItems;
    private List<CloudFile> mUploadsFile = Collections.synchronizedList(new ArrayList<>());

    public WebDavFileProvider(WebDavApi mApi, WebDavApi.Providers provider) {
        this.mApi = mApi;
        this.mProvider = provider;
    }

    @Override
    public Observable<Explorer> getFiles(String id, @Nullable Map<String, String> filter) {
        return Observable.fromCallable(() -> mApi.propfind(id).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(modelResponse -> {
                    if (modelResponse.isSuccessful() && modelResponse.body() != null) {
                        return modelResponse.body();
                    } else {
                        throw new HttpException(modelResponse);
                    }
                }).map(webDavModel -> getExplorer(webDavModel.getList(), filter));
    }

    @Override
    public Observable<CloudFile> createFile(String folderId, RequestCreate body) {
        String title = body.getTitle();
        String path = PATH_TEMPLATES + FileUtils.getTemplates(App.getApp(), App.getLocale(),
                StringUtils.getExtensionFromPath(body.getTitle().toLowerCase()));

        File temp = FileUtils.createTempAssetsFile(App.getApp(), path, StringUtils.getNameWithoutExtension(title), StringUtils.getExtensionFromPath(title));

        return Observable.fromCallable(() -> {
            CloudFile file = new CloudFile();
            file.setWebUrl(Uri.fromFile(temp).toString());
            file.setPureContentLength(temp != null ? temp.length() : 0);
            file.setId(folderId + body.getTitle());
            file.setUpdated(new Date());
            file.setTitle(body.getTitle());
            file.setFileExst(StringUtils.getExtensionFromPath(body.getTitle()));
            return file;
        });
    }

    @Override
    public Observable<CloudFolder> createFolder(String folderId, RequestCreate body) {
        return Observable.fromCallable(() -> mApi.createFolder(folderId + body.getTitle()).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> {
                    if (responseBody.isSuccessful()) {
                        CloudFolder folder = new CloudFolder();
                        folder.setTitle(body.getTitle());
                        folder.setId(folderId + body.getTitle());
                        folder.setUpdated(new Date());
                        return folder;
                    } else {
                        throw new HttpException(responseBody);
                    }
                });
    }

    @Override
    public Observable<Item> rename(Item item, String newName, @Nullable Integer version) {
        String correctPath;
        if (item instanceof CloudFile && version != null) {
            newName = StringUtils.getEncodedString(newName) + ((CloudFile) item).getFileExst();
            correctPath = filePath(item.getId(), newName);
            return renameFile(correctPath, newName, (CloudFile) item);
        } else if (item instanceof CloudFolder) {
            correctPath = folderPath(item.getId(), newName);
            return renameFolder(correctPath, newName, (CloudFolder) item);
        } else {
            return Observable.just(new Item());
        }
    }

    private Observable<Item> renameFolder(String correctPath, String newName, CloudFolder folder) {
        return Observable.fromCallable(() -> mApi.move(StringUtils.getEncodedString(correctPath), folder.getId(), "F").execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> {
                    if (responseBody.isSuccessful()) {
                        folder.setId(correctPath);
                        folder.setTitle(NetworkUtils.decodeUrl(newName));
                        folder.setUpdated(new Date());
                        return folder;
                    } else {
                        throw new HttpException(responseBody);
                    }
                });
    }

    private Observable<Item> renameFile(String correctPath, String newName, CloudFile file) {
        return Observable.fromCallable(() -> mApi.move(correctPath, file.getId(), "F").execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> {
                    if (responseBody.isSuccessful()) {
                        file.setId(correctPath);
                        file.setTitle(NetworkUtils.decodeUrl(newName));
                        file.setUpdated(new Date());
                        return file;
                    } else {
                        throw new HttpException(responseBody);
                    }
                });
    }

    @Override
    public Observable<List<Operation>> delete(List<Item> items, @Nullable CloudFolder from) {
        mBatchItems = items;
        return Observable.fromIterable(items).map(item -> mApi.delete(item.getId()).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> {
                    if (responseBody.isSuccessful() && (responseBody.code() == 204 || responseBody.code() == 202)) {
                        return responseBody;
                    } else {
                        throw new HttpException(responseBody);
                    }
                }).buffer(items.size())
                .map(responseBodies -> {
                    List<Operation> operations = new ArrayList<>();
                    Operation operation = new Operation();
                    operation.setProgress(TOTAL_PROGRESS);
                    operations.add(operation);
                    return operations;
                });
    }

    @Override
    public Observable<List<Operation>> transfer(List<Item> items, @Nullable CloudFolder to, int conflict, boolean isMove, boolean isOverwrite) {
        if (isMove) {
            return moveItems(items, to, isOverwrite);
        } else {
            return copyItems(items, to, isOverwrite);
        }
    }

    private Observable<List<Operation>> copyItems(List<Item> items, CloudFolder to, boolean overwrite) {
        String headerOverwrite = overwrite ? "T" : "F";
        return Observable.fromIterable(items)
                .flatMap(item ->
                        Observable.fromCallable(() -> mApi.
                                copy(StringUtils.getEncodedString(to.getId()) +
                                        StringUtils.getEncodedString(item.getTitle()), item.getId(), headerOverwrite)
                                .execute())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())).map(responseBody -> {
                    if (responseBody.isSuccessful()) {
                        Operation operation = new Operation();
                        operation.setProgress(TOTAL_PROGRESS);
                        return Collections.singletonList(operation);
                    } else {
                        HttpException httpException = new HttpException(responseBody);
                        httpException.addSuppressed(new Exception(getTitle(responseBody.raw().request().header("Destination"))));
                        throw httpException;
                    }
                });
    }

    private Observable<List<Operation>> moveItems(List<Item> items, CloudFolder to, boolean overwrite) {
        String headerOverwrite = overwrite ? "T" : "F";
        return Observable.fromIterable(items)
                .flatMap(item -> Observable.fromCallable(() -> mApi
                        .move(StringUtils.getEncodedString(to.getId()) +
                                StringUtils.getEncodedString(item.getTitle()), item.getId(), headerOverwrite)
                        .execute())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()))
                .map(responseBody -> {
                    if (responseBody.isSuccessful()) {
                        return responseBody;
                    } else {
                        HttpException httpException = new HttpException(responseBody);
                        httpException.addSuppressed(new Exception(getTitle(responseBody.raw().request().header("Destination"))));
                        throw httpException;
                    }
                })
                .buffer(items.size())
                .map(responses -> {
                    Operation operation = new Operation();
                    operation.setProgress(TOTAL_PROGRESS);
                    return Collections.singletonList(operation);
                });
    }

    @Override
    public Observable<CloudFile> fileInfo(Item item) {
        return Observable.create(emitter -> {

            File outputFile = checkDirectory(item);


            //TODO Возможно надо пределать
//            Response<WebDavModel> response = mApi.propfind(item.getId()).execute();
//            if (response.isSuccessful() && response.body() != null) {
//                WebDavModel.ResponseBean bean = response.body().getList().get(0);
//                if (bean != null) {
//                    if (outputFile != null && outputFile.exists()) {
//                        if (item instanceof File) {
//                            File file = (File) item;
//                            if (file.getPureContentLength() != outputFile.length() || outputFile.length() != Long.parseLong(bean.getContentLength())) {
//                                download(emitter, item, outputFile);
//                            } else  {
//                                emitter.onNext(setFile(item, outputFile));
//                                emitter.onComplete();
//                            }
//                        }
//                    }
//                }
//            }

            if (outputFile != null && outputFile.exists()) {
                if (item instanceof CloudFile) {
                    CloudFile file = (CloudFile) item;
                    if (file.getPureContentLength() != outputFile.length()) {
                        download(emitter, item, outputFile);
                    } else  {
                        emitter.onNext(setFile(item, outputFile));
                        emitter.onComplete();
                    }
                }
            }

        });
    }


    public Observable<CloudFile> fileInfo(Item item, Boolean isDownload) {
        return Observable.create(emitter -> {

            File outputFile = checkDirectory(item);

            if (outputFile != null && outputFile.exists()) {
                if (item instanceof CloudFile) {
                    if (isDownload) {
                        download(emitter, item, outputFile);
                    } else {
                        emitter.onNext(setFile(item, outputFile));
                        emitter.onComplete();
                    }
                }
            }

        });
    }

    private void download(Emitter<CloudFile> emitter, Item item, File outputFile) throws IOException {
        Response<ResponseBody> response = mApi.download(item.getId()).execute();
        if (response.body() != null) {
            try (InputStream inputStream = response.body().byteStream(); OutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, count);
                }
                outputStream.flush();

                emitter.onNext(setFile(item, outputFile));
                emitter.onComplete();

            } catch (IOException error) {
                emitter.onError(error);
            }
        } else {
            emitter.onError(new HttpException(response));
        }
    }

    @SuppressLint("MissingPermission")
    private File checkDirectory(Item item) {
        CloudFile file = (CloudFile) item;
        StringUtils.Extension extension = StringUtils.getExtension(file.getFileExst());
        switch (extension) {
            case UNKNOWN:
            case EBOOK:
            case ARCH:
            case VIDEO:
            case HTML:
                File parent = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OnlyOffice");
                return FileUtils.createFile(parent, file.getTitle());
        }
        File local = new File(Uri.parse(file.getWebUrl()).getPath());
        if (local.exists()) {
            return local;
        } else  {
            return FileUtils.createCacheFile(App.getApp(), item.getTitle());
        }

    }

    private CloudFile setFile(Item item, File outputFile) {
        CloudFile originFile = (CloudFile) item;
        CloudFile file = new CloudFile();
        file.setFolderId(originFile.getFolderId());
        file.setTitle(originFile.getTitle());
        file.setPureContentLength(outputFile.length());
        file.setFileExst(originFile.getFileExst());
        file.setViewUrl(originFile.getId());
        file.setId("");
        file.setWebUrl(Uri.fromFile(outputFile).toString());
        return file;
    }

    @Override
    public Observable<Integer> download(List<Item> items) {
        return Observable.fromIterable(items)
                .filter(item -> item instanceof CloudFile)
                .flatMap(this::startDownload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @SuppressLint("MissingPermission")
    private Observable<Integer> startDownload(Item item) {
        return Observable.create(emitter -> {
            Response<ResponseBody> response = mApi.download(item.getId()).execute();
            File outputFile = new File(PATH_DOWNLOAD, item.getTitle());
            if (response.body() != null) {
                try (InputStream inputStream = response.body().byteStream(); OutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int count;
                    int progress = 0;
                    long fileSize = response.body().contentLength();
                    while ((count = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, count);
                        progress += count;
                        emitter.onNext((int) ((double) progress / (double) fileSize * 100));

                    }
                    outputStream.flush();
                    emitter.onNext(100);
                    emitter.onComplete();

                } catch (IOException error) {
                    emitter.onNext(0);
                    emitter.onError(error);
                }
            } else {
                emitter.onError(new HttpException(response));
            }
        });
    }

    @Override
    public Observable<Integer> upload(String folderId, List<Uri> uris) {
        return Observable.fromIterable(uris)
                .map(uri -> addUploadsFile(folderId, uri))
                .flatMap(uri -> startUpload(folderId, uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Uri addUploadsFile(String id, Uri uri) {
        String fileName = ContentResolverUtils.getName(App.getApp(), uri);
        CloudFile uploadFile = new CloudFile();
        uploadFile.setId(id + fileName);
        uploadFile.setFolderId(id);
        uploadFile.setFileExst(StringUtils.getExtensionFromPath(fileName));
        uploadFile.setTitle(fileName);
        uploadFile.setUpdated(new Date());
        uploadFile.setWebUrl(uri.toString());
        uploadFile.setPureContentLength(ContentResolverUtils.getSize(App.getApp(), uri));
        mUploadsFile.add(uploadFile);
        return uri;
    }

    private ObservableSource<Integer> startUpload(String id, Uri uri) {
        return Observable.create(emitter -> {
            String fileName = ContentResolverUtils.getName(App.getApp(), uri);
            ProgressRequestBody requestBody = new ProgressRequestBody(App.getApp(), uri);
            requestBody.setOnUploadCallbacks((total, progress) -> emitter.onNext(FileUtils.getPercentOfLoading(total, progress)));
            Response<ResponseBody> response = mApi.upload(requestBody, id + fileName).execute();
            if (response.isSuccessful()) {
                emitter.onNext(100);
                emitter.onComplete();
            } else {
                emitter.onError(new HttpException(response));
            }
        });
    }

    @Override
    public ResponseOperation getStatusOperation() {
        ResponseOperation responseOperation = new ResponseOperation();
        responseOperation.setResponse(new ArrayList<>());
        return responseOperation;
    }

    @Override
    public Observable<List<Operation>> terminate() {
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

    @Override
    public Observable<ResponseExternal> share(String id, RequestExternal requestExternal) {
        return null;
    }

    private Explorer getExplorer(List<WebDavModel.ResponseBean> responseBeans, @Nullable Map<String, String> filter) throws UnsupportedEncodingException {
        Explorer explorer = new Explorer();
        String filteringValue = filter.get(Api.Parameters.ARG_FILTER_VALUE);
        List<CloudFile> files = new ArrayList<>();
        List<CloudFolder> folders = new ArrayList<>();

        CloudFolder parentFolder = getFolder(responseBeans.get(0));

        for (int i = 1; i < responseBeans.size(); i++) {
            WebDavModel.ResponseBean bean = responseBeans.get(i);
            if ((bean.getContentLength() == null && bean.getContentType() == null)
                    || (bean.getContentType() != null && bean.getContentType().equals("httpd/unix-directory"))) {
                CloudFolder folder = new CloudFolder();
                folder.setId(bean.getHref());
                folder.setTitle(getTitle(bean.getHref()));
                folder.setParentId(parentFolder.getId());
                folder.setUpdated(bean.getLastModifiedDate());
                folder.setEtag(bean.getEtag());
                if (filteringValue != null) {
                    if (folder.getTitle().toLowerCase().startsWith(filteringValue)) {
                        folders.add(folder);
                    }
                } else {
                    folders.add(folder);
                }
            } else {
                CloudFile file = new CloudFile();
                file.setId(bean.getHref());
                file.setTitle(getTitle(bean.getHref()));
                file.setFolderId(parentFolder.getId());
                file.setPureContentLength(Long.parseLong(bean.getContentLength()));
                file.setFileExst(StringUtils.getExtensionFromPath(file.getTitle().toLowerCase()));
                file.setCreated(bean.getLastModifiedDate());
                file.setUpdated(bean.getLastModifiedDate());
                if (filteringValue != null) {
                    if (file.getTitle().toLowerCase().startsWith(filteringValue)) {
                        files.add(file);
                    }
                } else {
                    files.add(file);
                }
            }
        }
        Current current = new Current();
        current.setId(parentFolder.getId());
        current.setFilesCount(String.valueOf(files.size()));
        current.setFoldersCount(String.valueOf(folders.size()));
        current.setTitle(parentFolder.getTitle());

        explorer.setCurrent(current);
        explorer.setFiles(files);
        explorer.setFolders(folders);
        return explorer;
    }

    private CloudFolder getFolder(WebDavModel.ResponseBean responseBean) throws UnsupportedEncodingException {
        CloudFolder folder = new CloudFolder();
        folder.setId(NetworkUtils.decodeUrl(responseBean.getHref()));
        folder.setTitle(NetworkUtils.decodeUrl(getFolderTitle(responseBean.getHref())));
        folder.setUpdated(responseBean.getLastModifiedDate());
        folder.setEtag(responseBean.getEtag());
        return folder;
    }

    private String getFolderTitle(String href) {
        return href.substring(href.lastIndexOf('/', href.length() - 2) + 1, href.lastIndexOf('/'));
    }

    private String getTitle(String href) throws UnsupportedEncodingException {
        String title = href.substring(href.lastIndexOf('/')).replaceAll("/", "");
        if (title.equals("")) {
            title = getFolderTitle(href);
        }
        title = NetworkUtils.decodeUrl(title);
        return title;
    }

    private String folderPath(String id, String newName) {
        StringBuilder builder = new StringBuilder();
        id = id.substring(0, id.lastIndexOf('/'));
        return builder.append(filePath(id, newName)).append("/").toString();
    }

    private String filePath(String id, String newName) {
        return id.substring(0, id.lastIndexOf('/') + 1) +
                newName;
    }

    public List<CloudFile> getUploadFile() {
        return mUploadsFile;
    }

    private String getTransferId(Item item) {
        String id;
        if (mProvider == WebDavApi.Providers.NextCloud || mProvider == WebDavApi.Providers.OwnCloud) {
            id = item.getTitle();
        } else {
            if (item instanceof CloudFolder) {
                id = item.getId().substring(0, item.getId().lastIndexOf('/'));
            } else {
                id = item.getId();
            }
        }
        return id;
    }
}
