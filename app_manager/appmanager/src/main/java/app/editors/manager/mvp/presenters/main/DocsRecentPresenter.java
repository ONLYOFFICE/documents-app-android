package app.editors.manager.mvp.presenters.main;

import android.content.ClipData;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.providers.WebDavFileProvider;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.account.Recent;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Current;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.models.ModelExplorerStack;
import app.editors.manager.mvp.views.main.DocsRecentView;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.PermissionUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import moxy.InjectViewState;
import retrofit2.HttpException;

@InjectViewState
public class DocsRecentPresenter extends DocsBasePresenter<DocsRecentView> {

    @Inject
    protected Context mContext;
    @Inject
    protected AccountSqlTool mAccountsSqlData;
    @Inject
    protected RetrofitTool mRetrofitTool;
    @Inject
    protected PreferenceTool mPreferenceTool;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private int mContextPosition;
    private Recent mItem;
    private Recent mContextItem;

    private AccountsSqlData mAccount;
    private app.editors.manager.mvp.models.explorer.File mTemp;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public DocsRecentPresenter() {
        App.getApp().getAppComponent().inject(this);
        mToken = mPreferenceTool.getToken();
        mModelExplorerStack = new ModelExplorerStack();
        mFilteringValue = "";
        mPlaceholderType = PlaceholderViews.Type.NONE;
        mIsContextClick = false;
        mIsFilteringMode = false;
        mIsSelectionMode = false;
        mIsFoldersMode = false;
        mAccount = mAccountsSqlData.getAccountOnline();
    }

    public void getRecentFiles() {
        List<Recent> list = mAccountsSqlData.getRecent();
        checkFiles(list);
    }

    private void checkFiles(List<Recent> list) {
        mDisposable.add(Observable.fromIterable(list)
                .filter(recent -> {
                    if (recent.isLocal()) {
                        File file = new File(recent.getPath());
                        if (!file.exists()) {
                            DocumentFile documentFile = DocumentFile.fromSingleUri(mContext, Uri.parse(recent.getPath()));
                            if (documentFile != null && !documentFile.exists()) {
                                mAccountSqlTool.delete(recent);
                            }
                            if (documentFile != null) {
                                return documentFile.exists();
                            }
                            return false;
                        }
                        return file.exists();
                    } else {
                        return true;
                    }
                }).toList()
                .subscribe(recents -> {
                    sortBy(mPreferenceTool.getSortBy(), mPreferenceTool.getSortOrder().equals(Api.Parameters.VAL_SORT_ORDER_ASC));
                }));
    }

    public void searchRecent(String newText) {
        mDisposable.add(Observable.fromIterable(mAccountsSqlData.getRecent())
                .filter(recent -> {
                    String name = recent.getName().toLowerCase();
                    return name.contains(newText);
                })
                .toList()
                .subscribe(list -> getViewState().updateFiles(new ArrayList<>(list))));
    }

