package app.editors.manager.mvp.presenters.base;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.managers.exceptions.NoConnectivityException;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.tools.AccountManagerTool;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.mvp.models.states.OperationsState;
import app.editors.manager.mvp.views.base.BaseView;
import app.editors.manager.mvp.views.base.BaseViewExt;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.MvpPresenter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;

public abstract class BasePresenter<View extends BaseView, Response> extends MvpPresenter<View> {

    public static final String TAG = BasePresenter.class.getSimpleName();

    protected static final String KEY_ERROR_CODE = "statusCode";
    protected static final String KEY_ERROR_INFO = "error";
    protected static final String KEY_ERROR_INFO_MESSAGE = "message";

    @Inject
    protected Context mContext;
    @Inject
    protected PreferenceTool mPreferenceTool;
    @Inject
    protected RetrofitTool mRetrofitTool;
    @Inject
    protected AccountManagerTool mAccountManagerTool;
    @Inject
    protected AccountSqlTool mAccountSqlTool;
    @Inject
    protected OperationsState mOperationsState;

    protected RetrofitTool mRetrofitApi;

    /*
     * TODO remove
     * */
    protected Call<Response> mRequestCall;

    public BasePresenter() {
        super();
        mRetrofitApi = new RetrofitTool(mContext);
        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException) {
                Log.d(TAG, "BasePresenter: " + throwable.getMessage());
            } else {
                fetchError(throwable);
            }
        });
    }

    public void cancelRequest() {
        if (mRequestCall != null) {
            mRequestCall.cancel();
        }
    }

    /*
     * Init retrofit for current portal address
     * */
    protected void initRetrofitPref(final String portal) throws UrlSyntaxMistake {
        try {
            mPreferenceTool.setPortal(portal);
            mRetrofitTool.initWithPreferences();
        } catch (UrlSyntaxMistake e) {
            getViewState().onError(mContext.getString(R.string.login_api_init_portal_error));
            throw new UrlSyntaxMistake(e);
        }
    }

    protected boolean isConfigConnection(final Throwable t) {
        if (t instanceof SSLHandshakeException && !mPreferenceTool.getSslCiphers() && mPreferenceTool.isHttpsConnect() &&
                android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            mPreferenceTool.setSslCiphers(true);
            return true;
        } else if ((t instanceof ConnectException || t instanceof SocketTimeoutException || t instanceof SSLHandshakeException ||
                t instanceof SSLPeerUnverifiedException) && mPreferenceTool.isHttpsConnect()) {
            mPreferenceTool.setSslCiphers(false);
            mPreferenceTool.setScheme(Api.SCHEME_HTTP);
            return true;
        }

        return false;
    }

    protected void fetchError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            onErrorHandle(exception.response().errorBody(), exception.code());
        } else {
            onFailureHandle(throwable);
        }
    }

    /*
     * TODO when added server side translation - remove translates errors in app
     * On common error connection
     * Add new handle of connect error here
     * */
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
        if (responseCode >= Api.HttpCodes.REDIRECTION && responseCode < Api.HttpCodes.CLIENT_ERROR) {
            getViewState().onError(mContext.getString(R.string.errors_redirect_error) + responseCode);
        } else if (responseCode >= Api.HttpCodes.CLIENT_ERROR && responseCode < Api.HttpCodes.SERVER_ERROR) {
            // Add here new message for common errors
            switch (responseCode) {
                case Api.HttpCodes.CLIENT_UNAUTHORIZED:
                    getViewState().onError(mContext.getString(R.string.errors_client_unauthorized));
                    return;
                case Api.HttpCodes.CLIENT_FORBIDDEN:
                    if (errorMessage != null) {
                        if (errorMessage.contains(Api.Errors.DISK_SPACE_QUOTA)) {
                            getViewState().onError(errorMessage);
                            return;
                        }
                    }

                    getViewState().onError(mContext.getString(R.string.errors_client_forbidden));
                    return;
                case Api.HttpCodes.CLIENT_NOT_FOUND:
                    getViewState().onError(mContext.getString(R.string.errors_client_host_not_found));
                    return;
                case Api.HttpCodes.CLIENT_PAYMENT_REQUIRED:
                    getViewState().onError(mContext.getString(R.string.errors_client_payment_required));
                    return;
            }

            getViewState().onError(mContext.getString(R.string.errors_client_error) + responseCode);
        } else if (responseCode >= Api.HttpCodes.SERVER_ERROR) {

            if (errorMessage != null) {
                // Add here new message for common errors
                if (errorMessage.contains(Api.Errors.AUTH)) {
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

    /*
     * TODO remove and replace with CommonCallback
     * Base for presenter with main type response
     * */
    protected abstract class BaseCallback extends CommonCallback<Response> {

        @Override
        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
            if (mRequestCall != null && !mRequestCall.isCanceled()) {
                super.onResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<Response> call, Throwable t) {
            if (mRequestCall != null && !mRequestCall.isCanceled()) {
                super.onFailure(call, t);
            }
        }
    }

    /*
     * Common for all responses
     * */
    protected abstract class CommonCallback<T> implements Callback<T> {

        @Nullable
        protected Call<T> mRequest;

        public CommonCallback() {
        }

        public CommonCallback(Call<T> request) {
            mRequest = request;
        }

        @Override
        public void onResponse(Call<T> call, retrofit2.Response<T> response) {
            if (mRequest != null && mRequest.isCanceled()) {
                return;
            }

            if (response.isSuccessful()) {
                onSuccessResponse(response);
            } else if (!onUnauthorized(response.code())) {
                onErrorResponse(response);
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (mRequest != null && mRequest.isCanceled()) {
                return;
            }
            onFailResponse(t);
        }

        public abstract void onSuccessResponse(retrofit2.Response<T> response);

        public void onErrorResponse(retrofit2.Response<T> response) {
            onErrorHandle(response.errorBody(), response.code());
        }

        public void onFailResponse(Throwable t) {
            onFailureHandle(t);
        }

        public boolean onUnauthorized(final int responseCode) {
            if (responseCode == Api.HttpCodes.CLIENT_UNAUTHORIZED && getViewState() instanceof BaseViewExt) {
                ((BaseViewExt) getViewState()).onUnauthorized(mContext.getString(R.string.login_not_authorization_info,
                        mPreferenceTool.getLogin(), mPreferenceTool.getPortal()));
                return true;
            }

            return false;
        }

        @Nullable
        public Call<T> getRequest() {
            return mRequest;
        }

    }

}
