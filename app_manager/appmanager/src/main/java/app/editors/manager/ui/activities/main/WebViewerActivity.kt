package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import app.editors.manager.databinding.ActivityViewerWebBinding
import app.editors.manager.mvp.models.explorer.CloudFile
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
            val file = intent.getSerializable(TAG_FILE, CloudFile::class.java)
            showFragment(WebViewerFragment.newInstance(file), null)
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
        private const val TAG_FILE = "TAG_FILE"

        fun getActivityIntent(context: Context, file: CloudFile) =
            Intent(context, WebViewerActivity::class.java).apply {
                putExtra(TAG_FILE, file)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
    }
}