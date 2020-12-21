package lib.toolkit.base.managers.utils

import android.os.AsyncTask

@Deprecated("Use AsyncRoutines class instead")
class AsyncTasks<Result, Progress> {

    companion object {

        @JvmStatic
        fun <R, P> run(onBackground: (OnProgress<P>) -> R?, onProgress: ((List<P>) -> Unit), onResult: ((R) -> Unit)): AsyncTasks<R, P> {
            return AsyncTasks(onBackground, onProgress, onResult)
        }

        @JvmStatic
        fun <R, P> run(onBackground: (OnProgress<P>) -> R?, onResult: ((R) -> Unit)): AsyncTasks<R, P> {
            return AsyncTasks(onBackground, null, onResult)
        }

        @JvmStatic
        fun <R, P> run(onBackground: (OnProgress<P>) -> R?): AsyncTasks<R, P> {
            return AsyncTasks(onBackground)
        }
    }

    @FunctionalInterface
    interface OnProgress<T> {
        fun publish(vararg result: T)
    }

    private constructor(onBackground: (OnProgress<Progress>) -> Result?, onProgress: ((List<Progress>) -> Unit)? = null, onResult: ((Result) -> Unit)? = null) {
        mOnBackground = onBackground
        mOnProgress = onProgress
        mOnResult = onResult
    }

    private val mAsyncTask: ATask
    private var mOnBackground: (OnProgress<Progress>) -> Result?
    private var mOnProgress: ((List<Progress>) -> Unit)?
    private var mOnResult: ((Result) -> Unit)?

    init {
        mAsyncTask = ATask()
        mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun cancel() {
        mAsyncTask.cancel(true)
    }

    fun join(): Result  {
        mOnResult = null
        return mAsyncTask.get()
    }


    private inner class ATask : AsyncTask<Void, Progress, Result>(),
        OnProgress<Progress> {

        override fun doInBackground(vararg voids: Void): Result? {
            return mOnBackground.invoke(this)
        }

        override fun onPostExecute(result: Result) {
            super.onPostExecute(result)
            mOnResult?.invoke(result)
        }

        override fun onProgressUpdate(vararg values: Progress) {
            super.onProgressUpdate(*values)
            mOnProgress?.invoke(values.asList())
        }

        override fun publish(vararg result: Progress) {
            publishProgress(*result)
        }
    }
}