    public void openFile(Recent recent, int position) {
        final String prefToken = mPreferenceTool.getToken();
        final String recentToken = recent.getAccountsSqlData().getToken();

        if (recentToken.equals(prefToken)) {
            mDisposable.add(Observable.fromCallable(() -> mRetrofitTool.getApiWithPreferences()
                    .getFileInfo(recent.getAccountsSqlData().getToken(), recent.getIdFile()).execute())
                    .map(response -> {
                        if (response.isSuccessful() && response.body() != null) {
                            return response.body().getResponse();
                        } else {
                            throw new HttpException(response);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> checkExt(file, recent, position),
                            throwable -> {
                                if (throwable instanceof HttpException) {
                                    if (((HttpException) throwable).code() == Api.HttpCodes.CLIENT_UNAUTHORIZED) {
                                        getViewState().onError(mContext.getString(R.string.errors_client_unauthorized));
                                    }
                                } else {
                                    getViewState().onError(mContext.getString(R.string.error_recent_account));
                                }

                            }));
        } else {
            getViewState().onError(mContext.getString(R.string.error_recent_enter_account));
        }
    }

    private void checkExt(app.editors.manager.mvp.models.explorer.File file, Recent recent, int position) {
        switch (StringUtils.getExtension(file.getFileExst())) {
            case DOC:
            case SHEET:
            case PRESENTATION:
            case PDF:
            case IMAGE:
            case IMAGE_GIF:
            case VIDEO_SUPPORT:
                addRecent(recent);
                getViewState().onMoveElement(recent, position);
                getViewState().openFile(file);
                break;
            default:
                getViewState().onError(mContext.getString(R.string.error_unsupported_format));
        }
    }

    public void loadMore(int itemCount) {
        mAccountsSqlData.getRecent();
    }

    public void sortBy(String parameters, boolean isAscending) {
        mPreferenceTool.setSortBy(parameters);
        setOrder(isAscending);
        switch (parameters) {
            case Api.Parameters.VAL_SORT_BY_CREATED:
                sortByCreated();
                break;
            case Api.Parameters.VAL_SORT_BY_TITLE:
                sortByName(isAscending);
                break;
            case Api.Parameters.VAL_SORT_BY_OWNER:
                sortByOwner(isAscending);
                break;
            case Api.Parameters.VAL_SORT_BY_UPDATED:
                sortByUpdated(isAscending);
                break;
            case Api.Parameters.VAL_SORT_BY_TYPE:
                sortByType(isAscending);
                break;
            case Api.Parameters.VAL_SORT_BY_SIZE:
                sortBySize(isAscending);
                break;
        }
    }

    private void sortByCreated() {
        List<Recent> list = mAccountsSqlData.getRecent();
        Collections.reverse(list);
        getViewState().updateFiles(new ArrayList<>(list));
    }

    private void sortBySize(boolean isAscending) {
        List<Recent> list = mAccountsSqlData.getRecent();
        Collections.sort(list, (o1, o2) -> Long.compare(o1.getSize(), o2.getSize()));
        if (!isAscending) {
            Collections.reverse(list);
        }
        getViewState().updateFiles(new ArrayList<>(list));
    }

    private void sortByType(boolean isAscending) {
        List<Recent> list = mAccountsSqlData.getRecent();
        Collections.sort(list, (o1, o2) -> StringUtils.getExtensionFromPath(o1.getName())
                .compareToIgnoreCase(StringUtils.getExtensionFromPath(o2.getName())));
        if (!isAscending) {
            Collections.reverse(list);
        }
        getViewState().updateFiles(new ArrayList<>(list));
    }

    private void sortByUpdated(boolean isAscending) {
        List<Recent> list = mAccountsSqlData.getRecent();
        Collections.sort(list, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));
        if (!isAscending) {
            Collections.reverse(list);
        }
        getViewState().updateFiles(new ArrayList<>(list));
    }

    private void sortByOwner(boolean isAscending) {
        List<Recent> list = mAccountsSqlData.getRecent();
        Collections.sort(list, (o1, o2) -> {
            if (o1.getAccountsSqlData() != null && o2.getAccountsSqlData() != null) {
                return o1.getAccountsSqlData().getId().compareTo(o2.getAccountsSqlData().getId());
            } else {
                return Boolean.compare(o1.isLocal(), o2.isLocal());
            }
        });
        if (isAscending) {
            Collections.reverse(list);
        }
        getViewState().updateFiles(new ArrayList<>(list));
    }

    private void sortByName(boolean isAscending) {
        List<Recent> list = mAccountsSqlData.getRecent();
        Collections.sort(list, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        if (!isAscending) {
            Collections.reverse(list);
        }
        getViewState().updateFiles(new ArrayList<>(list));
    }

    private void addRecent(Recent recent) {
        recent.setDate(new Date());
        mAccountsSqlData.addRecent(recent);
    }

    public void reverseList(List<Entity> itemList, boolean isAscending) {
        setOrder(isAscending);
        if (itemList != null) {
            ArrayList<Entity> reverseList = new ArrayList<>(itemList);
            Collections.reverse(reverseList);
            getViewState().updateFiles(reverseList);
        }
    }

    public void contextClick(Recent recent, int position) {
        mContextItem = recent;
        mContextPosition = position;
        ContextBottomDialog.State state = new ContextBottomDialog.State();
        state.mTitle = recent.getName();
        if (!recent.isLocal()) {
            state.mInfo = recent.getAccountsSqlData().getPortal() +
                    mContext.getString(R.string.placeholder_point) + TimeUtils.formatDate(recent.getDate());
        } else {
            state.mInfo = TimeUtils.formatDate(recent.getDate());
        }
        state.mIconResId = getIconContext(StringUtils.getExtensionFromPath(recent.getName()));
        state.mIsRecent = true;
        if (recent.isLocal()) {
            state.mIsLocal = true;
        }
        getViewState().onContextShow(state);
    }


    @Override
    public void upload(Uri uri, ClipData uris) {
        AccountsSqlData account = mAccountsSqlData.getAccountOnline();
        if (account != null && account.isWebDav()) {
            WebDavFileProvider webDavFileProvider = new WebDavFileProvider(WebDavApi.getApi(account.getScheme() + account.getPortal()),
                    WebDavApi.Providers.valueOf(account.getWebDavProvider()));

            app.editors.manager.mvp.models.explorer.File item = new app.editors.manager.mvp.models.explorer.File();
            item.setId(mItem.getId());
            item.setTitle(mItem.getName());
            item.setWebUrl(mItem.getPath());
            item.setFolderId(mItem.getId().substring(0, mItem.getId().lastIndexOf('/') + 1));
            item.setFileExst(StringUtils.getExtensionFromPath(mItem.getName()));

            mDisposable.add(webDavFileProvider.fileInfo(item, false)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(file -> {
                        addRecent(file);
                        return webDavFileProvider.upload(file.getFolderId(), Collections.singletonList(uri));
                    })
                    .subscribe(integer -> {
                    }, this::fetchError, () -> {
                        deleteTempFile();
                        getViewState().onSnackBar(mContext.getString(R.string.upload_manager_complete));
                    }));

        }
    }

    public void deleteRecent() {
        mAccountsSqlData.delete(mContextItem);
        getViewState().onDeleteItem(mContextPosition);
    }

    private Explorer getImages(File clickedFile) {
        Explorer explorer = new Explorer();
        String extension = StringUtils.getExtensionFromPath(clickedFile.getName());
        app.editors.manager.mvp.models.explorer.File explorerFile = new app.editors.manager.mvp.models.explorer.File();

        explorerFile.setPureContentLength(clickedFile.length());
        explorerFile.setWebUrl(clickedFile.getAbsolutePath());
        explorerFile.setFileExst(extension);
        explorerFile.setTitle(clickedFile.getName());
        explorerFile.setClicked(true);

        Current current = new Current();
        current.setTitle(clickedFile.getName());
        current.setFilesCount("1");
        explorer.setCurrent(current);
        explorer.setFiles(Collections.singletonList(explorerFile));
        return explorer;
    }

    private Explorer getWebDavImage(Recent recent) {
        Explorer explorer = new Explorer();
        String extension = StringUtils.getExtensionFromPath(recent.getName());
        app.editors.manager.mvp.models.explorer.File explorerFile = new app.editors.manager.mvp.models.explorer.File();

        explorerFile.setPureContentLength(recent.getSize());
        explorerFile.setId(recent.getId());
        explorerFile.setFileExst(extension);
        explorerFile.setTitle(recent.getName());
        explorerFile.setClicked(true);

        Current current = new Current();
        current.setTitle(recent.getName());
        current.setFilesCount("1");
        explorer.setCurrent(current);
        explorer.setFiles(Collections.singletonList(explorerFile));
        return explorer;

    }

    public void fileClick(Recent recent, int position) {
        mItem = recent;
        final AccountsSqlData recentAccount = recent.getAccountsSqlData();
        final AccountsSqlData onlineAccount = mAccountsSqlData.getAccountOnline();

        if (!recent.isLocal()) {

            if (onlineAccount == null) {
                getViewState().onError(mContext.getString(R.string.error_recent_enter_account));
                return;
            } else {
                final String onlineToken = onlineAccount.getToken();
                final String recentToken = recentAccount.getToken();

                if (!recentAccount.isOnline()) {
                    getViewState().onError(mContext.getString(R.string.error_recent_enter_account));
                } else if (recentAccount.isWebDav() && onlineAccount.getPassword().equals(recentAccount.getPassword())) {
                    openWebDavFile(recent, position);
                } else if (recentToken.equals(onlineToken)) {
                    openFile(recent, position);
                } else {
                    getViewState().onError(mContext.getString(R.string.error_recent_account));
                }
            }

        } else {
            if (recent.getPath() != null && !recent.getPath().equals("")) {
                Uri uri = Uri.parse(recent.getPath());
                if (uri.getScheme() != null) {
                    openLocalFile(uri);
                } else {
                    openLocalFile(Uri.fromFile(new File(recent.getPath())));
                }
            }
        }

        addRecent(recent);
        getViewState().onMoveElement(recent, position);
    }

    private void openLocalFile(Uri uri) {
        String name = ContentResolverUtils.getName(mContext, uri);
        switch (StringUtils.getExtension(StringUtils.getExtensionFromPath(name.toLowerCase()))) {
            case DOC:
                getViewState().onOpenDocs(uri);
                break;
            case SHEET:
                getViewState().onOpenCells(uri);
                break;
            case PRESENTATION:
                getViewState().onOpenPresentation(uri);
                break;
            case PDF:
                getViewState().onOpenPdf(uri);
                break;
            case IMAGE:
            case IMAGE_GIF:
            case VIDEO_SUPPORT:
                getViewState().onOpenMedia(getImages(new java.io.File(uri.getPath())), false);
                break;
            default:
                getViewState().onError(mContext.getString(R.string.error_unsupported_format));
        }
    }

    private void openWebDavFile(Recent recent, int position) {
        AccountsSqlData account = recent.getAccountsSqlData();
        WebDavFileProvider mWebDavFileProvider = new WebDavFileProvider(WebDavApi.getApi(account.getScheme() + account.getPortal()),
                WebDavApi.Providers.valueOf(account.getWebDavProvider()));

        app.editors.manager.mvp.models.explorer.File item = new app.editors.manager.mvp.models.explorer.File();
        item.setTitle(recent.getName());
        item.setId(recent.getId());
        item.setFileExst(StringUtils.getExtensionFromPath(recent.getName()));
        item.setPureContentLength(recent.getSize());

        if (StringUtils.isImage(item.getFileExst())) {
            getViewState().onOpenMedia(getWebDavImage(recent), true);

        } else {
            mDisposable.add(mWebDavFileProvider.fileInfo(item)
                    .doOnSubscribe(disposable -> getViewState().onDialogWaiting(mContext.getString(R.string.dialogs_wait_title), null))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        mTemp = file;
                        getViewState().onDialogClose();
                        openLocalFile(Uri.parse(file.getWebUrl()));
                        getViewState().onMoveElement(recent, position);
                    }, this::fetchError));
        }

    }

