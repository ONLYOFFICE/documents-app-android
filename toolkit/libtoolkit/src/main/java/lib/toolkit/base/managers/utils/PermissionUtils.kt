package lib.toolkit.base.managers.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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

    @JvmStatic
    fun requestSmsPermission(activity: AppCompatActivity, requestCode: Int): Boolean {
        return requestPermission(
            activity,
            requestCode,
            Manifest.permission.RECEIVE_SMS
        )
    }

    @JvmStatic
    fun requestSmsPermission(fragment: Fragment, requestCode: Int): Boolean {
        return requestPermission(
            fragment,
            requestCode,
            Manifest.permission.RECEIVE_SMS
        )
    }

    @JvmStatic
    fun requestCameraPermission(activity: AppCompatActivity, requestCode: Int): Boolean {
        return requestPermission(
            activity,
            requestCode,
            Manifest.permission.CAMERA
        )
    }

    @JvmStatic
    fun requestCameraPermission(fragment: Fragment, requestCode: Int): Boolean {
        return requestPermission(
            fragment,
            requestCode,
            Manifest.permission.CAMERA
        )
    }

}

