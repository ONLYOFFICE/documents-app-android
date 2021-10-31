package app.editors.manager.mvp.presenters.base

import android.content.Context
import android.util.Log
import app.documents.core.account.AccountDao
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.managers.exceptions.NoConnectivityException
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.views.base.BaseView
import app.editors.manager.mvp.views.base.BaseViewExt
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import lib.toolkit.base.managers.utils.StringUtils.getJsonObject
import moxy.MvpPresenter
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

abstract class BasePresenter<View : BaseView> : MvpPresenter<View>() {

    companion object {
        val TAG: String = BasePresenter::class.java.simpleName

        protected const val KEY_ERROR_CODE = "statusCode"
        protected const val KEY_ERROR_INFO = "error"
        protected const val KEY_ERROR_INFO_MESSAGE = "message"
    }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var preferenceTool: PreferenceTool

    @Inject
    lateinit var operationsState: OperationsState

    @Inject
    lateinit var networkSettings: NetworkSettings

    @Inject
    lateinit var accountDao: AccountDao

    init {
        RxJavaPlugins.setErrorHandler { throwable: Throwable ->
            if (throwable is UndeliverableException) {
                Log.d(TAG, "BasePresenter: " + throwable.message)
            } else {
                fetchError(throwable)
            }
        }
    }

    open fun cancelRequest() {}

    protected open fun fetchError(throwable: Throwable) {if (throwable is HttpException) {
            if (throwable.response()?.code() == ApiContract.HttpCodes.CLIENT_UNAUTHORIZED) {
                onUnauthorized(throwable.response()?.code() ?: -1)
            } else {
                onErrorHandle(throwable.response()?.errorBody(), throwable.code())
            }
        } else {
            onFailureHandle(throwable)
        }
    }

    /*
     * TODO when added server side translation - remove translates errors in app
     * On common error connection
     * Add new handle of connect error here
     * */
    protected fun onErrorHandle(responseBody: ResponseBody?, responseCode: Int) {
        // Error values from server
//        var errorCode: Int? = null
        var errorMessage: String? = null
        var responseMessage: String? = null

        // Get error message
        try {
            responseMessage = responseBody!!.string()
        } catch (e: Exception) {
            // No need handle
        }

        // Get Json error message
        if (responseMessage != null) {
            val jsonObject = getJsonObject(responseMessage)
            if (jsonObject != null) {
                try {
//                    errorCode = jsonObject.getInt(KEY_ERROR_CODE)
                    errorMessage = jsonObject.getJSONObject(KEY_ERROR_INFO).getString(KEY_ERROR_INFO_MESSAGE)
                } catch (e: JSONException) {
                    Log.e(TAG, "onErrorHandle()", e)
                    FirebaseUtils.addCrash(e)
                }
            }
        }

        // Delete this block -- BEGIN --
        // Callback error
        if (responseCode >= ApiContract.HttpCodes.REDIRECTION && responseCode < ApiContract.HttpCodes.CLIENT_ERROR) {
            viewState.onError(context.getString(R.string.errors_redirect_error) + responseCode)
        } else if (responseCode >= ApiContract.HttpCodes.CLIENT_ERROR && responseCode < ApiContract.HttpCodes.SERVER_ERROR) {
            // Add here new message for common errors
            when (responseCode) {
                ApiContract.HttpCodes.CLIENT_UNAUTHORIZED -> {
                    viewState.onError(context.getString(R.string.errors_client_unauthorized))
                    return
                }
                ApiContract.HttpCodes.CLIENT_FORBIDDEN -> {
                    if (errorMessage != null) {
                        if (errorMessage.contains(ApiContract.Errors.DISK_SPACE_QUOTA)) {
                            viewState.onError(errorMessage)
                            return
                        }
                    }
                    viewState.onError(context.getString(R.string.errors_client_forbidden))
                    return
                }
                ApiContract.HttpCodes.CLIENT_NOT_FOUND -> {
                    viewState.onError(context.getString(R.string.errors_client_host_not_found))
                    return
                }
                ApiContract.HttpCodes.CLIENT_PAYMENT_REQUIRED -> {
                    viewState.onError(context.getString(R.string.errors_client_payment_required))
                    return
                }
            }
            viewState.onError(context.getString(R.string.errors_client_error) + responseCode)
        } else if (responseCode >= ApiContract.HttpCodes.SERVER_ERROR) {
            if (errorMessage != null) {
                // Add here new message for common errors
                when {
                    errorMessage.contains(ApiContract.Errors.AUTH) -> {
                        viewState.onError(context.getString(R.string.errors_server_auth_error))
                        return
                    }
                    errorMessage.contains(ApiContract.Errors.AUTH_TOO_MANY_ATTEMPTS) -> {
                        viewState.onError(context.getString(R.string.errors_server_auth_too_many_attempts))
                        return
                    }
                }
            }
            viewState.onError(context.getString(R.string.errors_server_error) + responseCode)
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
    protected fun onFailureHandle(error: Throwable) {
        when (error) {
            is NoConnectivityException -> {
                viewState.onError(context.getString(R.string.errors_connection_error))
            }
            is UnknownHostException -> {
                viewState.onError(context.getString(R.string.errors_unknown_host_error))
            }
            is SSLHandshakeException -> {
                viewState.onError(context.getString(R.string.errors_ssl_error))
            }
            else -> {
                FirebaseUtils.addCrash(BasePresenter::class.java.simpleName + " - method - onFailureHandle()")
                FirebaseUtils.addCrash(error)
                viewState.onError(context.getString(R.string.errors_unknown_error))
            }
        }
    }

    fun onUnauthorized(responseCode: Int) {
        if (responseCode == ApiContract.HttpCodes.CLIENT_UNAUTHORIZED && viewState is BaseViewExt) {
            (viewState as BaseViewExt).onUnauthorized(
                context.getString(
                    R.string.login_not_authorization_info_token
                )
            )
        }
    }

}