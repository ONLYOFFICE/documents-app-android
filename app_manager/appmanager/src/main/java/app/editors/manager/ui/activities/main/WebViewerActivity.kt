package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import app.editors.manager.databinding.ActivityViewerWebBinding
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.main.WebViewerFragment
import lib.toolkit.base.managers.utils.getSerializable

class WebViewerActivity : BaseAppActivity() {

    private var viewBinding: ActivityViewerWebBinding? = null
    private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityViewerWebBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
        killSelf()
    }

    private fun init(savedInstanceState: Bundle?) {
        initException()
        savedInstanceState ?: run {
            showFragment(WebViewerFragment.newInstance(
                file = intent.getSerializable(TAG_FILE, CloudFile::class.java),
                isEditMode = intent.getBooleanExtra(TAG_IS_EDIT, false)
            ), null)
        }
    }

    private fun initException() {
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable? ->
            Log.d(TAG, "ID: " + thread.id + "; NAME: " + thread.name)
            setResult(RESULT_CANCELED, Intent())
            uncaughtExceptionHandler?.uncaughtException(thread, throwable ?: Throwable())
        }
    }

    companion object {

        val TAG: String = WebViewerActivity::class.java.simpleName
        const val TAG_VIEWER_FAIL = "TAG_VIEWER_FAIL"
        const val TAG_FILE = "TAG_FILE"
        const val TAG_IS_EDIT = "TAG_IS_EDIT"

        fun getActivityIntent(context: Context, file: CloudFile, isEditMode: Boolean) =
            Intent(context, WebViewerActivity::class.java).apply {
                putExtra(TAG_FILE, file)
                putExtra(TAG_IS_EDIT, isEditMode)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
    }
}