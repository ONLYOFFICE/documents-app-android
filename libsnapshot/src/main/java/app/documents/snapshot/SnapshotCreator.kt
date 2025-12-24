@file:Suppress("PackageDirectoryMismatch")

package lib.editors.base.snapshots

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import lib.toolkit.base.managers.utils.FileUtils

class SnapshotCreator {
    companion object {
        init {
            Log.d("log", "loading library bootstrap_jni...")
            System.loadLibrary("bootstrap_jni")
        }

        private val assetsPaths: List<String> by lazy {
            arrayListOf(
                "documents",
                "presentations",
                "spreadsheets",
                "fonts",
                "themes",
                "icu",
                "dictionaries",
                "sdk.version"
            )
        }

        @SuppressLint("MissingPermission")
        @JvmStatic
        fun unpackAssets(context: Context): String {
            return FileUtils.assetUnpack(context, assetsPaths, isExternal = false) ?: ""
        }

        @JvmStatic
        external fun start(snapshotsPath: String, assetsPath: String)

        @JvmStatic
        external fun stop()
    }
}
