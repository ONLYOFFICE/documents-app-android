package app.editors.manager.viewModels.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.editors.manager.managers.tools.ErrorHandler
import app.editors.manager.mvp.models.error.AppErrors
import lib.toolkit.base.managers.tools.ResourcesProvider
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    private val _errorLiveData: MutableLiveData<AppErrors> = MutableLiveData()
    val errorLiveData: LiveData<AppErrors> = _errorLiveData

    protected fun fetchError(throwable: Throwable) {
        errorHandler.fetchError(throwable) { error ->
            _errorLiveData.value = error
        }
    }

}