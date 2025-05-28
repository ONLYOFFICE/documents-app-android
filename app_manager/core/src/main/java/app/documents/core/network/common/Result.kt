package app.documents.core.network.common

import app.documents.core.network.common.Result.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed class Result<out T> {
    class Success<T>(val result: T) : Result<T>()
    class Error(val exception: Throwable) : Result<Nothing>()
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>>(::Success)
    .catch { cause -> emit(Result.Error(cause)) }

fun <T> Result<T>.getOrDefault(default: T): T {
    return when(this){
        is Success -> result
        else -> default
    }
}