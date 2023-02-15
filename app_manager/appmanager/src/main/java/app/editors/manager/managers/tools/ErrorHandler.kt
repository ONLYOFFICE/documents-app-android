package app.editors.manager.managers.tools

import android.util.Log
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.managers.exceptions.NoConnectivityException
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.models.error.AppErrors
import app.editors.manager.mvp.presenters.base.BasePresenter
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

class ErrorHandler @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) {

    companion object {
        private const val KEY_ERROR_CODE = "statusCode"
        private const val KEY_ERROR_INFO = "error"
        private const val KEY_ERROR_INFO_MESSAGE = "message"
    }

    fun fetchError(throwable: Throwable, handler: (error: AppErrors) -> Unit) {
        if (throwable is HttpException) {
            if (throwable.response()?.code() == ApiContract.HttpCodes.CLIENT_UNAUTHORIZED) {
                onUnauthorized(throwable.response()?.code() ?: -1, handler)
            } else {
                onErrorHandle(throwable.response()?.errorBody(), throwable.code(), handler)
            }
        } else {
            onFailureHandle(throwable, handler)
        }
    }


    private fun onErrorHandle(responseBody: ResponseBody?, responseCode: Int, handler: (error: AppErrors) -> Unit) {
        // Error values from server
        var errorCode: Int? = null
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
            val jsonObject = StringUtils.getJsonObject(responseMessage)
            if (jsonObject != null) {
                try {
                    errorCode = jsonObject.getInt(KEY_ERROR_CODE)
                    errorMessage = jsonObject.getJSONObject(KEY_ERROR_INFO).getString(KEY_ERROR_INFO_MESSAGE)
                } catch (e: JSONException) {
                    Log.e(BasePresenter.TAG, "onErrorHandle()", e)
                    FirebaseUtils.addCrash(e)
                }
            }
        }

        // Delete this block -- BEGIN --
        // Callback error
        if (responseCode >= ApiContract.HttpCodes.REDIRECTION && responseCode < ApiContract.HttpCodes.CLIENT_ERROR) {
            handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_redirect_error) + responseCode))
        } else if (responseCode >= ApiContract.HttpCodes.CLIENT_ERROR && responseCode < ApiContract.HttpCodes.SERVER_ERROR) {
            // Add here new message for common errors
            when (responseCode) {
                ApiContract.HttpCodes.CLIENT_UNAUTHORIZED -> {
                    handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_client_unauthorized)))
                    return
                }
                ApiContract.HttpCodes.CLIENT_FORBIDDEN -> {
                    if (errorMessage != null) {
                        if (errorMessage.contains(ApiContract.Errors.DISK_SPACE_QUOTA)) {
                            handler(AppErrors.HttpError(errorMessage))
                            return
                        }
                    }
                    handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_client_forbidden)))
                    return
                }
                ApiContract.HttpCodes.CLIENT_NOT_FOUND -> {
                    handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_client_host_not_found)))
                    return
                }
                ApiContract.HttpCodes.CLIENT_PAYMENT_REQUIRED -> {
                    handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_client_payment_required)))
                    return
                }
            }
            handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_client_error) + responseCode))
        } else if (responseCode >= ApiContract.HttpCodes.SERVER_ERROR) {
            if (errorMessage != null) {
                // Add here new message for common errors
                if (errorMessage.contains(ApiContract.Errors.AUTH)) {
                    handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_server_auth_error)))
                    return
                }
            }
            handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_server_error) + responseCode))
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

    private fun onFailureHandle(error: Throwable, handler: (error: AppErrors) -> Unit) {
        when (error) {
            is NoConnectivityException -> {
                handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_connection_error)))
            }
            is UnknownHostException -> {
                handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_unknown_host_error)))
            }
            is SSLHandshakeException -> {
                handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_ssl_error)))
            }
            else -> {
                FirebaseUtils.addCrash(ErrorHandler::class.java.simpleName + " - method - onFailureHandle()")
                FirebaseUtils.addCrash(error)
                handler(AppErrors.HttpError(resourcesProvider.getString(R.string.errors_unknown_error)))
            }
        }
    }

    private fun onUnauthorized(responseCode: Int, handler: (error: AppErrors) -> Unit) {
        if (responseCode == ApiContract.HttpCodes.CLIENT_UNAUTHORIZED) {
            handler(AppErrors.Unauthorized(resourcesProvider.getString(R.string.login_not_authorization_info_token)))
        }
    }

}