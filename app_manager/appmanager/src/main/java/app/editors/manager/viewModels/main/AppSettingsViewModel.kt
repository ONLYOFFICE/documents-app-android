package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import androidx.lifecycle.*
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.tools.PreferenceTool
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.FileUtils
import javax.inject.Inject

data class AppSettingsState(
    val cache: Long = 0,
    val themeMode: Int = 0,
    val analytics: Boolean = false,
    val wifi: Boolean = false,
    val passcode: Boolean = false
)

class AppSettingsViewModel : ViewModel() {

    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    @Inject
    lateinit var preferenceTool: PreferenceTool

    private val _settingsState: MutableStateFlow<AppSettingsState> = MutableStateFlow(AppSettingsState())
    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    private val _message: MutableSharedFlow<String> = MutableSharedFlow(1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    private val cache: Long
        get() = FileUtils.getSize(resourcesProvider.getCacheDir(true)) +
                FileUtils.getSize(resourcesProvider.getCacheDir(false))

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getData() {
        with(preferenceTool) {
            _settingsState.value = AppSettingsState(
                cache = cache,
                analytics = isAnalyticEnable,
                wifi = uploadWifiState,
                passcode = isPasscodeLockEnable
            )
        }
    }

    fun setAnalytic(isEnable: Boolean) {
        preferenceTool.isAnalyticEnable = isEnable
        _settingsState.value = _settingsState.value.copy(analytics = isEnable)
    }

    fun setWifiState(isEnable: Boolean) {
        preferenceTool.setWifiState(isEnable)
        _settingsState.value = _settingsState.value.copy(wifi = isEnable)
    }

    fun setThemeMode(mode: Int) {
        _settingsState.value = _settingsState.value.copy(themeMode = mode)
    }

    @SuppressLint("MissingPermission")
    fun clearCache() {
        viewModelScope.launch {
            resourcesProvider.getCacheDir(true)?.let(FileUtils::deletePath)
            resourcesProvider.getCacheDir(false)?.let(FileUtils::deletePath)
            _message.emit(resourcesProvider.getString(R.string.setting_cache_cleared))
            _settingsState.value = _settingsState.value.copy(cache = cache)
        }
    }

}