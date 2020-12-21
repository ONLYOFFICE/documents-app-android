package app.editors.manager.managers.retrofit;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.tools.PreferenceTool;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class WebDavInterceptor implements Interceptor {

    @Inject
    PreferenceTool mPreferenceTool;

    public WebDavInterceptor() {
        App.getApp().getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        String auth = chain.request().header(WebDavApi.HEADER_AUTHORIZATION);

        Request request;

        if (auth == null) {
            request = chain.request().newBuilder()
                    .addHeader(WebDavApi.HEADER_AUTHORIZATION, Credentials.basic(mPreferenceTool.getLogin(), mPreferenceTool.getPassword()))
                    .build();
        } else {
            request = chain.request();
        }

        return chain.proceed(request);
    }
}
