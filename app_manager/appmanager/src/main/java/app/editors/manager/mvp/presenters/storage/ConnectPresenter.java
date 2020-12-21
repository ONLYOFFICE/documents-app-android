package app.editors.manager.mvp.presenters.storage;

import app.editors.manager.app.App;
import app.editors.manager.mvp.models.request.RequestStorage;
import app.editors.manager.mvp.models.response.ResponseFolder;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.storage.ConnectView;
import moxy.InjectViewState;
import retrofit2.Response;

@InjectViewState
public class ConnectPresenter extends BasePresenter<ConnectView, ResponseFolder> {

    public static final String TAG = ConnectPresenter.class.getSimpleName();

    public ConnectPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    public void connectService(final String token, final String providerKey, final String title, final boolean isCorporate) {
        final RequestStorage requestStorage = new RequestStorage();
        requestStorage.setToken(token);
        requestStorage.setProviderKey(providerKey);
        requestStorage.setCustomerTitle(title);
        requestStorage.setCorporate(isCorporate);
        connectStorage(requestStorage);
    }

    public void connectWebDav(final String providerKey, final String url, final String login,
                              final String password, final String title, final boolean isCorporate) {
        final RequestStorage requestStorage = new RequestStorage();
        requestStorage.setProviderKey(providerKey);
        requestStorage.setUrl(url);
        requestStorage.setLogin(login);
        requestStorage.setPassword(password);
        requestStorage.setCustomerTitle(title);
        requestStorage.setCorporate(isCorporate);
        connectStorage(requestStorage);
    }

    private void connectStorage(final RequestStorage requestStorage) {
        mRequestCall = mRetrofitTool.getApiWithPreferences().connectStorage(mPreferenceTool.getToken(), requestStorage);
        mRequestCall.enqueue(new BaseCallback() {

            @Override
            public void onSuccessResponse(Response<ResponseFolder> response) {
                getViewState().onConnect(response.body().getResponse());
            }
        });
    }

}
