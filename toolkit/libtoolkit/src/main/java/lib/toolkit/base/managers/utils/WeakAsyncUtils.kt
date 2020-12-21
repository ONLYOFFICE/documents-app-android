package lib.toolkit.base.managers.utils

import android.os.AsyncTask
import android.util.Log
import java.lang.ref.WeakReference
import java.util.*

open abstract class WeakAsyncUtils<Params, Progress, Result, Reference>(var mKey: String) : AsyncTask<Params, Progress, Result>() {

    companion object {

        @JvmField
        val TAG = WeakAsyncUtils::class.java!!.simpleName

        protected val TASKS: MutableMap<String, WeakAsyncUtils<*, *, *, *>> = LinkedHashMap()

        @JvmStatic
        fun clearAllTasks() {
            for (key in TASKS.keys) {
                val aTask = TASKS[key]
                aTask?.cancel(true)
            }
            TASKS.clear()
        }
    }

    @JvmField
    protected var mWeakReference: WeakReference<Reference>? = null

    constructor(key: String,  reference: Reference) : this(key) {
        setWeakReference(reference)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        TASKS[mKey] = this
    }

    override fun onPostExecute(result: Result) {
        super.onPostExecute(result)
        mWeakReference?.get()?.let {
            TASKS.remove(mKey)
        }
    }

    override fun onCancelled(result: Result) {
        super.onCancelled(result)
        TASKS.remove(mKey)
    }

    override fun onCancelled() {
        super.onCancelled()
        TASKS.remove(mKey)
    }

    @SafeVarargs
    fun execute(isInterrupt: Boolean, vararg params: Params): Boolean {
        TASKS[mKey]?.let { aTask ->
            // Set callback for previous computing
//            mWeakReference?.let {
//                aTask.setWeakReference(mWeakReference as WeakReference)
//            }

            // Check status previous computing
            // If finish - try get result
            if (aTask.status == AsyncTask.Status.FINISHED) {
                try {
                    onPostExecute(aTask.get() as Result)
                    TASKS.remove(mKey)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, e.message ?: "")
                    aTask.cancel(true)
                }

            } else if (isInterrupt) {
                aTask.cancel(true)
            } else {
                return false
            }
        }

        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *params)
        TASKS[mKey] = this
        return true
    }

    fun cancel() {
        val aTask = TASKS[mKey]
        aTask?.cancel(true)
    }

    fun setWeakReference(weakReference: WeakReference<Reference>) {
        mWeakReference = weakReference
    }

    fun setWeakReference(reference: Reference) {
        mWeakReference = WeakReference(reference)
    }

}
