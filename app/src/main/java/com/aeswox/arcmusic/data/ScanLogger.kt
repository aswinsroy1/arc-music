package com.aeswox.arcmusic.data

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes ARC_SCAN_DIAG lines to both [android.util.Log] and an in-app
 * file so the user can export them without needing ADB.
 *
 * Log file: <filesDir>/arc_scan_diag.log
 * Max size: 2 MB — rotated automatically when exceeded.
 */
@Singleton
class ScanLogger @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Named 'diagFile' to avoid JVM clash with the public fun getLogFile().
    private val diagFile: File get() = File(context.filesDir, "arc_scan_diag.log")

    private val dateFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private fun timestamp() = dateFmt.format(Date())

    private fun write(level: String, message: String) {
        try {
            val f = diagFile
            // Rotate if > 2 MB so the file never grows unbounded.
            if (f.exists() && f.length() > 2 * 1024 * 1024) f.delete()
            f.appendText("${timestamp()} $level $message\n")
        } catch (_: Exception) {
            // Never let logging crash the app.
        }
    }

    fun i(message: String) { Log.i(TAG, message); write("I", message) }
    fun w(message: String) { Log.w(TAG, message); write("W", message) }
    fun d(message: String) { Log.d(TAG, message); write("D", message) }

    /** Deletes the log file entirely and writes a marker entry. */
    fun clear() {
        try { diagFile.delete() } catch (_: Exception) {}
        i("--- Log cleared by user ---")
    }

    /**
     * Returns the log [File] for sharing via FileProvider.
     * Returns null if the file doesn't exist or is empty.
     */
    fun getLogFile(): File? {
        val f = diagFile
        return if (f.exists() && f.length() > 0) f else null
    }

    companion object {
        const val TAG = "ARC_SCAN_DIAG"
    }
}
