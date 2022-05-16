package lib.toolkit.base.managers.tools

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class ThemePreferencesTools(val context: Context) {

    private val preferencesTools = context.getSharedPreferences(ThemePreferencesTools::class.java.simpleName, Context.MODE_PRIVATE)

    var mode: Int
        get() {
            return preferencesTools.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        set(value) {
            preferencesTools.edit().putInt("mode", value).apply()
        }

}