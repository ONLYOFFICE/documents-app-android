package app.editors.manager.mvp.presenters.storage;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.StorageUtils;
import app.editors.manager.mvp.models.account.Storage;
import app.editors.manager.mvp.views.storage.SelectView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class SelectPresenter extends MvpPresenter<SelectView> {

    public static final String TAG = SelectPresenter.class.getSimpleName();

    @Inject
    Context mContext;
    @Inject
    PreferenceTool mPreferenceTool;
    @Inject
    RetrofitTool mRetrofitTool;
    @Inject
    AccountSqlTool mAccountSqlTool;

    private CompositeDisposable mDisposable;
    private List<Storage> mStorages;

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

    public void getStorages() {
        getViewState().showProgress(true);
        mDisposable.add(mRetrofitTool.getApiWithPreferences().getThirdpartyCapabilities(mPreferenceTool.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> new JSONObject(responseBody.string()))
                .map(this::collectListStorage)
                .subscribe(list -> {
                    mStorages = list;
                    getViewState().onUpdate(getNames(list));
                    getViewState().showProgress(false);
                }, error -> {
                    getViewState().onError(error.getMessage());
                }));
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

    public void connect(String providerKey) {
        Storage storage = getStorage(providerKey);

        if (storage != null && storage.getClientId() != null) {
            getViewState().showWebTokenFragment(storage);
        } else {
            String url = "";
            String title = "WebDav service";
            switch (providerKey) {
                case Api.Storage.YANDEX:
                    url = StorageUtils.WevDav.URL_YANDEX;
                    title = mContext.getString(R.string.storage_select_yandex);
                    break;
                case Api.Storage.SHAREPOINT:
                    title = mContext.getString(R.string.storage_select_share_point);
                    break;
                case Api.Storage.OWNCLOUD:
                    title = mContext.getString(R.string.storage_select_own_cloud);
                    providerKey = Api.Storage.WEBDAV;
                    break;
                case Api.Storage.NEXTCLOUD:
                    title = mContext.getString(R.string.storage_select_next_cloud);
                    providerKey = Api.Storage.WEBDAV;
                    break;
            }

            getViewState().showWebDavFragment(providerKey, url, title);
        }

    }

    private Storage getStorage(String providerKey) {
        if (mStorages != null) {
            for (Storage storage : mStorages) {
                if (storage.getName().equals(providerKey)) {
                    return storage;
                }
            }
        }
        return null;
    }

    private List<String> getNames(ArrayList<Storage> list) {
        List<String> names = new ArrayList<>();
        for (Storage storage : list) {
            names.add(storage.getName());
        }
        return names;
    }
}
