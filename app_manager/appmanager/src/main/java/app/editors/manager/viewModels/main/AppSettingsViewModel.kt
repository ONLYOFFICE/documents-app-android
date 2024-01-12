package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.*
import app.editors.manager.R
import app.editors.manager.managers.tools.PreferenceTool
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import lib.toolkit.base.managers.utils.FileUtils

data class AppSettingsState(
    val cache: Long = 0,
    val themeMode: Int = 0,
    val analytics: Boolean = false,
    val wifi: Boolean = false,
    val passcodeEnabled: Boolean = false
)

class AppSettingsViewModelFactory(
    private val themePrefs: ThemePreferencesTools,
    private val resourcesProvider: ResourcesProvider,
    private val preferenceTool: PreferenceTool
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AppSettingsViewModel::class.java)) {
            AppSettingsViewModel(themePrefs, resourcesProvider, preferenceTool) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}

class AppSettingsViewModel(
    private val themePrefs: ThemePreferencesTools,
    private val resourcesProvider: ResourcesProvider,
    private val preferenceTool: PreferenceTool
) : ViewModel() {


    private val _settingsState: MutableStateFlow<AppSettingsState> = MutableStateFlow(
        AppSettingsState(
            cache = cache,
            analytics = preferenceTool.isAnalyticEnable,
            wifi = preferenceTool.uploadWifiState,
            passcodeEnabled = preferenceTool.passcodeLock.enabled,
            themeMode = themePrefs.mode
        )
    )

    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    private val _message: MutableSharedFlow<String> = MutableSharedFlow(1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    private val cache: Long
        get() = FileUtils.getSize(resourcesProvider.getCacheDir(true)) +
                FileUtils.getSize(resourcesProvider.getCacheDir(false))

    fun setAnalytic(isEnable: Boolean) {
        preferenceTool.isAnalyticEnable = isEnable
        _settingsState.value = _settingsState.value.copy(analytics = isEnable)
    }

    fun setWifiState(isEnable: Boolean) {
        preferenceTool.setWifiState(isEnable)
        _settingsState.value = _settingsState.value.copy(wifi = isEnable)
    }

    fun setThemeMode(mode: Int) {
        themePrefs.mode = mode
        _settingsState.value = _settingsState.value.copy(themeMode = mode)
        AppCompatDelegate.setDefaultNightMode(mode)
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