package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.editors.manager.R
import app.editors.manager.managers.tools.PreferenceTool
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.mutableStateIn
import java.io.File

data class AppSettingsState(
    val cache: Long = 0,
    val themeMode: Int = 0,
    val analytics: Boolean = false,
    val wifi: Boolean = false,
    val passcodeEnabled: Boolean = false,
    val fonts: List<File> = emptyList()
)

sealed class AppSettingsEffect {

    data class Error(val message: String) : AppSettingsEffect()
    data class Progress(val value: Int) : AppSettingsEffect()
    data object ShowDialog : AppSettingsEffect()
    data object HideDialog : AppSettingsEffect()
}

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

    private val _settingsState: MutableStateFlow<AppSettingsState> = flow {
        emit(
            AppSettingsState(
                cache = cache,
                analytics = preferenceTool.isAnalyticEnable,
                wifi = preferenceTool.uploadWifiState,
                passcodeEnabled = preferenceTool.passcodeLock.enabled,
                themeMode = themePrefs.mode,
                fonts = File(FileUtils.getFontsDir(resourcesProvider.context)).listFiles()?.toList().orEmpty()
            )
        )
    }.mutableStateIn(viewModelScope, AppSettingsState())

    private val _effect: MutableSharedFlow<AppSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<AppSettingsEffect> = _effect.asSharedFlow()

    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    private val _message: MutableSharedFlow<String> = MutableSharedFlow(1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    private var addFontsJob: Job? = null

    private val cache: Long
        get() = FileUtils.getSize(resourcesProvider.getCacheDir(true)) +
                FileUtils.getSize(resourcesProvider.getCacheDir(false)) -
                FileUtils.getSize(File(resourcesProvider.getCacheDir(false)?.absolutePath + "/assets"))

    private fun fetchFonts() {
        val fonts = File(FileUtils.getFontsDir(resourcesProvider.context)).listFiles()?.toList()
        _settingsState.value = _settingsState.value.copy(fonts = fonts.orEmpty())
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

    fun clearFonts() {
        viewModelScope.launch {
            val fonts = File(FileUtils.getFontsDir(resourcesProvider.context))
            if (fonts.exists()) {
                fonts.deleteRecursively()
                fetchFonts()
            }
        }
    }

    fun deleteFont(font: File) {
        viewModelScope.launch {
            val file = File("${FileUtils.getFontsDir(resourcesProvider.context)}/${font.name}")
            if (file.exists()) file.delete()
            fetchFonts()
        }
    }

    fun addFont(fonts: List<Uri>?) {
        addFontsJob = viewModelScope.launch {
            if (!fonts.isNullOrEmpty()) {
                _effect.emit(AppSettingsEffect.ShowDialog)
                fonts.forEachIndexed { index, font ->
                    try {
                        val filename = DocumentFile.fromSingleUri(resourcesProvider.context, font)?.name.orEmpty()
                        val fontDir = FileUtils.getFontsDir(resourcesProvider.context)
                        val file = File(fontDir, filename)
                        file.createNewFile()
                        file.writeBytes(
                            resourcesProvider.context
                                .contentResolver
                                .openInputStream(font)?.readBytes() ?: ByteArray(0)
                        )
                        _effect.emit(AppSettingsEffect.Progress(((index + 1).toFloat() / fonts.size * 100).toInt()))
                        fetchFonts()
                        delay(250)
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            _effect.emit(AppSettingsEffect.Error(resourcesProvider.context.getString(R.string.upload_manager_error)))
                        } else {
                            _effect.emit(AppSettingsEffect.HideDialog)
                        }
                    }
                }
                _effect.emit(AppSettingsEffect.HideDialog)
            }
        }
    }

    fun cancelJob() {
        addFontsJob?.cancel()
    }
}