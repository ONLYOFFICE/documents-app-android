package app.documents.core.network.common

import android.util.Log
import app.documents.core.BuildConfig
import app.documents.core.network.common.Result.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed class Result<out T> {
    class Success<T>(val result: T) : Result<T>()
    class Error(val exception: Throwable) : Result<Nothing>()
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>>(::Success)
    .catch { cause ->
        if (BuildConfig.DEBUG) Log.e("asResult", cause.message.toString())
        emit(Result.Error(cause))
    }