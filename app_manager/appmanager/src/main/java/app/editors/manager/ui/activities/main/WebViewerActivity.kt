package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import app.editors.manager.databinding.ActivityViewerWebBinding
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.main.WebViewerFragment.Companion.newInstance

class WebViewerActivity : BaseAppActivity() {
    private var viewBinding: ActivityViewerWebBinding? = null
    private var mUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

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
            val file = intent.getSerializableExtra(TAG_FILE) as CloudFile?
            showFragment(newInstance(file), null)
        }
    }

    private fun initException() {
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable? ->
            Log.d(TAG, "ID: " + thread.id + "; NAME: " + thread.name)
            setResult(RESULT_CANCELED, Intent())
            mUncaughtExceptionHandler?.uncaughtException(thread, throwable ?: Throwable())
        }
    }

    companion object {

        val TAG = WebViewerActivity::class.java.simpleName
        const val TAG_VIEWER_FAIL = "TAG_VIEWER_FAIL"
        private const val TAG_FILE = "TAG_FILE"

        private fun getActivityIntent(context: Context?) =
            Intent(context, WebViewerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }

        fun show(fragment: Fragment, file: CloudFile?) {
            val intent = getActivityIntent(fragment.context).apply { putExtra(TAG_FILE, file) }
            fragment.startActivityForResult(intent, REQUEST_ACTIVITY_WEB_VIEWER)
        }

        @JvmStatic
        fun show(activity: Activity, file: CloudFile?) {
            val intent = getActivityIntent(activity).apply { putExtra(TAG_FILE, file) }
            activity.startActivityForResult(intent, REQUEST_ACTIVITY_WEB_VIEWER)
        }
    }
}