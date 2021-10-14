package lib.toolkit.base.managers.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


object KeyboardUtils {

    @JvmField
    val TAG = KeyboardUtils::class.java.simpleName

    val TAG_TEXT = "TAG_TEXT"
    val TAG_HTML = "TAG_HTML"
    val TAG_INTENT = "TAG_INTENT"


    @JvmOverloads
    @JvmStatic
    fun showKeyboard(view: View?, isShowForce: Boolean = true) {
        view?.let {
            (view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { manager ->
                view.apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                }.requestFocus()

                manager.toggleSoftInput(
                    if (isShowForce) InputMethodManager.SHOW_FORCED else InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }
        }
    }

    @JvmOverloads
    @JvmStatic
    fun showTextAutoCompleteKeyboard(view: EditText, isShowForce: Boolean = true) {
        view.inputType = view.inputType or InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
        showKeyboard(view, isShowForce)
    }

    @JvmOverloads
    @JvmStatic
    fun showTextAutoCorrectKeyboard(view: EditText, isShowForce: Boolean = true) {
        view.inputType = view.inputType or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        showKeyboard(view, isShowForce)
    }

    @JvmOverloads
    @JvmStatic
    fun showNumericKeyboard(view: EditText, isShowForce: Boolean = true) {
        view.inputType = InputType.TYPE_CLASS_NUMBER
        showKeyboard(view, isShowForce)
    }

    @JvmStatic
    fun hideKeyboard(view: View?) {
        view?.let {
            (view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { manager ->
                view.clearFocus()
                manager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        hideKeyboard(activity.currentFocus ?: View(activity))
    }

    fun hideKeyboard(context: Context, token: IBinder?) {
        token?.let {
            (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                it,
                0
            )
        }
    }

    // Don't call twice
    fun forceHide(context: Activity) {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun getAllDataFromClipboard(context: Context, label: String? = null): Map<String, String>? {
        return (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.primaryClip?.let { manager ->
            hashMapOf<String, String>().also { map ->
                if (manager.itemCount > 0 && (label == null || label == manager.description.label)) {
                    (0 until manager.itemCount).forEach { index ->
                        when {
                            manager.getItemAt(index).text != null -> {
                                map[TAG_TEXT] = map[TAG_TEXT] ?: "" + manager.getItemAt(index).text
                            }

                            manager.getItemAt(index).htmlText != null -> {
                                map[TAG_HTML] = map[TAG_HTML] ?: "" + manager.getItemAt(index).htmlText
                            }

                            manager.getItemAt(index).intent != null -> {
                                manager.getItemAt(index).intent?.let { intent ->
                                    intent.extras?.keySet()?.forEach { key ->
                                        intent.getStringExtra(key)?.let { value ->
                                            map[key] = value
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } ?: null
    }

    @JvmOverloads
    @JvmStatic
    fun getTextFromClipboard(context: Context, label: String? = null): String {
        return getAllDataFromClipboard(context, label)?.get(TAG_TEXT) ?: ""
    }

    @JvmOverloads
    @JvmStatic
    fun setDataToClipboard(context: Context, value: String, label: String? = "", values: Map<String, String>? = null) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.apply {
            setPrimaryClip(ClipData.newPlainText(label, value).apply {
                values?.entries?.forEach { item ->
                    addItem(ClipData.Item(Intent().apply {
                        putExtra(item.key, item.value)
                    }))
                }
            })
        }
    }

    @JvmStatic
    fun clearDataFromClipboard(context: Context) {
        setDataToClipboard(context, "", "")
    }

    @JvmStatic
    fun isDataClipboard(context: Context): Boolean {
        return (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.primaryClip?.let { manager ->
            manager.itemCount > 0
        } ?: false
    }

}
