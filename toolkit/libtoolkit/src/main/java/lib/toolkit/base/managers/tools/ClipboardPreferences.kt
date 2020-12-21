package lib.toolkit.base.managers.tools

import android.content.Context

object ClipboardPreferences {

    private val NAME = ClipboardPreferences::class.java.simpleName
    private const val KEY_CLIPBOARD = "KEY_CLIPBOARD"

    @JvmStatic
    fun setClipboard(context: Context, value: String) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).apply {
            edit().putString(KEY_CLIPBOARD, value).apply()
        }
    }

    @JvmStatic
    fun getClipboard(context: Context): String? {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(KEY_CLIPBOARD, null)
    }

    @JvmStatic
    fun clear(context: Context) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).apply {
            edit().putString(KEY_CLIPBOARD, null).apply()
        }
    }

}