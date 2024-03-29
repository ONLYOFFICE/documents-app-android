package app.editors.manager.managers.tools

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import app.editors.manager.R
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Singleton
class AppLocaleHelper @Inject constructor(val context: Context, val preferenceTool: PreferenceTool) {

    private val localeManager: LocaleManager by lazy { context.getSystemService(LocaleManager::class.java) }

    val locales: List<Locale> by lazy {
        context.resources.getStringArray(R.array.app_locales).map { stringLocale ->
            with(stringLocale.split("_")) {
                if (size > 1) {
                    Locale(this[0], this[1])
                } else {
                    Locale(this[0])
                }
            }
        }
    }

    val appLocale: Locale?
        get() = localeManager.applicationLocales.get(0)

    val systemLocale: Locale?
        get() = localeManager.systemLocales.get(0)

    val currentLocale: Locale
        get() = appLocale ?: systemLocale ?: Locale.getDefault()

    fun changeLocale(locale: Locale?, skip: Boolean) {
        localeManager.applicationLocales = locale?.let { LocaleList(locale) } ?: LocaleList.getEmptyLocaleList()
        setPrefs(skip)
    }

    fun setPrefs(skip: Boolean) {
        preferenceTool.systemLocale = systemLocale?.language
        preferenceTool.skipLocaleConfirmation = skip
    }

    fun checkAppLocale(withPrefs: Boolean = false): Boolean {
        return locales.any { locale -> locale.language == systemLocale?.language }
                && appLocale != null
                && appLocale?.language != systemLocale?.language
                && if (withPrefs) systemLocale?.language != preferenceTool.systemLocale
                && !preferenceTool.skipLocaleConfirmation else true
    }

}