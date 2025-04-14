package app.editors.manager.mvp.models.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

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
}.onStart { emit(ResultUi.Loading) }