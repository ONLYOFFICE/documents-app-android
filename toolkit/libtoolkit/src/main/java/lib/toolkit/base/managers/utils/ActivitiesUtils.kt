package lib.toolkit.base.managers.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BadParcelableException
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.ActivitiesUtils.IMAGE_TYPE
import java.io.File
import java.io.Serializable


object ActivitiesUtils {

    @JvmField
    val TAG: String = ActivitiesUtils::class.java.simpleName

    const val IMAGE_TYPE = "image/*"

    private const val PLAY_MARKET = "market://details?id="
    private const val PLAY_STORE = "https://play.google.com/store/apps/details?id="
    const val PICKER_NO_FILTER = "*/*"
    private const val PICKER_PNG_FILTER = "image/png"

    @JvmStatic
    fun getDownloadsViewerIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "vnd.android.document/*")
        }
    }

    @JvmStatic
    private fun getIntentSingleFilePicker(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            type = PICKER_NO_FILTER
        }
    }

    @JvmStatic
    private fun getIntentMultipleFilePicker(): Intent {
        return getIntentSingleFilePicker()
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    @JvmStatic
    fun showSingleFilePicker(activity: Activity, title: String?, requestCode: Int) {
        activity.startActivityForResult(Intent.createChooser(getIntentSingleFilePicker(), title), requestCode)
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    @JvmStatic
    fun showSingleFilePicker(activity: Activity, @StringRes titleId: Int, requestCode: Int) {
        showSingleFilePicker(
            activity,
            activity.getString(titleId),
            requestCode
        )
    }

    @JvmStatic
    fun showDownloadViewer(fragment: Fragment, requestCode: Int, uri: Uri): Boolean {
        return try {
            fragment.startActivityForResult(getDownloadsViewerIntent(uri), requestCode)
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, e.message ?: "")
            false
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun showCamera(fragment: Fragment, requestCode: Int, name: String): Uri? {
        try {
            val uri = ContentResolverUtils.getImageUri(
                fragment.requireContext(),
                name
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            fragment.startActivityForResult(intent, requestCode)
            return uri
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "")
        }

        return null
    }

    @JvmStatic
    fun showBrowser(activity: Activity, chooseTitle: String?, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        activity.startActivity(Intent.createChooser(intent, chooseTitle))
    }

    fun getBrowserIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
    }

    @JvmStatic
    fun showPlayMarket(context: Context, packageId: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_MARKET + packageId))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE + packageId))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    @JvmStatic
    fun showApp(context: Context, packageId: String) {
        context.packageManager.getLaunchIntentForPackage(packageId)?.let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(it)
        }
    }

    @JvmStatic
    fun showEmail(context: Context, chooseTitle: String, to: String, subject: String, body: String) {
        val selectorIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.selector = selectorIntent
        context.startActivity(Intent.createChooser(intent, chooseTitle))
    }

    @JvmStatic
    fun showFileShare(context: Context, chooseTitle: String?, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = PICKER_PNG_FILTER
        context.startActivity(Intent.createChooser(intent, chooseTitle))
    }

    @JvmStatic
    fun minimizeApp(context: Context) {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(startMain)
    }

    @JvmStatic
    fun killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    @JvmStatic
    fun isActivityFront(activity: AppCompatActivity): Boolean {
        return if (activity.intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            activity.finish()
            true
        } else {
            false
        }
    }

    @JvmStatic
    fun showSingleFilePicker(fragment: Fragment, i: Int) {
        fragment.startActivityForResult(getIntentSingleFilePicker(), i)
    }

    @JvmStatic
    fun isTestApp(context: Context): Boolean {
        val appId = context.packageName
        return appId == "app.editors.gcells" || appId == "app.editors.gdocs" || appId == "app.editors.gslides"
    }

    @JvmStatic
    fun createFile(fragment: Fragment?, name: String, code: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = StringUtils.getMimeTypeFromPath(name)
        intent.putExtra(Intent.EXTRA_TITLE, name)
        fragment?.startActivityForResult(intent, code)
    }

    @JvmStatic
    fun getExternalStoragePermission(context: Context, uri: Uri, flag: Int) {
        context.contentResolver?.takePersistableUriPermission(uri, flag)
    }

    @JvmStatic
    fun releaseExternalStoragePermission(context: Context, uri: Uri, flag: Int) {
        context.contentResolver.releasePersistableUriPermission(uri, flag)
    }

    fun isPackageExist(context: Application, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (error: Throwable) {
            false
        }
    }

    fun sendFeedbackEmail(context: Context, message: String) {
        with(context) {
            showEmail(
                this,
                getString(R.string.chooser_email_client),
                getString(R.string.app_support_email),
                getString(R.string.about_email_subject),
                message + UiUtils.getDeviceInfoString(this, false)
            )
        }
    }
}

fun Intent.clearIntent() {
    replaceExtras(Bundle())
    action = ""
    data = null
    flags = 0
}

