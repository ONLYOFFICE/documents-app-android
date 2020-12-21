package app.editors.manager.managers.retrofit;

import androidx.annotation.NonNull;

import java.io.IOException;

import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.exceptions.NoConnectivityException;
import app.editors.manager.managers.tools.PreferenceTool;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BaseInterceptor implements Interceptor {

    private static final String KEY_AUTH = "Bearer ";

    private PreferenceTool mPreferenceTool;

    public BaseInterceptor() {
        mPreferenceTool = App.getApp().getAppComponent().getPreference();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        checkConnection();

        Request request = chain.request();
        int version = StringUtils.convertServerVersion(mPreferenceTool.getServerVersion());

        if (mPreferenceTool.getSslState() && version >= 10 && mPreferenceTool.getToken() != null) {
            request = request.newBuilder().header(Api.HEADER_AUTHORIZATION, KEY_AUTH +
                    request.header(Api.HEADER_AUTHORIZATION)).build();
        }

        return chain.proceed(request);
    }

    private void checkConnection() throws NoConnectivityException {
        if (!NetworkUtils.isOnline(App.getApp())) {
            throw new NoConnectivityException();
        }
    }

}
