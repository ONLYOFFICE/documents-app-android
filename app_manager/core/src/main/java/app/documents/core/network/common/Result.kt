package app.documents.core.network.common

import android.util.Log
import app.documents.core.BuildConfig
import app.documents.core.network.common.Result.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

sealed class Result<out T> {
    class Success<T>(val result: T) : Result<T>()
    class Error(val exception: Throwable) : Result<Nothing>()
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>>(::Success)
    .catch { cause ->
        if (BuildConfig.DEBUG) {
            if (cause is HttpException) {
                Log.e("Result", cause.response()?.errorBody()?.string().toString())
            } else {
                Log.e("Result", cause.message.toString())
            }
        }
        emit(Result.Error(cause))
    }