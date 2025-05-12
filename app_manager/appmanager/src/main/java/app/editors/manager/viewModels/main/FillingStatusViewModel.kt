package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.Result
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
    val loading: Boolean = false,
    val roles: List<FormRole> = emptyList(),
    val completeStatus: FormCompleteStatus = FormCompleteStatus.Waiting
)

sealed class FillingStatusEffect {

    data object Error : FillingStatusEffect()
}

class FillingStatusViewModel(
    fileId: String,
    private val cloudFileProvider: CloudFileProvider
) : ViewModel() {

    private val _state: MutableStateFlow<FillingStatusState> =
        MutableStateFlow(FillingStatusState())
    val state: StateFlow<FillingStatusState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<FillingStatusEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<FillingStatusEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            cloudFileProvider.getFillingStatus(fileId)
                .collect { result ->
                    when (result) {
                        is Result.Success<List<FormRole>> -> {
                            val roles = result.result
                            _state.update {
                                it.copy(
                                    loading = false,
                                    roles = roles,
                                    completeStatus = FormCompleteStatus.from(roles)
                                )
                            }
                        }

                        is Result.Error -> {
                            _effect.emit(FillingStatusEffect.Error)
                        }
                    }
                }
        }
    }
}