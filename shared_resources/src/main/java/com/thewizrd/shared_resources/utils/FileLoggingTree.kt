package com.thewizrd.shared_resources.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.thewizrd.shared_resources.BuildConfig
import com.thewizrd.shared_resources.utils.Logger.DEBUG_MODE_ENABLED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors

@SuppressLint("LogNotTimber")
class FileLoggingTree(context: Context) : Timber.Tree() {
    private val scope =
        CoroutineScope(Job() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    private val logDirectory = File(context.getExternalFilesDir(null).toString() + "/logs")

    companion object {
        private val TAG = FileLoggingTree::class.java.simpleName
        private const val DAYS_TO_KEEP = 7
    }

    init {
        if (!logDirectory.exists()) {
            logDirectory.mkdir()
        }
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return if (BuildConfig.DEBUG || DEBUG_MODE_ENABLED) {
            true
        } else {
            priority > Log.DEBUG
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        scope.launch {
            logEntry(priority, tag, message, t)
        }
    }

    private fun logEntry(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            if (!logDirectory.exists()) {
                logDirectory.mkdir()
            }

            val today = LocalDateTime.now(ZoneOffset.UTC)

            val dateTimeStamp = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT))
            val logTimeStamp = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS", Locale.ROOT))

            val logNameFormat = "Logger.%s.log"
            val fileName = String.format(Locale.ROOT, logNameFormat, dateTimeStamp)

            val file = File(logDirectory.path + File.separator + fileName)

            if (!file.exists())
                file.createNewFile()

            if (file.exists()) {
                val fileWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(file, true)))

                val priorityTAG = when (priority) {
                    Log.VERBOSE -> "VERBOSE"
                    Log.DEBUG -> "DEBUG"
                    Log.INFO -> "INFO"
                    Log.WARN -> "WARN"
                    Log.ERROR -> "ERROR"
                    Log.ASSERT -> "ASSERT"
                    else -> "DEBUG"
                }

                fileWriter.write("$logTimeStamp|$priorityTAG|${if (tag == null) "" else "$tag|"}$message\n")
                fileWriter.flush()
                fileWriter.close()
            }

            val existingFiles = logDirectory.listFiles { dir, name ->
                name.startsWith("Logger")
            } ?: emptyArray<File>()

            // Cleanup old logs if they exist
            if (existingFiles.size > DAYS_TO_KEEP) {
                runCatching {
                    // Get todays date
                    val todayLocal = today.toLocalDate()

                    // Create a list of the last 7 day's dates
                    val dateStampsToKeep = mutableListOf<String>()
                    for (i in 0 until DAYS_TO_KEEP) {
                        val date = todayLocal.minusDays(i.toLong())
                        val dateStamp =
                            date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT))

                        dateStampsToKeep.add(String.format(Locale.ROOT, logNameFormat, dateStamp))
                    }

                    // List all log files not in the above list
                    val logs = existingFiles.filterNot { f -> dateStampsToKeep.contains(f.name) }

                    // Delete all log files in the array above
                    for (logToDel in logs) {
                        logToDel.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while logging into file : $e")
        }
    }
}