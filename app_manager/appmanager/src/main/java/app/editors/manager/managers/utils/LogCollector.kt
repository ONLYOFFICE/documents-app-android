package app.editors.manager.managers.utils

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object LogCollector {
    private const val LOG_COMMAND = "logcat -d -v threadtime *:V"
    private const val MAX_LINES = 1000 // Limit the number of lines for performance reasons

    fun collectLogs(): List<String> {
        return try {
            val process = Runtime.getRuntime().exec(LOG_COMMAND)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val logs = mutableListOf<String>()
            var line: String?
            var lineCount = 0

            while (reader.readLine().also { line = it } != null && lineCount < MAX_LINES) {
                line?.let {
                    logs.add(it)
                    lineCount++
                }
            }
            logs
        } catch (e: IOException) {
            Log.e("LogCollector", "Error collecting logs", e)
            listOf("Error collecting logs: ${e.message}")
        }
    }

    fun clearLogs() {
        try {
            Runtime.getRuntime().exec("logcat -c")
        } catch (e: IOException) {
            Log.e("LogCollector", "Error clearing logs", e)
        }
    }
}