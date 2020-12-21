package app.editors.manager.mvp.presenters.storage;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.Constants;
import app.editors.manager.managers.utils.StorageUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.account.Storage;
import app.editors.manager.mvp.views.storage.SelectView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import moxy.InjectViewState;
import moxy.MvpPresenter;
import retrofit2.HttpException;

@InjectViewState
public class SelectPresenter extends MvpPresenter<SelectView> {

    public static final String TAG = SelectPresenter.class.getSimpleName();

    private static final String[] STORAGE = new String[]{"Box",
            "DropboxV2",
            "GoogleDrive",
            "OneDrive",
            "SkyDrive",
            "Google",
            "SharePoint",
            "Yandex",
            "OwnCloud",
            "Nextcloud",
            "WebDav"};

    @Inject
    PreferenceTool mPreferenceTool;
    @Inject
    RetrofitTool mRetrofitTool;
    @Inject
    AccountSqlTool mAccountSqlTool;

    private CompositeDisposable mDisposable;

    public SelectPresenter() {
        App.getApp().getAppComponent().inject(this);
        mDisposable = new CompositeDisposable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) {
            mDisposable.clear();
        }
    }

    public void getStorage() {
        getViewState().showProgress(true);
        mDisposable.add(mRetrofitTool.getApiWithPreferences().getThirdpartyCapabilities(mPreferenceTool.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> new JSONObject(responseBody.string()))
                .map(this::collectListStorage)
                .subscribe(this::addStorage, this::fetchError));
    }

    private void fetchError(Throwable throwable) {
        if (throwable instanceof JSONException) {
            Log.d(TAG, "fetchError: " + throwable.getMessage());
            getStorageList(false);
        } else if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            if (exception.code() == Api.HttpCodes.CLIENT_NOT_FOUND) {
                getStorageList(false);
            } else {
                getViewState().onError(exception.message());
            }
        } else {
            Log.d(TAG, "fetchError: " + throwable.getMessage());
        }
    }

    private void addStorage(ArrayList<Storage> storages) {
        String portal = mPreferenceTool.getPortal();
        if (portal != null) {
            mAccountSqlTool.addStorage(mAccountSqlTool.getAccount(mPreferenceTool.getPortal(),
                    mPreferenceTool.getLogin(), mPreferenceTool.getSocialProvider()), storages);

            getStorageList(true);
        }
    }

    private ArrayList<Storage> collectListStorage(JSONObject jsonObject) throws JSONException {
        ArrayList<Storage> storages = new ArrayList<>();
        JSONArray array = jsonObject.getJSONArray("response");

        for (int i = 0; i < array.length(); i++) {
            JSONArray arrayString = array.getJSONArray(i);
            Storage storage = new Storage();

            for (int j = 0; j < arrayString.length(); j++) {
                if (j == 0) {
                    storage.setName(arrayString.getString(j));
                } else if (j == 1) {
                    storage.setClientId(arrayString.getString(j));
                } else if (j == 2) {
                    storage.setRedirectUrl(arrayString.getString(j));
                }

            }
            storages.add(storage);
        }
        return storages;
    }

    private void getStorageList(boolean isStorage) {
        mDisposable.add(Observable.just(buildStorageList(isStorage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(strings -> {
                    getViewState().showProgress(false);
                    getViewState().onUpdate(new ArrayList<>(strings));
                }));
    }

    private List<String> buildStorageList(boolean isStorage) {
        final Set<String> stringSet = new LinkedHashSet<>();
        if (isStorage) {
            final List<Storage> storage = mAccountSqlTool.getStorage();
            for (Storage s : storage) {
                for (String string : STORAGE) {
                    if (s.getName().equals(string)) {
                        stringSet.add(string);
                    }
                }
            }
        } else {
            stringSet.add(Api.Storage.GOOGLEDRIVE);
            stringSet.add(Api.Storage.DROPBOX);
            stringSet.add(Api.Storage.ONEDRIVE);
            stringSet.add(Api.Storage.YANDEX);
            stringSet.add(Api.Storage.BOXNET);
            stringSet.add(Api.Storage.SHAREPOINT);
            stringSet.add(Api.Storage.OWNCLOUD);
            stringSet.add(Api.Storage.NEXTCLOUD);
            stringSet.add(Api.Storage.WEBDAV);

        }
        return new ArrayList<>(stringSet);
    }

    public void getAccountStorage(String providerKey) {
        StorageUtils.Storage storage = checkStorage(providerKey);
        if (storage != null) {
            getViewState().showWebTokenFragment(storage);
        } else {
            String url = "";
            String title = "WebDav service";
            switch (providerKey) {
                case Api.Storage.YANDEX:
                    url = Constants.WevDav.URL_YANDEX;
                    title = App.getApp().getString(R.string.storage_select_yandex);
                    break;
                case Api.Storage.SHAREPOINT:
                    title = App.getApp().getString(R.string.storage_select_share_point);
                    break;
                case Api.Storage.OWNCLOUD:
                    title = App.getApp().getString(R.string.storage_select_own_cloud);
                    providerKey = Api.Storage.WEBDAV;
                    break;
                case Api.Storage.NEXTCLOUD:
                    title = App.getApp().getString(R.string.storage_select_next_cloud);
                    providerKey = Api.Storage.WEBDAV;
                    break;
            }
            getViewState().showWebDavFragment(providerKey, url, title);
        }

    }

    private StorageUtils.Storage checkStorage(String providerKey) {
        final AccountsSqlData account = mAccountSqlTool.getAccount(mPreferenceTool.getPortal(),
                mPreferenceTool.getLogin(), mPreferenceTool.getSocialProvider());
        if (account != null) {
            Collection<Storage> storages = account.getStorage();
            for (Storage s : storages) {
                if (s.getName().equals(providerKey)) {
                    return StorageUtils.getNewStorageInstance(providerKey, s);
                }
            }

            StorageUtils.Storage storage = StorageUtils.getStorageInstance(providerKey, mPreferenceTool.isPortalInfo());
            if (storage != null) {
                return storage;
            }
        }
        return null;
    }
}
