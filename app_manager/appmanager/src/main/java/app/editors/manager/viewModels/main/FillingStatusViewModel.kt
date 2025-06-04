package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.FormRole
import app.documents.core.providers.CloudFileProvider
import app.editors.manager.ui.views.custom.FormCompleteStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FillingStatusState(
    val formInfo: CloudFile,
    val loading: Boolean = false,
    val requestLoading: Boolean = false,
    val roles: List<FormRole> = emptyList(),
    val completeStatus: FormCompleteStatus = FormCompleteStatus.Waiting
)

sealed class FillingStatusEffect {

    data object Error : FillingStatusEffect()
}

class FillingStatusViewModel(
    private val formInfo: CloudFile,
    private val cloudFileProvider: CloudFileProvider
) : ViewModel() {

    private val _state: MutableStateFlow<FillingStatusState> =
        MutableStateFlow(FillingStatusState(formInfo))
    val state: StateFlow<FillingStatusState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<FillingStatusEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<FillingStatusEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            fetchFormInfo()
            fetchRoles()
        }
    }

    fun stopFilling() {
        viewModelScope.launch {
            _state.update { it.copy(requestLoading = true) }
            cloudFileProvider.stopFilling(formInfo.id)
                .collect { result ->
                    _state.update { it.copy(requestLoading = false) }
                    when (result) {
                        is NetworkResult.Error -> _effect.emit(FillingStatusEffect.Error)
                        is NetworkResult.Success<*> -> {
                            fetchFormInfo()
                            fetchRoles()
                        }
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }

    private suspend fun fetchFormInfo() {
        cloudFileProvider.getFileInfo(formInfo.id)
            .collect { result ->
                when (result) {
                    is NetworkResult.Success<CloudFile> -> {
                        _state.update {
                            it.copy(formInfo = result.data)
                        }
                    }

                    is NetworkResult.Error -> {
                        _effect.emit(FillingStatusEffect.Error)
                    }

                    is NetworkResult.Loading -> Unit
                }
            }

    }

    private suspend fun fetchRoles() {
        cloudFileProvider.getFillingStatus(formInfo.id)
            .collect { result ->
                when (result) {
                    is NetworkResult.Success<List<FormRole>> -> {
                        val roles = result.data
                        _state.update {
                            it.copy(
                                loading = false,
                                roles = roles,
                                completeStatus = FormCompleteStatus.from(roles)
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        _effect.emit(FillingStatusEffect.Error)
                    }

                    is NetworkResult.Loading -> Unit
                }
            }
    }
}