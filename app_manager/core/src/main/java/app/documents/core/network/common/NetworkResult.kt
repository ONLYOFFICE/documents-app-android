package app.documents.core.network.common

import app.documents.core.network.common.NetworkResult.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Throwable) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

fun <T> Flow<T>.asResult(): Flow<NetworkResult<T>> = map<T, NetworkResult<T>>(::Success)
    .catch { cause -> emit(NetworkResult.Error(cause)) }

fun <T> NetworkResult<T>.getOrDefault(default: T): T {
    return when (this) {
        is Success -> data
        else -> default
    }
}

fun <T, R> Flow<NetworkResult<T>>.mapResult(mapper: (T) -> R) = map { result ->
    when (result) {
        is Success -> Success(mapper(result.data))
        is NetworkResult.Error -> NetworkResult.Error(result.exception)
        is NetworkResult.Loading -> NetworkResult.Loading
    }
}