class DocumentsPicker(
    activityResultRegistry: ActivityResultRegistry,
    private val callback: (uris: List<Uri>?) -> Unit,
) {
    private val choseDocuments: ActivityResultLauncher<Array<String>> =
        activityResultRegistry.register("Documents", ActivityResultContracts.OpenMultipleDocuments()) {
            callback.invoke(it)
        }

    fun show() {
        choseDocuments.launch(arrayOf("*/*"))
    }
}

class CreateDocument : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT)
            .setType(StringUtils.getMimeTypeFromPath(input))
            .putExtra(Intent.EXTRA_TITLE, input)
    }

    override fun getSynchronousResult(
        context: Context,
        input: String
    ): SynchronousResult<Uri?>? = null

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
    }
}

class ImagePicker(
    private val activityResultRegistry: ActivityResultRegistry,
    private val callback: (imageUri: Uri?) -> Unit
) {

    private val getContent: ActivityResultLauncher<String> =
        activityResultRegistry.register("Image", ActivityResultContracts.GetContent(), callback)

    fun pickImage() {
        getContent.launch(IMAGE_TYPE)
    }

}

class FolderChooser(
    activityResultRegistry: ActivityResultRegistry,
    private val callback: (uri: Uri?) -> Unit,
    private val uri: Uri? = null
) {
    private val choseFolder: ActivityResultLauncher<Uri?> =
        activityResultRegistry.register("Camera", ActivityResultContracts.OpenDocumentTree()) {
            callback.invoke(it)
        }

    fun show() {
        choseFolder.launch(uri)
    }
}

class CameraPicker(
    activityResultRegistry: ActivityResultRegistry,
    private val callback: (isSuccess: Boolean) -> Unit,
    private val imageUri: Uri
) {

    private val takePicture: ActivityResultLauncher<Uri> =
        activityResultRegistry.register("Camera", ActivityResultContracts.TakePicture()) {
            callback.invoke(it)
        }

    fun show() {
        takePicture.launch(imageUri)
    }

}

class LaunchActivityForResult(
    activityResultRegistry: ActivityResultRegistry,
    private val callback: (result: ActivityResult) -> Unit,
    private val intent: Intent,
) {

    private val launchActivity: ActivityResultLauncher<Intent> =
        activityResultRegistry.register(
            "EditorsForResult",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            callback.invoke(result)
        }

    fun show() {
        launchActivity.launch(intent)
    }

}

class FontPicker(
    activityResultRegistry: ActivityResultRegistry,
    callback: (uris: List<Uri>?) -> Unit
) {

    private val getContent: ActivityResultLauncher<String> =
        activityResultRegistry.register("Font", ActivityResultContracts.GetMultipleContents(), callback)

    fun pickFonts() {
        getContent.launch("font/*")
    }

}

@Suppress("DEPRECATION", "UNCHECKED_CAST")
fun <T : Serializable> Intent.getSerializable(key: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializableExtra(key, clazz)!!
    else
        this.getSerializableExtra(key) as T
}

@Suppress("DEPRECATION", "UNCHECKED_CAST")
fun <T : Serializable> Bundle.getSerializableExt(key: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializable(key, clazz)!!
    else
        this.getSerializable(key) as T
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Intent.getSerializableExt(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getSerializableExtra(key, T::class.java)
    else
        getSerializableExtra(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializableExt(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getSerializable(key, T::class.java)
    else
        getSerializable(key) as? T
}


fun <T : Parcelable> Intent.getParcelable(key: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getParcelableExtra(key, clazz)!!
    else
        this.getParcelableExtra<T>(key) as T
}

fun <T : Parcelable> Bundle.getParcelableExt(key: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getParcelable(key, clazz)!!
    else
        this.getParcelable<T>(key) as T
}

fun Bundle.contains(key: String): Boolean {
    return try {
        this.containsKey(key)
    } catch (error: BadParcelableException) {
        false
    }
}

inline fun <reified T> Bundle?.getJsonString(key: String, encodedUrl: Boolean = false): T? {
    return this?.getString(key)
        ?.let { if (encodedUrl) java.net.URLDecoder.decode(it, Charsets.UTF_8.toString()) else it }
        ?.let(Json::decodeFromString)
}

fun FragmentActivity.getSendFileIntent(title: Int, file: File? = null, uri: Uri? = null): Intent =
    Intent.createChooser(Intent().apply {
        val intentUri = if (file != null) {
            FileProvider.getUriForFile(this@getSendFileIntent, "$packageName.asc.provider", file)
        } else uri
        action = Intent.ACTION_SEND
        type = "application/*"
        putExtra(Intent.EXTRA_STREAM, intentUri)
    }, getString(title))

fun FragmentActivity.openSendFileActivity(title: Int, uri: Uri) {
    if (uri.scheme == "content") {
        startActivity(getSendFileIntent(title, uri = uri))
    } else {
        startActivity(getSendFileIntent(title, file = uri.toFile()))
    }
}

fun Context.openSendTextActivity(title: String, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, title)
    startActivity(shareIntent)
}

fun Fragment.launchAfterResume(block: () -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            block()
        }
    }
}