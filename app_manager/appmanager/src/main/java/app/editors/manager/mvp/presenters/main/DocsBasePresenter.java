package app.editors.manager.mvp.presenters.main;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.net.ssl.SSLHandshakeException;

import app.documents.core.account.AccountDao;
import app.documents.core.account.RecentDao;
import app.documents.core.network.ApiContract;
import app.documents.core.settings.NetworkSettings;
import app.editors.manager.R;
import app.editors.manager.managers.exceptions.NoConnectivityException;
import app.editors.manager.managers.providers.BaseFileProvider;
import app.editors.manager.managers.providers.ProviderError;
import app.editors.manager.managers.providers.WebDavFileProvider;
import app.editors.manager.managers.services.DownloadService;
import app.editors.manager.managers.services.UploadService;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.managers.works.DownloadWork;
import app.editors.manager.managers.works.UploadWork;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.CloudFile;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.explorer.Operation;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.models.models.ExplorerStack;
import app.editors.manager.mvp.models.models.ExplorerStackMap;
import app.editors.manager.mvp.models.models.ModelExplorerStack;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.models.request.RequestDownload;
import app.editors.manager.mvp.models.states.OperationsState;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.main.DocsBaseView;
import app.editors.manager.onedrive.OneDriveFileProvider;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import moxy.InjectViewState;
import moxy.MvpPresenter;
import okhttp3.ResponseBody;
import retrofit2.HttpException;


@InjectViewState
public abstract class DocsBasePresenter<View extends DocsBaseView> extends MvpPresenter<View> {

    public static final String TAG = DocsBasePresenter.class.getSimpleName();

    private static final String KEY_ERROR_CODE = "statusCode";
    private static final String KEY_ERROR_INFO = "error";
    private static final String KEY_ERROR_INFO_MESSAGE = "message";

    /*
     * Tags for dialog action callback
     * */
    public static final String TAG_DIALOG_CONTEXT_RENAME = "TAG_DIALOG_CONTEXT_RENAME";
    public static final String TAG_DIALOG_CONTEXT_SHARE_DELETE = "TAG_DIALOG_CONTEXT_SHARE_DELETE";

    public static final String TAG_DIALOG_ACTION_SHEET = "TAG_DIALOG_ACTION_SHEET";
    public static final String TAG_DIALOG_ACTION_PRESENTATION = "TAG_DIALOG_ACTION_PRESENTATION";
    public static final String TAG_DIALOG_ACTION_DOC = "TAG_DIALOG_ACTION_DOC";
    public static final String TAG_DIALOG_ACTION_FOLDER = "TAG_DIALOG_ACTION_FOLDER";
    public static final String TAG_DIALOG_ACTION_REMOVE_SHARE = "TAG_DIALOG_ACTION_REMOVE_SHARE";

    public static final String TAG_DIALOG_DELETE_CONTEXT = "TAG_DIALOG_DELETE_CONTEXT";
    public static final String TAG_DIALOG_BATCH_DELETE_CONTEXT = "TAG_DIALOG_BATCH_DELETE_CONTEXT";
    public static final String TAG_DIALOG_BATCH_DELETE_SELECTED = "TAG_DIALOG_BATCH_DELETE_SELECTED";
    public static final String TAG_DIALOG_BATCH_EMPTY = "TAG_DIALOG_BATCH_EMPTY";
    public static final String TAG_DIALOG_BATCH_TERMINATE = "TAG_DIALOG_BATCH_TERMINATE";

    public static final String TAG_DIALOG_CANCEL_DOWNLOAD = "TAG_DIALOG_CANCEL_DOWNLOAD";
    public static final String TAG_DIALOG_CANCEL_UPLOAD = "TAG_DIALOG_CANCEL_UPLOAD";
    public static final String TAG_DIALOG_CANCEL_SINGLE_OPERATIONS = "TAG_DIALOG_CANCEL_SINGLE_OPERATIONS";
    public static final String TAG_DIALOG_CANCEL_BATCH_OPERATIONS = "TAG_DIALOG_CANCEL_BATCH_OPERATIONS";


    /*
     * Requests values
     * */
    private static final int ITEMS_PER_PAGE = 25;
    private static final int FILTERING_DELAY = 500;

    protected BaseFileProvider mFileProvider;

    /*
     * Handler for some common job
     * */
    private Handler mHandler = new Handler();

    /*
     * Saved values
     * */
    protected ModelExplorerStack mModelExplorerStack;
    protected String mFilteringValue;
    boolean mIsSubmitted;
    protected PlaceholderViews.Type mPlaceholderType;


    @Nullable
    protected String mDestFolderId;
    @Nullable
    private Uri mUploadUri;
    @Nullable
    protected ExplorerStackMap mOperationStack;

    /*
     * Modes
     * */
    protected boolean mIsFilteringMode;
    protected boolean mIsSelectionMode;
    protected boolean mIsFoldersMode;

    /*
     * Clicked/Checked and etc...
     * */
    @Nullable
    protected Item mItemClicked;
    private int mItemClickedPosition;
    protected boolean mIsContextClick;

    /*
     * Headers date
     * */
    private boolean mIsFolderHeader;
    private boolean mIsFileHeader;
    private boolean mIsCreatedHeader;
    private boolean mIsTodayHeader;
    private boolean mIsYesterdayHeader;
    private boolean mIsWeekHeader;
    private boolean mIsMonthHeader;
    private boolean mIsYearHeader;
    private boolean mIsMoreYearHeader;

    /*
     * Get docs
     * */
    private Disposable mGetDisposable;

    /*
     * Tasks for async job
     * */
    private Runnable mFilterRun;
    protected CompositeDisposable mDisposable = new CompositeDisposable();
    protected Disposable mBatchDisposable;
    private Disposable mDownloadDisposable;
    protected Disposable mUploadDisposable;
    private boolean mIsTerminate = false;
    private boolean mIsAccessDenied = false;


    /*
     * Download WorkManager
     */
    WorkManager mDownloadManager = WorkManager.getInstance();

    private boolean mIsMultipleDelete = false;

