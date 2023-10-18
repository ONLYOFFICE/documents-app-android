package lib.toolkit.base.managers.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


object PermissionUtils {

    @JvmStatic
    fun checkPermission(context: Context, vararg permissions: String): Boolean {
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun requestPermission(fragment: Fragment, requestCode: Int, vararg permissions: String): Boolean {
        return if (!checkPermission(
                fragment.requireContext(),
                *permissions
            )
        ) {
            fragment.requestPermissions(permissions, requestCode)
            false
        } else {
            true
        }
    }

    @JvmStatic
    fun requestPermission(activity: AppCompatActivity, requestCode: Int, vararg permissions: String): Boolean {
        return if (!checkPermission(
                activity,
                *permissions
            )
        ) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
            false
        } else {
            true
        }
    }


    @JvmStatic
    fun requestReadWritePermission(activity: AppCompatActivity, requestCode: Int): Boolean {
        return requestPermission(
            activity,
            requestCode,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    @JvmStatic
    fun checkReadWritePermission(context: Context): Boolean {
        return checkPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    @JvmStatic
    fun checkReadPermission(context: Context): Boolean {
        return checkPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    @JvmStatic
    fun requestWritePermission(activity: AppCompatActivity, requestCode: Int): Boolean {
        return requestPermission(
            activity,
            requestCode,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    @JvmStatic
    fun requestWritePermission(fragment: Fragment, requestCode: Int): Boolean {
        return requestPermission(
            fragment,
            requestCode,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    @JvmStatic
    fun requestReadPermission(activity: AppCompatActivity, requestCode: Int): Boolean {
        return requestPermission(
            activity,
            requestCode,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    @JvmStatic
    fun requestReadPermission(fragment: Fragment, requestCode: Int): Boolean {
        return requestPermission(
            fragment,
            requestCode,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

}

/**
 * @param callback callback to be called when permission granted
 */
class RequestPermission(
    activityResultRegistry: ActivityResultRegistry,
    private val callback: (permission: Boolean) -> Unit,
    private val permission: String
) {

    private val requestPermissions: ActivityResultLauncher<String> =
        activityResultRegistry.register("Permission", ActivityResultContracts.RequestPermission()) {
            callback(it)
        }

    fun request() {
        requestPermissions.launch(permission)
    }

}

class RequestPermissions(
    activityResultRegistry: ActivityResultRegistry,
    private val callback: (permissions: Map<String, Boolean>) -> Unit,
    private val permissions: Array<String>
) {

    private val requestPermissions: ActivityResultLauncher<Array<String>> =
        activityResultRegistry.register("Permissions", ActivityResultContracts.RequestMultiplePermissions()) {
            callback(it)
        }

    fun request() {
        requestPermissions.launch(permissions)
    }

}

