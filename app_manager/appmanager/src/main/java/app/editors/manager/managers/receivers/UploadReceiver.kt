package app.editors.manager.managers.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.managers.utils.FirebaseUtils.addCrash
import lib.toolkit.base.managers.utils.getSerializableExt

open class UploadReceiver : BaseReceiver<Intent?>() {

    companion object {
        const val UPLOAD_ACTION_ERROR = "UPLOAD_ACTION_ERROR"
        const val UPLOAD_ACTION_ERROR_URL_INIT = "UPLOAD_ACTION_ERROR_URL_INIT"
        const val UPLOAD_ACTION_PROGRESS = "UPLOAD_ACTION_PROGRESS"
        const val UPLOAD_ACTION_COMPLETE = "UPLOAD_ACTION_COMPLETE"
        const val UPLOAD_ACTION_REPEAT = "UPLOAD_ACTION_REPEAT"
        const val UPLOAD_ACTION_CANCELED = "UPLOAD_ACTION_CANCELED"
        const val UPLOAD_AND_OPEN = "UPLOAD_AND_OPEN"

        const val EXTRAS_KEY_PATH = "EXTRAS_KEY_PATH"
        const val EXTRAS_KEY_TITLE = "EXTRAS_KEY_TITLE"
        const val EXTRAS_KEY_FILE = "EXTRAS_KEY_FILE"
        const val EXTRAS_KEY_PROGRESS = "EXTRAS_KEY_PROGRESS"
        const val EXTRAS_KEY_ID = "EXTRAS_KEY_ID"
        const val EXTRAS_FOLDER_ID = "EXTRAS_FOLDER_ID"
        const val EXTRAS_ERROR_PDF_FORM = "EXTRAS_ERROR_PDF_FORM"
    }

    interface OnUploadListener {
        fun onUploadError(path: String?, info: String?, file: String?)
        fun onUploadErrorDialog(title: String, message: String, file: String?)
        fun onUploadComplete(path: String?, info: String?, title: String?, file: CloudFile?, id: String?)
        fun onUploadAndOpen(path: String?, title: String?, file: CloudFile?, id: String?)
        fun onUploadFileProgress(progress: Int, id: String?, folderId: String?)
        fun onUploadCanceled(path: String?, info: String?, id: String?)
        fun onUploadRepeat(path: String?, info: String?)
    }

    private var onUploadListener: OnUploadListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (onUploadListener != null) {
                val title = intent.getStringExtra(EXTRAS_KEY_TITLE)
                val file = intent.getSerializableExt<CloudFile>(EXTRAS_KEY_FILE)
                val path = intent.getStringExtra(EXTRAS_KEY_PATH)
                val id = intent.getStringExtra(EXTRAS_KEY_ID)

                when (intent.action) {
                    UPLOAD_ACTION_ERROR -> {
                        if (intent.hasExtra(EXTRAS_ERROR_PDF_FORM)) {
                            onUploadListener?.onUploadErrorDialog(
                                title = context.getString(R.string.dialogs_warning_title),
                                message = context.getString(R.string.dialogs_warning_only_pdf_form_message),
                                file = intent.getStringExtra(EXTRAS_KEY_FILE)
                            )
                            return
                        }
                        onUploadListener?.onUploadError(
                            path = title,
                            info = context.getString(R.string.upload_manager_error),
                            file = intent.getStringExtra(EXTRAS_KEY_FILE)
                        )
                    }
                    UPLOAD_ACTION_ERROR_URL_INIT -> {
                        onUploadListener?.onUploadError(
                            path = title,
                            info = context.getString(R.string.upload_manager_error_url),
                            file = null
                        )
                    }
                    UPLOAD_ACTION_PROGRESS -> {
                        onUploadListener?.onUploadFileProgress(
                            progress = intent.getIntExtra(EXTRAS_KEY_PROGRESS, 0),
                            folderId = intent.getStringExtra(EXTRAS_FOLDER_ID),
                            id = intent.getStringExtra(EXTRAS_KEY_FILE)
                        )
                    }
                    UPLOAD_ACTION_COMPLETE -> {
                        onUploadListener?.onUploadComplete(
                            path = path,
                            info = context.getString(R.string.upload_manager_complete),
                            title = title,
                            file = file,
                            id = id
                        )
                    }
                    UPLOAD_AND_OPEN -> {
                        onUploadListener?.onUploadAndOpen(
                            path = path,
                            title = title,
                            file = file,
                            id = id
                        )
                    }
                    UPLOAD_ACTION_REPEAT -> {
                        onUploadListener?.onUploadRepeat(
                            path = path,
                            info = context.getString(R.string.upload_manager_repeat)
                        )
                    }
                    UPLOAD_ACTION_CANCELED -> {
                        onUploadListener?.onUploadCanceled(
                            path = path,
                            info = context.getString(R.string.upload_manager_cancel),
                            id = id
                        )
                    }
                }
            }
        } catch (e: RuntimeException) {
            addCrash(e)
        }
    }

    override fun getFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(UPLOAD_ACTION_ERROR)
            addAction(UPLOAD_ACTION_ERROR_URL_INIT)
            addAction(UPLOAD_ACTION_PROGRESS)
            addAction(UPLOAD_ACTION_COMPLETE)
            addAction(UPLOAD_ACTION_REPEAT)
            addAction(UPLOAD_ACTION_CANCELED)
            addAction(UPLOAD_AND_OPEN)
        }
    }

    fun setOnUploadListener(onUploadListener: OnUploadListener?) {
        this.onUploadListener= onUploadListener
    }
}