    @Inject
    protected Context mContext;
    @Inject
    protected PreferenceTool mPreferenceTool;
    @Inject
    protected OperationsState mOperationsState;
    @Inject
    protected AccountDao accountDao;
    @Inject
    protected NetworkSettings networkSettings;
    @Inject
    protected RecentDao recentDao;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
        mDisposable.dispose();
        mFileProvider = null;
    }

    public void getItemsById(String id) {
        setPlaceholderType(PlaceholderViews.Type.LOAD);
        mDisposable.add(mFileProvider.getFiles(id, getArgs(null))
                .subscribe(this::loadSuccess, this::fetchError));
    }

    public boolean refresh() {
        setPlaceholderType(PlaceholderViews.Type.LOAD);
        final String id = mModelExplorerStack.getCurrentId();
        if (id != null) {
            mDisposable.add(mFileProvider.getFiles(id, getArgs(mFilteringValue))
                    .subscribe(explorer -> {
                        mModelExplorerStack.refreshStack(explorer);
                        updateViewsState();
                        getViewState().onDocsRefresh(getListWithHeaders(mModelExplorerStack.last(), true));
                    }, this::fetchError));
            getViewState().onSwipeEnable(true);
            return true;
        }

        return false;
    }

    public boolean sortBy(@NonNull String value, boolean isRepeatedTap) {
        mPreferenceTool.setSortBy(value);

        if (isRepeatedTap) {
            reverseSortOrder();
        }
        return refresh();
    }

    protected void reverseSortOrder() {
        if (mPreferenceTool.getSortOrder().equals(ApiContract.Parameters.VAL_SORT_ORDER_ASC)) {
            mPreferenceTool.setSortOrder(ApiContract.Parameters.VAL_SORT_ORDER_DESC);
            getViewState().onReverseSortOrder(ApiContract.Parameters.VAL_SORT_ORDER_DESC);
        } else {
            mPreferenceTool.setSortOrder(ApiContract.Parameters.VAL_SORT_ORDER_ASC);
            getViewState().onReverseSortOrder(ApiContract.Parameters.VAL_SORT_ORDER_ASC);
        }
    }

    public boolean orderBy(@NonNull String value) {
        mPreferenceTool.setSortOrder(value);
        return refresh();
    }

    public boolean filter(@NonNull final String value, boolean isSubmitted) {
        if (mIsFilteringMode) {
            mIsSubmitted = isSubmitted;
            final String id = mModelExplorerStack.getCurrentId();
            if (id != null) {

                mFilteringValue = value;
                mDisposable.add(mFileProvider.getFiles(id, getArgs(value))
                        .debounce(FILTERING_DELAY, TimeUnit.MILLISECONDS)
                        .subscribe(explorer -> {
                            mModelExplorerStack.setFilter(explorer);
                            setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.SEARCH : PlaceholderViews.Type.NONE);
                            updateViewsState();
                            getViewState().onDocsFilter(getListWithHeaders(mModelExplorerStack.last(), true));
                        }, this::fetchError));

                getViewState().onSwipeEnable(true);
                return true;
            }
        }

        return false;
    }

    public void filterWait(@NonNull final String value) {
        if (mIsSubmitted) {
            return;
        }

        if (mFilterRun != null) {
            mHandler.removeCallbacks(mFilterRun);
        }

        mFilterRun = () -> filter(value, false);
        mHandler.postDelayed(mFilterRun, FILTERING_DELAY);
    }

    /*
     * Change docs
     * */

    public void createFolder(@Nullable final String title) {
        if (mPreferenceTool.getPortal() != null) {
            FirebaseUtils.addAnalyticsCreateEntity(mPreferenceTool.getPortal(), false, null);
        }

        final String id = mModelExplorerStack.getCurrentId();
        if (id != null) {
            final RequestCreate requestCreate = new RequestCreate();
            if (title != null && !title.equals("")) {
                requestCreate.setTitle(title);
            } else {
                requestCreate.setTitle(mContext.getString(R.string.dialogs_edit_create_docs));
            }

            mDisposable.add(mFileProvider.createFolder(id, requestCreate).subscribe(folder -> {
                setPlaceholderType(PlaceholderViews.Type.NONE);
                getViewState().onDialogClose();
                addFolder(folder);
            }, this::fetchError));
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS);
        }
    }

    private void renameFolder(final Item id, final String title) {
        mDisposable.add(mFileProvider.rename(id, title, null)
                .flatMap(item -> mFileProvider.getFiles(mModelExplorerStack.getCurrentId(), getArgs(null)))
                .subscribe(item -> {
                    getViewState().onDialogClose();
                    getViewState().onSnackBar(mContext.getString(R.string.list_context_rename_success));
                    loadSuccess(item);
                }, this::fetchError));
    }

    private void renameFile(final Item id, final String title, final int version) {
        mDisposable.add(mFileProvider.rename(id, title, version)
                .flatMap(item -> mFileProvider.getFiles(mModelExplorerStack.getCurrentId(), getArgs(null)))
                .subscribe(item -> {
                    getViewState().onDialogClose();
                    getViewState().onSnackBar(mContext.getString(R.string.list_context_rename_success));
                    loadSuccess(item);
                }, this::fetchError));
    }

    abstract public void getNextList();

    abstract public void createDocs(@NonNull final String title);

    abstract public void getFileInfo();

    abstract public void addRecent(CloudFile file);

    abstract protected void updateViewsState();

    public abstract void onContextClick(final Item item, final int position, final boolean isTrash);

    public abstract void onActionClick();

    private void loadSuccess(Explorer explorer) {
        mModelExplorerStack.addStack(explorer);
        updateViewsState();
        setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);

        final Explorer last = mModelExplorerStack.last();
        if (last != null) {
            getViewState().onDocsGet(getListWithHeaders(last, true));
        }
    }

    public void deleteItems() {
        List<Item> items = new ArrayList<>();
        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            items.addAll(mModelExplorerStack.getSelectedFiles());
            items.addAll(mModelExplorerStack.getSelectedFolders());
        } else if (mItemClicked != null) {
            items.add(mModelExplorerStack.getItemById(mItemClicked));
        }
        deleteRecent();
        showDialogProgress(true, TAG_DIALOG_CANCEL_BATCH_OPERATIONS);

        mBatchDisposable = mFileProvider.delete(items, null)
                .switchMap(operations -> getStatus())
                .subscribe(progress -> {
                            getViewState().onDialogProgress(100, progress);
                        },
                        this::fetchError,
                        () -> {
                            if (mModelExplorerStack.getCountSelectedItems() > 0) {
                                mModelExplorerStack.removeSelected();
                                getBackStack();
                            } else if (mItemClicked != null) {
                                mModelExplorerStack.removeItemById(mItemClicked.getId());
                            }
                            resetDatesHeaders();
                            setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);
                            getViewState().onDeleteBatch(getListWithHeaders(mModelExplorerStack.last(), true));
                            if (mIsMultipleDelete) {
                                onFileDeleteProtected();
                                mIsMultipleDelete = false;
                            } else {
                                onBatchOperations();
                            }
                        });
    }

    public boolean delete() {
        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            for (Item item : mModelExplorerStack.getSelectedFiles()) {
                mDisposable.add(isFileDeleteProtected(item).subscribe(isFileProtected -> {
                            if (isFileProtected) {
                                mIsMultipleDelete = true;
                                mModelExplorerStack.setSelectById(item, false);
                            }
                        }, this::fetchError)
                );
            }
            getViewState().onDialogQuestion(mContext.getString(R.string.dialogs_question_delete), null,
                    TAG_DIALOG_BATCH_DELETE_SELECTED);
        } else if (mItemClicked != null) {
            if (mItemClicked instanceof CloudFile) {
                mDisposable.add(
                        ((mFileProvider instanceof WebDavFileProvider || mFileProvider instanceof OneDriveFileProvider) ? ((OneDriveFileProvider) mFileProvider).fileInfo(mItemClicked, false) : (mFileProvider.fileInfo(mItemClicked))).subscribe(
                                response -> {
                                    if (!response.getFileStatus().isEmpty()) {
                                        int statusMask =
                                                Integer.parseInt(response.getFileStatus()) & ApiContract.FileStatus.IS_EDITING;
                                        if (statusMask != 0) {
                                            onFileDeleteProtected();
                                        } else {
                                            deleteItems();
                                        }
                                    } else {
                                        deleteItems();
                                    }
                                }, this::fetchError)
                );
            } else {
                deleteItems();
            }
        } else {
            getViewState().onSnackBar(mContext.getString(R.string.operation_empty_lists_data));
        }
        return true;
    }


    private Observable<Boolean> isFileDeleteProtected(Item item) {
        return Observable.just(mFileProvider.fileInfo(item))
                .flatMap(response -> response.flatMap(file -> {
                    int statusMask = Integer.parseInt(file.getFileStatus()) & ApiContract.FileStatus.IS_EDITING;
                    if (statusMask != 0) {
                        return Observable.just(Boolean.TRUE);
                    } else {
                        return Observable.just(Boolean.FALSE);
                    }
                }));
    }


    private void deleteRecent() {
//        List<CloudFile> files = mModelExplorerStack.getSelectedFiles();
//        List<Recent> recents = mAccountSqlTool.getRecent();
//        if (files != null) {
//            for (CloudFile file : files) {
//                for (Recent recent : recents) {
//                    if (recent.getIdFile() != null && recent.getIdFile().equals(file.getId())) {
//                        mAccountSqlTool.delete(recent);
//                    } else if (mItemClicked != null && recent.getIdFile() != null &&
//                            recent.getIdFile().equals(mItemClicked.getId())) {
//                        mAccountSqlTool.delete(recent);
//                    }
//                }
//            }
//        }
    }

    public boolean move() {
        mDestFolderId = mModelExplorerStack.getCurrentId();
        if (mDestFolderId != null && mOperationStack != null) {
            if (StringUtils.equals(mDestFolderId, mOperationStack.getCurrentId())) {
                getViewState().onError(mContext.getString(R.string.operation_error_move_to_same));
                return false;
            }

            if (mOperationStack.getSelectedItems() > 0) {
                if (isContainsPathInPath(mOperationStack.getSelectedFoldersIds())) {
                    getViewState().onError(mContext.getString(R.string.operation_error_move_to_same_subfolder));
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public void transfer(int conflict, boolean isMove) {
        List<Item> items = new ArrayList<>();
        CloudFolder destination = new CloudFolder();
        destination.setId(mDestFolderId);
        items.addAll(mOperationStack.getSelectedFiles());
        items.addAll(mOperationStack.getSelectedFolders());

        mBatchDisposable = mFileProvider.transfer(items, destination, conflict, isMove, false)
                .switchMap(operations -> getStatus())
                .subscribe(progress -> getViewState().onDialogProgress(100, progress),
                        this::fetchError,
                        () -> {
                            if (mOperationStack != null && !mOperationStack.getCurrentId().equalsIgnoreCase(mDestFolderId)) {
                                mOperationStack.setSelectionAll(false);
                                Explorer explorer = mOperationStack.getExplorer();
                                explorer.setDestFolderId(mDestFolderId);
                                if (mModelExplorerStack.getRootFolderType() == ApiContract.SectionType.CLOUD_USER) {
                                    explorer = setAccess(explorer);
                                }
                                mOperationsState.insert(mModelExplorerStack.getRootFolderType(), explorer);
                            }

                            setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);
                            onBatchOperations();
                        });

        showDialogWaiting(TAG_DIALOG_CANCEL_BATCH_OPERATIONS);
    }

    Observable<Integer> getStatus() {
        return Observable.<Integer>create(emitter -> {
            do {
                try {
                    if (mIsTerminate && mBatchDisposable.isDisposed()) {
                        terminateOperation();
                        break;
                    }
                    List<Operation> response = mFileProvider.getStatusOperation().getResponse();
                    if (!response.isEmpty()) {
                        Log.d(TAG, "getStatus: " + response.get(0).getId());
                        emitter.onNext(response.get(0).getProgress());
                    } else {
                        emitter.onComplete();
                        break;
                    }
                } catch (Exception e) {
                    emitter.onError(ProviderError.throwInterruptException());
                    break;
                }
            } while (true);
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void terminateOperation() {
        mDisposable.add(mFileProvider.terminate()
                .doOnSubscribe(disposable -> showDialogProgress(true, TAG_DIALOG_BATCH_TERMINATE))
                .subscribe(operations -> {
                    mIsTerminate = false;
                    onBatchOperations();
                }, this::fetchError));
    }

    public boolean copy() {
        String thisFolderId = null;
        if (mOperationStack != null) {
            thisFolderId = mOperationStack.getCurrentId();
        }
        mDestFolderId = mModelExplorerStack.getCurrentId();
        if (mDestFolderId != null && thisFolderId != null) {
            if (mDestFolderId.equals(mOperationStack.getCurrentId())) {
                getViewState().onError(mContext.getString(R.string.operation_error_move_to_same));
                return false;
            }
            if (mOperationStack.getSelectedItems() > 0) {
                if (isContainsPathInPath(mOperationStack.getSelectedFoldersIds())) {
                    getViewState().onError(mContext.getString(R.string.operation_error_move_to_same_subfolder));
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public void terminate() {
        if (mBatchDisposable != null) {
            mIsTerminate = true;
            mBatchDisposable.dispose();
            getViewState().onDialogClose();
            refresh();
        }
    }

    public void rename(@Nullable String title) {
        if (title != null && !title.isEmpty() && mItemClicked != null) {
            final Item item = mModelExplorerStack.getItemById(mItemClicked);
            if (item != null) {
                if (item instanceof CloudFolder) {
                    renameFolder(item, title);
                } else if (item instanceof CloudFile) {
                    renameFile(item, title, ((CloudFile) item).getNextVersion());
                }

                showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS);
            }
        }
    }

    /*
     * Downloads/Uploads
     * */

    public void createDownloadFile() {
        if (!mModelExplorerStack.getSelectedFiles().isEmpty() || !mModelExplorerStack.getSelectedFolders().isEmpty() || (mItemClicked instanceof CloudFolder)) {
            getViewState().onCreateDownloadFile(ApiContract.DOWNLOAD_ZIP_NAME);
        } else if (mItemClicked instanceof CloudFile) {
            getViewState().onCreateDownloadFile(mItemClicked.getTitle());
        }
    }

    public void download(@NonNull Uri downloadTo) {
        if (mPreferenceTool.getUploadWifiState() && !NetworkUtils.isWifiEnable(mContext)) {
            getViewState().onSnackBar(mContext.getString(R.string.upload_error_wifi));
            return;
        }

        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            downloadSelected(downloadTo);
            return;
        }

        if (mItemClicked instanceof CloudFolder) {
            bulkDownload(null, new ArrayList<>(Collections.singleton((CloudFolder) mItemClicked)), downloadTo);
            return;
        }

        if (mItemClicked != null && mItemClicked instanceof CloudFile) {
            startDownloadWork(downloadTo, mItemClicked.getId(), ((CloudFile) mItemClicked).getViewUrl(), null);
        }
    }

    private void downloadSelected(@NonNull Uri downloadTo) {
        final List<CloudFile> files = mModelExplorerStack.getSelectedFiles();
        final List<CloudFolder> folders = mModelExplorerStack.getSelectedFolders();

        if (mFileProvider instanceof WebDavFileProvider && !folders.isEmpty()) {
            getViewState().onError(mContext.getString(R.string.download_manager_folders_download));
            return;
        }
        bulkDownload(files, folders, downloadTo);


        if (isSelectionMode()) {
            getBackStack();
        }
    }

    @SuppressLint("MissingPermission")
    private void bulkDownload(@Nullable List<CloudFile> files, @Nullable List<CloudFolder> folders, @NonNull Uri downloadTo) {
        final List<String> filesIds = new ArrayList<>();
        final List<String> foldersIds = new ArrayList<>();
        if (files != null) {
            for (CloudFile file : files) {
                filesIds.add(file.getId());
            }
        }
        if (folders != null) {
            for (CloudFolder folder : folders) {
                foldersIds.add(folder.getId());
            }
        }

        final RequestDownload requestDownload = new RequestDownload();
        requestDownload.setFilesIds(filesIds);
        requestDownload.setFoldersIds(foldersIds);

        startDownloadWork(downloadTo, null, null, requestDownload);
    }

    private void startDownloadWork(Uri to, String id, String url, RequestDownload requestDownload) {

        final Gson gson = new Gson();

        final String stringRequestDownload = gson.toJson(requestDownload);

        final Data workData = new Data.Builder()
                .putString(DownloadWork.FILE_ID_KEY, id)
                .putString(DownloadWork.URL_KEY, url)
                .putString(DownloadWork.FILE_URI_KEY, to.toString())
                .putString(DownloadWork.REQUEST_DOWNLOAD, stringRequestDownload)
                .build();

        final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWork.class)
                .setInputData(workData)
                .build();

        mDownloadManager.enqueue(request);
    }

    public void cancelDownload() {
        if (mDownloadDisposable != null && !mDownloadDisposable.isDisposed()) {
            mDownloadDisposable.dispose();
            return;
        }
        if (mItemClicked instanceof CloudFile) {
            final CloudFile file = (CloudFile) mItemClicked;
            DownloadService.cancelDownload(file.getId());
        }
    }

    public void upload(final Uri uri, ClipData uris) {
        if (mPreferenceTool.getUploadWifiState() && !NetworkUtils.isWifiEnable(mContext)) {
            getViewState().onSnackBar(mContext.getString(R.string.upload_error_wifi));
            return;
        }
        final String id = mModelExplorerStack.getCurrentId();
        ArrayList<Uri> uriList = new ArrayList<>();
        if (id != null) {
            if (uri != null) {
                mUploadUri = uri;
                uriList.add(uri);
            } else if (uris != null) {
                for (int i = 0; i < uris.getItemCount(); i++) {
                    uriList.add(uris.getItemAt(i).getUri());
                }
            }
            if (uriList.size() > 20) {
                getViewState().onError(mContext.getString(R.string.upload_manager_error_number_files));
                return;
            }
            addUploadFiles(uriList, id);
        }
    }

    public void uploadToMy(final Uri uri) {
//        if (mAccountSqlTool.getAccountOnline() != null) {
//            if (mPreferenceTool.getUploadWifiState() && !NetworkUtils.isWifiEnable(mContext)) {
//                getViewState().onSnackBar(mContext.getString(R.string.upload_error_wifi));
//                return;
//            }
//            if (ContentResolverUtils.getSize(mContext, uri) > FileUtils.STRICT_SIZE) {
//                getViewState().onSnackBar(mContext.getString(R.string.upload_manager_error_file_size));
//                return;
//            }
//            if (mAccountSqlTool.getAccountOnline().isWebDav()) {
//                return;
//            }
//
//            final Data workData = new Data.Builder()
//                    .putString(UploadWork.TAG_UPLOAD_FILES, uri.toString())
//                    .putString(UploadWork.ACTION_UPLOAD_MY, UploadWork.ACTION_UPLOAD_MY)
//                    .putString(UploadWork.TAG_FOLDER_ID, null)
//                    .build();
//            startUpload(workData);
//        }
    }

    private void addUploadFiles(ArrayList<Uri> uriList, String id) {
        ArrayList<UploadFile> uploadFiles = new ArrayList<>();
        for (int i = 0; i < uriList.size(); i++) {
            Uri uri = uriList.get(i);
            if (ContentResolverUtils.getSize(mContext, uri) > FileUtils.STRICT_SIZE) {
                getViewState().onSnackBar(mContext.getString(R.string.upload_manager_error_file_size));
                continue;
            }
            UploadFile uploadFile = new UploadFile();
            uploadFile.setProgress(0);
            uploadFile.setUri(uri);
            uploadFile.setFolderId(id);
            uploadFile.setId(uri.getPath());
            uploadFile.setName(ContentResolverUtils.getName(mContext, uri));
            uploadFile.setSize(setSize(uri));
            uploadFiles.add(uploadFile);
        }
        if (!uploadFiles.isEmpty()) {
            UploadWork.putNewUploadFiles(id, uploadFiles);
            for (Uri uri : uriList) {
                final Data workData = new Data.Builder()
                        .putString(UploadWork.TAG_UPLOAD_FILES, uri.toString())
                        .putString(UploadWork.ACTION_UPLOAD_MY, UploadWork.ACTION_UPLOAD)
                        .putString(UploadWork.TAG_FOLDER_ID, id)
                        .build();
                startUpload(workData);
            }
            if (mModelExplorerStack.last().getItemsCount() == 0) {
                refresh();
            } else {
                getViewState().onAddUploadsFile(uploadFiles);
            }
        }
    }

    private void startUpload(Data data) {
        final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadWork.class)
                .addTag(data.getString(UploadWork.TAG_UPLOAD_FILES))
                .setInputData(data)
                .build();

        mDownloadManager.enqueue(request);
    }


    private String setSize(Uri uri) {
        long bytes = ContentResolverUtils.getSize(mContext, uri);
        return StringUtils.getFormattedSize(mContext, bytes);
    }

    public void cancelUpload() {
        if (mUploadDisposable != null && !mUploadDisposable.isDisposed()) {
            mUploadDisposable.dispose();
            return;
        }

        if (mDownloadDisposable != null && !mDownloadDisposable.isDisposed()) {
            mDownloadDisposable.dispose();
            return;
        }

        if (mUploadUri != null) {
            UploadService.cancelUpload(mUploadUri, "");
        }
    }

    void addFile(final CloudFile file) {
        file.setJustCreated(true);
        mModelExplorerStack.addFileFirst(file);
        getViewState().onDocsGet(getListWithHeaders(mModelExplorerStack.last(), true));
    }

    private void addFolder(final CloudFolder folder) {
        folder.setJustCreated(true);
        mModelExplorerStack.addFolderFirst(folder);
        getViewState().onDocsGet(getListWithHeaders(mModelExplorerStack.last(), true));
    }

    public void addFolderAndOpen(final CloudFolder folder, final int position) {
        addFolder(folder);
        openFolder(folder.getId(), position);
    }

    /*
     * ==============================================================================================
     * Common methods
     * ==============================================================================================
     * */
    protected Map<String, String> getArgs(@Nullable final String filteringValue) {
        final Map<String, String> args = new TreeMap<>();
        args.put(ApiContract.Parameters.ARG_COUNT, String.valueOf(ITEMS_PER_PAGE));
        args.put(ApiContract.Parameters.ARG_SORT_BY, mPreferenceTool.getSortBy());
        args.put(ApiContract.Parameters.ARG_SORT_ORDER, mPreferenceTool.getSortOrder());

        if (filteringValue != null) {
            args.put(ApiContract.Parameters.ARG_FILTER_BY, ApiContract.Parameters.VAL_FILTER_BY);
            args.put(ApiContract.Parameters.ARG_FILTER_OP, ApiContract.Parameters.VAL_FILTER_OP_CONTAINS);
            args.put(ApiContract.Parameters.ARG_FILTER_VALUE, filteringValue);
        }

        return args;
    }

    private void cancelGetRequests() {
        mDisposable.clear();
    }

    public void cancelSingleOperationsRequests() {
        mDisposable.clear();
    }

    void resetDatesHeaders() {
        mIsFolderHeader = false;
        mIsFileHeader = false;
        mIsCreatedHeader = false;
        mIsTodayHeader = false;
        mIsYesterdayHeader = false;
        mIsWeekHeader = false;
        mIsMonthHeader = false;
        mIsYearHeader = false;
        mIsMoreYearHeader = false;
    }

    /*
     * Batch operations
     * */
    public void moveSelected() {
        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            final ExplorerStack explorerStack = mModelExplorerStack.clone();
            if (explorerStack != null) {
                explorerStack.removeUnselected();
                getViewState().onBatchMove(explorerStack.getExplorer());
                getBackStack();
            }
            return;
        }

        getViewState().onError(mContext.getString(R.string.operation_empty_lists_data));
    }

    public void moveContext() {
        final Explorer explorer = mModelExplorerStack.last().clone();
        explorer.setCount(1);
        explorer.setTotal(1);

        if (mItemClicked instanceof CloudFolder) {
            final CloudFolder folder = ((CloudFolder) mItemClicked).clone();
            folder.setSelected(true);
            explorer.setFolders(new ArrayList<>(Collections.singletonList(folder)));
            explorer.getFiles().clear();
        } else if (mItemClicked instanceof CloudFile) {
            final CloudFile file = ((CloudFile) mItemClicked).clone();
            file.setSelected(true);
            explorer.setFiles(new ArrayList<>(Collections.singletonList(file)));
            explorer.getFolders().clear();
        }

        getViewState().onBatchMove(explorer);
    }

    public void copySelected() {
        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            final ExplorerStack explorerStack = mModelExplorerStack.clone();
            explorerStack.removeUnselected();
            getViewState().onBatchCopy(explorerStack.getExplorer());
        }

        getViewState().onError(mContext.getString(R.string.operation_empty_lists_data));
    }

    public void copyContext() {
        final Explorer explorer = mModelExplorerStack.last().clone();
        explorer.setCount(1);
        explorer.setTotal(1);

        if (mItemClicked instanceof CloudFolder) {
            final CloudFolder folder = ((CloudFolder) mItemClicked).clone();
            folder.setSelected(true);
            explorer.setFolders(new ArrayList<>(Collections.singletonList(folder)));
            explorer.getFiles().clear();
        } else if (mItemClicked instanceof CloudFile) {
            final CloudFile file = ((CloudFile) mItemClicked).clone();
            file.setSelected(true);
            explorer.setFiles(new ArrayList<>(Collections.singletonList(file)));
            explorer.getFolders().clear();
        }

        getViewState().onBatchCopy(explorer);
    }

    /*
     *  Get common list with headers
     * */
    @NonNull
    protected List<Entity> getListWithHeaders(@Nullable final Explorer explorer, final boolean isResetHeaders) {
        if (explorer != null) {
            final List<Entity> entityList = new ArrayList<>();

            // Reset headers, when new list
            if (isResetHeaders) {
                resetDatesHeaders();
            }

            if (UploadWork.getUploadFiles(mModelExplorerStack.getCurrentId()) != null &&
                    UploadWork.getUploadFiles(mModelExplorerStack.getCurrentId()).size() != 0) {
                entityList.add(new Header(mContext.getString(R.string.upload_manager_progress_title)));
                entityList.addAll(UploadWork.getUploadFiles(mModelExplorerStack.getCurrentId()));
            }

            // Set folders headers
            if (!explorer.getFolders().isEmpty()) {
                if (!mIsFolderHeader) {
                    mIsFolderHeader = true;
                    entityList.add(new Header(mContext.getString(R.string.list_headers_folder)));
                }
                entityList.addAll(explorer.getFolders());
            }

            // Set files headers
            if (!explorer.getFiles().isEmpty() && !mIsFoldersMode) {
                final String sortBy = mPreferenceTool.getSortBy();
                final String sortOrder = mPreferenceTool.getSortOrder();
                final List<CloudFile> fileList = explorer.getFiles();

                if (ApiContract.Parameters.VAL_SORT_BY_UPDATED.equals(sortBy)) { // For date sort add times headers
                    final long todayMs = TimeUtils.getTodayMs();
                    final long yesterdayMs = TimeUtils.getYesterdayMs();
                    final long weekMs = TimeUtils.getWeekMs();
                    final long monthMs = TimeUtils.getMonthMs();
                    final long yearMs = TimeUtils.getYearMs();
                    long itemMs;

                    // Set time headers
                    Collections.sort(fileList, (o1, o2) -> o1.getUpdated().compareTo(o2.getUpdated()));
                    if (sortOrder.equals(ApiContract.Parameters.VAL_SORT_ORDER_DESC)) {
                        Collections.reverse(fileList);
                    }

                    for (CloudFile item : fileList) {
                        itemMs = item.getUpdated().getTime();

                        // Check created property
                        if (item.isJustCreated()) {
                            if (!mIsCreatedHeader) {
                                mIsCreatedHeader = true;
                                entityList.add(new Header(mContext.getString(R.string.list_headers_created)));
                            }
                        } else {
                            // Check time intervals
                            if (itemMs >= todayMs) {
                                if (!mIsTodayHeader) {
                                    mIsTodayHeader = true;
                                    entityList.add(new Header(mContext.getString(R.string.list_headers_today)));
                                }
                            } else if (itemMs < todayMs && itemMs > yesterdayMs) {
                                if (!mIsYesterdayHeader) {
                                    mIsYesterdayHeader = true;
                                    entityList.add(new Header(mContext.getString(R.string.list_headers_yesterday)));
                                }
                            } else if (itemMs < yesterdayMs && itemMs > weekMs) {
                                if (!mIsWeekHeader) {
                                    mIsWeekHeader = true;
                                    entityList.add(new Header(mContext.getString(R.string.list_headers_week)));
                                }
                            } else if (itemMs < weekMs && itemMs > monthMs) {
                                if (!mIsMonthHeader) {
                                    mIsMonthHeader = true;
                                    entityList.add(new Header(mContext.getString(R.string.list_headers_month)));
                                }
                            } else if (itemMs < monthMs && itemMs > yearMs) {
                                if (!mIsYearHeader) {
                                    mIsYearHeader = true;
                                    entityList.add(new Header(mContext.getString(R.string.list_headers_year)));
                                }
                            } else if (itemMs < yearMs) {
                                if (!mIsMoreYearHeader) {
                                    mIsMoreYearHeader = true;
                                    entityList.add(new Header(mContext.getString(R.string.list_headers_more_year)));
                                }
                            }
                        }

                        entityList.add(item);
                    }
                } else { // In all other cases
                    if (!mIsFileHeader) {
                        mIsFileHeader = true;
                        entityList.add(new Header(mContext.getString(R.string.list_headers_files)));
                    }
                    entityList.addAll(fileList);
                }
            }

            if (mIsFilteringMode) {
                setPlaceholderType(entityList.isEmpty() ? PlaceholderViews.Type.SEARCH : PlaceholderViews.Type.NONE);
            } else {
                setPlaceholderType(entityList.isEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);
            }
            return entityList;
        }

        return Collections.emptyList();
    }


    /*
     * ==============================================================================================
     * States methods
     * ==============================================================================================
     * */


    public void initViews() {
        setPlaceholderType(mPlaceholderType);
        if (!mIsAccessDenied) {
            getViewState().onDocsGet(getListWithHeaders(mModelExplorerStack.last(), true));
        }
        refresh();
        updateOperationStack(mModelExplorerStack.getCurrentId());
    }

    public void initMenu() {
        if (mIsSelectionMode) {
            getViewState().onStateMenuSelection();
        } else {
            getViewState().onStateMenuDefault(mPreferenceTool.getSortBy(),
                    mPreferenceTool.getSortOrder().equalsIgnoreCase(ApiContract.Parameters.VAL_SORT_ORDER_ASC));
        }
    }

    public void initMenuSearch() {
        if (mIsFilteringMode && !mIsSelectionMode) {
            getViewState().onStateUpdateFilter(true, mFilteringValue);
        }
    }

    public void initMenuState() {
        getViewState().onStateMenuEnabled(!mModelExplorerStack.isListEmpty());
    }

    public boolean getBackStack() {
        cancelGetRequests();

        if (mIsSelectionMode) {
            setSelection(false);
            updateViewsState();
            return true;
        }

        if (mIsFilteringMode) {
            setFiltering(false);
            if (mModelExplorerStack.isStackFilter()) {
                popBackStack();
            }
            updateViewsState();
            return true;
        }

        popBackStack();
        updateViewsState();
        return !mModelExplorerStack.isStackEmpty();
    }

    private void popBackStack() {
        if (mModelExplorerStack.previous() != null) {
            final List<Entity> entities = getListWithHeaders(mModelExplorerStack.last(), true);
            setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);
            getViewState().onDocsGet(entities);
            getViewState().onScrollToPosition(mModelExplorerStack.getListPosition());
        }
    }

    private void updateOperationStack(@Nullable final String folderId) {
        final List<OperationsState.Operation> operations = mOperationsState.getOperations(mModelExplorerStack.getRootFolderType(), folderId);
        if (!operations.isEmpty()) {
            for (OperationsState.Operation item : operations) {
                switch (item.mOperationType) {
                    case INSERT:
                        refresh();
//                        addExplorer(item.mExplorer);
                        break;
                }
            }
        }
    }

    /*
     * Reset mods
     * */
    private void resetMods() {
        setFiltering(false);
        setSelection(false);
    }

    /*
     * Reset/Set views to filtering mode
     * */
    public void setFiltering(boolean isFiltering) {
        mIsSubmitted = false;
        if (mIsFilteringMode != isFiltering) {
            mIsFilteringMode = isFiltering;
            if (!isFiltering) {
                mFilteringValue = "";
            }
            getViewState().onStateUpdateFilter(isFiltering, mFilteringValue);
        }
    }

    public boolean isFilteringMode() {
        return mIsFilteringMode;
    }

    /*
     * Reset/Set model to selection mode
     * */
    public void setSelection(boolean isSelection) {
        if (mIsSelectionMode != isSelection) {
            mIsSelectionMode = isSelection;
            if (!isSelection) {
                mModelExplorerStack.setSelection(false);
            }
            getViewState().onStateUpdateSelection(isSelection);
        }
    }

    public void setSelectionAll() {
        setSelection(true);
        selectAll();
    }

    public void selectAll() {
        getViewState().onStateUpdateSelection(true);
        getViewState().onItemsSelection(String.valueOf(mModelExplorerStack.setSelection(true)));
    }

    public void deselectAll() {
        getViewState().onStateUpdateSelection(true);
        getViewState().onItemsSelection(String.valueOf(mModelExplorerStack.setSelection(false)));
        getBackStack();
    }

    public boolean isSelectionMode() {
        return mIsSelectionMode;
    }

    public void setFoldersMode(boolean foldersMode) {
        mIsFoldersMode = foldersMode;
    }


    /*
     * Get clicked item and do action with current state
     * */
    protected void onClickEvent(final Item item, final int position) {
        mItemClickedPosition = position;
        mItemClicked = mModelExplorerStack.getItemById(item);
    }

    public void onItemClick(final Item item, final int position) {
        onClickEvent(item, position);
        mIsContextClick = false;

        if (mIsSelectionMode) {
            final boolean isChecked = !mItemClicked.isSelected();
            mModelExplorerStack.setSelectById(item, isChecked);
            if (!isSelectedItemsEmpty()) {
                getViewState().onStateUpdateSelection(true);
                getViewState().onItemSelected(position, String.valueOf(mModelExplorerStack.getCountSelectedItems()));
            }
        } else {
            if (mItemClicked instanceof CloudFolder) {
                openFolder(mItemClicked.getId(), position);
            } else if (mItemClicked instanceof CloudFile) {
                getFileInfo();
            }
        }
    }

    protected boolean isSelectedItemsEmpty() {
        if (mModelExplorerStack.getCountSelectedItems() <= 0) {
            getBackStack();
            return true;
        } else {
            return false;
        }
    }

    Explorer getListMedia(final String clickedId) {
        final Explorer explorer = mModelExplorerStack.last().clone();
        final List<CloudFile> files = explorer.getFiles();
        explorer.setFolders(new ArrayList<>());
        if (files != null && !files.isEmpty()) {
            final ListIterator<CloudFile> listIterator = files.listIterator();
            while (listIterator.hasNext()) {
                final CloudFile file = listIterator.next();
                file.setClicked(file.getId().equalsIgnoreCase(clickedId));

                // Check for extensions
                if (!StringUtils.isImage(file.getFileExst()) && !StringUtils.isVideoSupport(file.getFileExst())) {
                    listIterator.remove();
                }
            }
        }

        return explorer;
    }

    void openFolder(final String id, final int position) {
        mModelExplorerStack.setListPosition(position);
        getViewState().onSwipeEnable(true);
        getItemsById(id);
    }

    /*
     * Get clicked context item and save it
     * */


    protected boolean isPdf() {
        if (mItemClicked instanceof CloudFile) {
            CloudFile file = (CloudFile) mItemClicked;
            return StringUtils.getExtension(file.getFileExst()).equals(StringUtils.Extension.PDF);
        }
        return false;
    }

    protected int getIconContext(String ext) {
        final StringUtils.Extension extension = StringUtils.getExtension(ext);
        switch (extension) {
            case DOC:
                return R.drawable.ic_type_text_document;
            case SHEET:
                return R.drawable.ic_type_spreadsheet;
            case PRESENTATION:
                return R.drawable.ic_type_presentation;
            case IMAGE:
            case IMAGE_GIF:
                return R.drawable.ic_type_image;
            case HTML:
            case EBOOK:
            case PDF:
                return R.drawable.ic_type_pdf;
            case VIDEO_SUPPORT:
                return R.drawable.ic_type_video;
            case UNKNOWN:
                return R.drawable.ic_type_file;
        }
        return R.drawable.ic_type_folder;
    }

    public void uploadPermission() {
        getViewState().onFileUploadPermission();
    }

    /*
     * Open in edit mode
     * */

    @Nullable
    public Item getItemClicked() {
        return mItemClicked;
    }

    @NonNull
    public String getItemTitle() {
        return mItemClicked != null ? StringUtils.getNameWithoutExtension(mItemClicked.getTitle()) : "";
    }

    public void setItemsShared(final boolean isShared) {
        if (mItemClicked != null) {
            mItemClicked.setShared(isShared);
        }
    }

    /*
     * Check on empty stack
     * */
    public void checkBackStack() {
        if (isBackStackEmpty()) {
            getViewState().onStateEmptyBackStack();
        } else {
            initViews();
        }
    }

    /*
     * Dialogs templates
     * */
    protected void showDialogWaiting(@Nullable String tag) {
        getViewState().onDialogWaiting(mContext.getString(R.string.dialogs_wait_title), tag);
    }

    protected void showDialogProgress(final boolean isHideButtons, @Nullable String tag) {
        getViewState().onDialogProgress(mContext.getString(R.string.dialogs_wait_title), isHideButtons, tag);
    }

    /*
     * On batch operation
     * */
    void onBatchOperations() {
        getViewState().onDialogClose();
        getViewState().onSnackBar(mContext.getString(R.string.operation_complete_message));
        getViewState().onDocsBatchOperation();
    }

    void onFileDeleteProtected() {
        getViewState().onDialogClose();
        if (mIsMultipleDelete) {
            getViewState().onSnackBar(mContext.getString(R.string.operation_complete_message) + mContext.getString(R.string.operation_delete_multiple));
        } else {
            getViewState().onSnackBar(mContext.getString(R.string.operation_delete_impossible));
        }
        getViewState().onDocsBatchOperation();
    }

    private boolean isBackStackEmpty() {
        return mModelExplorerStack.isStackEmpty();
    }

    public boolean isRoot() {
        return mModelExplorerStack.isRoot();
    }

    protected String getCurrentTitle() {
        final String title = mModelExplorerStack.getCurrentTitle();
        return title != null ? title : "";
    }

    protected String getItemClickedTitle() {
        return mItemClicked != null ? mItemClicked.getTitle() : "";
    }

    protected boolean isClickedItemFile() {
        return mItemClicked instanceof CloudFile;
    }

    protected boolean isClickedItemDocs() {
        return isClickedItemFile() && StringUtils.isDocument(((CloudFile) mItemClicked).getFileExst());
    }

    @Nullable
    protected Date getItemClickedDate() {
        return mItemClicked != null ? mItemClicked.getUpdated() : null;
    }

    protected void setPlaceholderType(PlaceholderViews.Type placeholderType) {
        mPlaceholderType = placeholderType;
        if (mIsFoldersMode && placeholderType.equals(PlaceholderViews.Type.EMPTY)) {
            mPlaceholderType = PlaceholderViews.Type.SUBFOLDER;
        }
        getViewState().onPlaceholder(mPlaceholderType);
    }

    public void setOperationExplorer(@NonNull Explorer explorer) {
        mOperationStack = new ExplorerStackMap(explorer);
    }

    private Explorer changeContent(final Explorer explorer) {
        final ListIterator<CloudFile> fileList = explorer.getFiles().listIterator();
        while (fileList.hasNext()) {
            final CloudFile file = fileList.next();
            final CloudFolder currentFolder = new CloudFolder();
            currentFolder.setId(mModelExplorerStack.getCurrentId());
            if (file.getFileType().isEmpty() && file.getFileExst().isEmpty()) {
                fileList.remove();

                mDisposable.add(mFileProvider.delete(Collections.singletonList(file), currentFolder)
                        .subscribe(operations -> mModelExplorerStack.refreshStack(explorer), this::fetchError));
            }
        }
        return explorer;
    }

    private boolean isContainsInPath(final String folderId) {
        final List<String> foldersPath = mModelExplorerStack.getPath();
        if (foldersPath != null) {
            for (String item : foldersPath) {
                if (StringUtils.equals(item, folderId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isContainsPathInPath(final List<String> path) {
        for (String item : path) {
            if (isContainsInPath(item)) {
                return true;
            }
        }

        return false;
    }

    @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
    protected void fetchError(Throwable throwable) {
        if (throwable.getMessage().equals(ProviderError.INTERRUPT)) {
            checkStatusOperation();
            return;
        } else if (throwable.getMessage().equals(ProviderError.FORBIDDEN)) {
            getViewState().onError(mContext.getString(R.string.dialogs_edit_forbidden_symbols));
            return;
        } else if (!NetworkUtils.isOnline(mContext) && throwable instanceof UnknownHostException) {
            getViewState().onError(mContext.getString(R.string.errors_connection_error));
            return;
        }
        getViewState().onDialogClose();
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            if (exception.response().code() == 412) {
                getViewState().onError(mContext.getString(R.string.operation_move_file_existing, exception.getSuppressed()[0].getMessage()));
                return;
            } else if (exception.response().code() >= ApiContract.HttpCodes.CLIENT_ERROR && exception.response().code() < ApiContract.HttpCodes.SERVER_ERROR) {
                if (!isRoot()) {
                    mModelExplorerStack.previous();
                    getItemsById(mModelExplorerStack.getCurrentId());
                }
            } else if (exception.response().code() >= ApiContract.HttpCodes.SERVER_ERROR) {
                setPlaceholderType(PlaceholderViews.Type.ACCESS);
                getViewState().onError(throwable.getMessage());
                return;
            } else if (exception.response().code() == 500) {
                getViewState().onSnackBar(mContext.getString(R.string.list_context_rename_success));
                refresh();
            }
        }
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            onErrorHandle(exception.response().errorBody(), exception.code());
        } else {
            onFailureHandle(throwable);
        }
    }

    protected void onErrorHandle(final ResponseBody responseBody, final int responseCode) {
        // Error values from server
        Integer errorCode = null;
        String errorMessage = null;
        String responseMessage = null;

        // Get error message
        try {
            responseMessage = responseBody.string();
        } catch (Exception e) {
            // No need handle
        }

        // Get Json error message
        if (responseMessage != null) {
            final JSONObject jsonObject = StringUtils.getJsonObject(responseMessage);
            if (jsonObject != null) {
                try {
                    errorCode = jsonObject.getInt(KEY_ERROR_CODE);
                    errorMessage = jsonObject.getJSONObject(KEY_ERROR_INFO).getString(KEY_ERROR_INFO_MESSAGE);
                } catch (JSONException e) {
                    Log.e(TAG, "onErrorHandle()", e);
                    FirebaseUtils.addCrash(e);
                }
            }
        }

        // Delete this block -- BEGIN --
        // Callback error
        if (responseCode >= ApiContract.HttpCodes.REDIRECTION && responseCode < ApiContract.HttpCodes.CLIENT_ERROR) {
            getViewState().onError(mContext.getString(R.string.errors_redirect_error) + responseCode);
        } else if (responseCode >= ApiContract.HttpCodes.CLIENT_ERROR && responseCode < ApiContract.HttpCodes.SERVER_ERROR) {
            // Add here new message for common errors
            switch (responseCode) {
                case ApiContract.HttpCodes.CLIENT_UNAUTHORIZED:
                    getViewState().onError(mContext.getString(R.string.errors_client_unauthorized));
                    return;
                case ApiContract.HttpCodes.CLIENT_FORBIDDEN:
                    if (errorMessage != null) {
                        if (errorMessage.contains(ApiContract.Errors.DISK_SPACE_QUOTA)) {
                            getViewState().onError(errorMessage);
                            return;
                        }
                    }

                    getViewState().onError(mContext.getString(R.string.errors_client_forbidden));
                    return;
                case ApiContract.HttpCodes.CLIENT_NOT_FOUND:
                    getViewState().onError(mContext.getString(R.string.errors_client_host_not_found));
                    return;
                case ApiContract.HttpCodes.CLIENT_PAYMENT_REQUIRED:
                    getViewState().onError(mContext.getString(R.string.errors_client_payment_required));
                    return;
            }

            getViewState().onError(mContext.getString(R.string.errors_client_error) + responseCode);
        } else if (responseCode >= ApiContract.HttpCodes.SERVER_ERROR) {

            if (errorMessage != null) {
                // Add here new message for common errors
                if (errorMessage.contains(ApiContract.Errors.AUTH)) {
                    getViewState().onError(mContext.getString(R.string.errors_server_auth_error));
                    return;
                }
            }

            getViewState().onError(mContext.getString(R.string.errors_server_error) + responseCode);
        } // Delete this block -- END --

//        // Uncomment this block, after added translation to server
//        // Callback error
//        if (errorMessage == null) {
//            if (responseCode >= Api.HttpCodes.REDIRECTION && responseCode < Api.HttpCodes.CLIENT_ERROR) {
//                getViewState().onError(mContext.getString(R.string.errors_redirect_error) + responseCode);
//            } else if (responseCode >= Api.HttpCodes.CLIENT_ERROR && responseCode < Api.HttpCodes.SERVER_ERROR) {
//                getViewState().onError(mContext.getString(R.string.errors_client_error) + responseCode);
//            } else if (responseCode >= Api.HttpCodes.SERVER_ERROR) {
//                getViewState().onError(mContext.getString(R.string.errors_server_error) + responseCode);
//            }
//        } else {
//            getViewState().onError(errorMessage);
//        }
    }

    /*
     * On fail connection
     * Add new handle of failure error here
     * */
    protected void onFailureHandle(Throwable t) {
        if (t instanceof NoConnectivityException) {
            getViewState().onError(mContext.getString(R.string.errors_connection_error));
        } else if (t instanceof UnknownHostException) {
            getViewState().onError(mContext.getString(R.string.errors_unknown_host_error));
        } else if (t instanceof SSLHandshakeException) {
            getViewState().onError(mContext.getString(R.string.errors_ssl_error));
        } else {
            FirebaseUtils.addCrash(BasePresenter.class.getSimpleName() + " - method - onFailureHandle()");
            FirebaseUtils.addCrash(t);
            getViewState().onError(mContext.getString(R.string.errors_unknown_error));
        }
    }

    private void checkStatusOperation() {
        onBatchOperations();
//        if (mFileProvider.getStatusOperation().getResponse().isEmpty()){
//            onBatchOperations();
//        }
    }

    public Explorer getStack() {
        return mModelExplorerStack.last();
    }

    private Explorer setAccess(Explorer explorer) {
        List<CloudFile> files = explorer.getFiles();
        List<CloudFolder> folders = explorer.getFolders();
        if (!files.isEmpty()) {
            for (CloudFile file : files) {
                file.setAccess(0);
            }
            explorer.setFiles(files);
        }
        if (!folders.isEmpty()) {
            for (CloudFolder folder : folders) {
                folder.setAccess(0);
            }
            explorer.setFolders(folders);
        }
        return explorer;
    }

    public void setAccessDenied() {
        mIsAccessDenied = true;
        mPlaceholderType = PlaceholderViews.Type.ACCESS;
    }

    public void clearStack() {
        mModelExplorerStack.clear();
    }

    public void recreateStack() {
        mModelExplorerStack = new ModelExplorerStack();
    }
}
