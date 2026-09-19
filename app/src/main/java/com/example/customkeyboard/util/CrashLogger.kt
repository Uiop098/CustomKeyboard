package com.example.customkeyboard.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {
    private const val TAG = "CustomKeyboard"
    private const val FILE_NAME = "crash.log"

    var latestCrash: String? = null
        private set

    fun register(context: Context) {
        val appCtx = context.applicationContext
        val file = File(appCtx.filesDir, FILE_NAME)
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            latestCrash = sw.toString()
            try {
                file.appendText(
                    buildString {
                        append("=== ")
                        append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                        append(" ===\nThread: ")
                        append(thread.name)
                        append('\n')
                        append(sw)
                        append('\n')
                    }
                )
            } catch (_: Exception) {
                // Never let crash logging itself crash.
            }
            Log.e(TAG, "Uncaught crash on ${thread.name}", throwable)
            prev?.uncaughtException(thread, throwable)
        }
    }

    fun readLog(context: Context): String {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) file.readText().trim() else ""
    }

    fun clearLog(context: Context) {
        latestCrash = null
        File(context.filesDir, FILE_NAME).delete()
    }
}