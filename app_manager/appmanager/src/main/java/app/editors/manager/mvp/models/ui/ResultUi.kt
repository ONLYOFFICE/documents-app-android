package app.editors.manager.mvp.models.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.documents.core.network.common.Result as NetworkResult

sealed interface ResultUi<out T> {
    data class Success<T>(val data: T) : ResultUi<T>
    data class Error(val e: Throwable) : ResultUi<Nothing>
    data object Loading : ResultUi<Nothing>
}

fun <T, R> Flow<Result<T>>.asFlowResultUI(mapper: (T) -> R) = map { result ->
    result.fold(
        onSuccess = { ResultUi.Success(mapper(it)) },
        onFailure = { ResultUi.Error(it) }
    )
}

fun <T, R> Flow<NetworkResult<T>>.networkAsFlowResultUI(mapper: (T) -> R) = map { result ->
    when(result){
        is NetworkResult.Success -> ResultUi.Success(mapper(result.result))
        is NetworkResult.Error -> ResultUi.Error(result.exception)
    }
}