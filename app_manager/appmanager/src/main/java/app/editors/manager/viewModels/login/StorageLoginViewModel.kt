package app.editors.manager.viewModels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.login.StorageLoginRepository
import app.documents.core.network.common.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StorageLoginState {

    data object Progress : StorageLoginState()
    data object Success : StorageLoginState()
    data object None : StorageLoginState()
    data class Error(val exception: Throwable) : StorageLoginState()
}

class StorageLoginViewModelFactory(private val repository: StorageLoginRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StorageLoginViewModel(repository) as T
    }
}

class StorageLoginViewModel(private val repository: StorageLoginRepository) : ViewModel() {

    private val _state: MutableStateFlow<StorageLoginState> = MutableStateFlow(StorageLoginState.None)
    val state: StateFlow<StorageLoginState> = _state.asStateFlow()

    private var signInJob: Job? = null

    fun cancel() {
        signInJob?.cancel()
        _state.value = StorageLoginState.None
    }

    fun signIn(code: String) {
        _state.value = StorageLoginState.Progress
        signInJob = viewModelScope.launch {
            repository.signIn(code)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Error -> _state.value = StorageLoginState.Error(result.exception)
                        is NetworkResult.Success -> _state.value = StorageLoginState.Success
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }
}