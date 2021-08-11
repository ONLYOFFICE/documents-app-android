package app.editors.manager.onedrive.managers.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadWork(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {
    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}