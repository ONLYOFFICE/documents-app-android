package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import androidx.lifecycle.*
import app.editors.manager.R
import app.editors.manager.managers.tools.PreferenceTool
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.SingleLiveEvent
import java.io.File
import javax.inject.Inject


class AppSettingsViewModel : ViewModel() {

    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    @Inject
    lateinit var preferencesTool: PreferenceTool

    private val _cacheLiveData: MutableLiveData<Long> = MutableLiveData()
    val cacheLiveData: LiveData<Long> = _cacheLiveData

    val analyticState = liveData { emit(preferencesTool.isAnalyticEnable) }
    val wifiState = liveData { emit(preferencesTool.uploadWifiState) }

    val message = SingleLiveEvent<String>()

    fun getCache() {
        _cacheLiveData.value = FileUtils.getSize(resourcesProvider.getCacheDir(true)) + FileUtils.getSize(
            resourcesProvider.getCacheDir(false)
        )
    }

    fun setAnalytic(isEnable: Boolean) {
        preferencesTool.isAnalyticEnable = isEnable
    }

    fun setWifiState(isEnable: Boolean) {
        preferencesTool.setWifiState(isEnable)
    }

    @SuppressLint("MissingPermission")
    fun clearCache() {
        viewModelScope.launch {
            FileUtils.deletePath(resourcesProvider.getCacheDir(true) ?: File(""))
            FileUtils.deletePath(resourcesProvider.getCacheDir(false) ?: File(""))
            getCache()
            message.value = resourcesProvider.getString(R.string.setting_cache_cleared)
        }
    }

}