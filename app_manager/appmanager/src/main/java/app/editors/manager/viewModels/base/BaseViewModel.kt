package app.editors.manager.viewModels.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import app.editors.manager.managers.tools.ErrorHandler
import app.editors.manager.mvp.models.error.AppErrors
import lib.toolkit.base.managers.utils.SingleLiveEvent
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    @Inject
    protected lateinit var errorHandler: ErrorHandler

    private val _errorLiveData: SingleLiveEvent<AppErrors> = SingleLiveEvent()
    val errorLiveData: LiveData<AppErrors> = _errorLiveData

    protected fun fetchError(throwable: Throwable) {
        errorHandler.fetchError(throwable) { error ->
            _errorLiveData.value = error
        }
    }

}