    @Override
    public void addRecent(app.editors.manager.mvp.models.explorer.File file) {
        if (mAccount != null && mAccount.isWebDav()) {
            mAccountSqlTool.addRecent(file.getViewUrl(), file.getWebUrl(), file.getTitle(),
                    file.getPureContentLength(), false, true, new Date(), mAccount);
        }

    }

    public void clearRecent() {
        List<Recent> recents = mAccountSqlTool.getRecent();
        for (Recent recent : recents) {
            mAccountSqlTool.delete(recent);
        }
        recents.clear();
        getViewState().updateFiles(new ArrayList<>(recents));
    }

    @Override
    public void onContextClick(Item item, int position, boolean isTrash) {
        // stub
    }

    @Override
    public void getNextList() {
        // stub
    }

    @Override
    public void createDocs(@NonNull String title) {
        // stub
    }

    @Override
    public void getFileInfo() {
        // stub
    }

    @Override
    protected void updateViewsState() {
        // stub
    }

    @Override
    public void onActionClick() {
        // stub
    }

    public void deleteTempFile() {
        if (mTemp != null && PermissionUtils.checkReadWritePermission(mContext)) {
            Uri uri = Uri.parse(mTemp.getWebUrl());
            if (uri.getPath() != null) {
                FileUtils.asyncDeletePath(uri.getPath());
            }
        }
        mTemp = null;
    }

    private void setOrder(boolean isAsc) {
        if (isAsc) {
            mPreferenceTool.setSortOrder(Api.Parameters.VAL_SORT_ORDER_ASC);
        } else {
            mPreferenceTool.setSortOrder(Api.Parameters.VAL_SORT_ORDER_DESC);
        }
    }
}
