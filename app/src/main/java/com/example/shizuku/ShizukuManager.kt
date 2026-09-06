package com.example.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.data.model.CommandLogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

enum class ShizukuStatus {
    AVAILABLE_AUTHORIZED,
    AVAILABLE_UNAUTHORIZED,
    UNAVAILABLE,
    PERMISSION_DENIED
}

class ShizukuManager(private val context: Context) {

    private val _status = MutableStateFlow(ShizukuStatus.UNAVAILABLE)
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val _commandLogs = MutableStateFlow<List<CommandLogEntry>>(emptyList())
    val commandLogs: StateFlow<List<CommandLogEntry>> = _commandLogs.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _status.value = ShizukuStatus.UNAVAILABLE
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    _status.value = ShizukuStatus.AVAILABLE_AUTHORIZED
                    logEntry("Shizuku Permission Granted", 0, "Authorized by user.")
                } else {
                    _status.value = ShizukuStatus.PERMISSION_DENIED
                    logEntry("Shizuku Permission Denied", 1, "Permission denied by user.")
                }
            }
        }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Shizuku listeners", e)
            _status.value = ShizukuStatus.UNAVAILABLE
        }
    }

    fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Exception) {}
    }

    fun isAuthorized(): Boolean {
        return try {
            if (!Shizuku.pingBinder()) return false
            if (Shizuku.isPreV11()) {
                context.checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            Log.w(TAG, "isAuthorized check failed: ${e.message}")
            false
        }
    }

    fun checkStatus(): ShizukuStatus {
        val currentStatus = try {
            if (Shizuku.pingBinder()) {
                if (isAuthorized()) {
                    ShizukuStatus.AVAILABLE_AUTHORIZED
                } else if (!Shizuku.isPreV11() && Shizuku.shouldShowRequestPermissionRationale()) {
                    ShizukuStatus.AVAILABLE_UNAUTHORIZED
                } else {
                    ShizukuStatus.AVAILABLE_UNAUTHORIZED
                }
            } else {
                ShizukuStatus.UNAVAILABLE
            }
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku pingBinder failed: ${e.message}")
            ShizukuStatus.UNAVAILABLE
        }
        _status.value = currentStatus
        return currentStatus
    }

    fun getShizukuDetails(): String {
        return try {
            if (Shizuku.pingBinder() && isAuthorized()) {
                val uid = Shizuku.getUid()
                val version = Shizuku.getVersion()
                val mode = if (uid == 0) "Root (UID 0)" else "ADB Shell (UID $uid)"
                "$mode • API v$version"
            } else if (Shizuku.pingBinder()) {
                "Service Connected • Awaiting Permission"
            } else {
                "Service Inactive"
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.isPreV11()) {
                    _status.value = if (context.checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED)
                        ShizukuStatus.AVAILABLE_AUTHORIZED else ShizukuStatus.AVAILABLE_UNAUTHORIZED
                } else {
                    if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                        Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
                    } else {
                        _status.value = ShizukuStatus.AVAILABLE_AUTHORIZED
                    }
                }
            } else {
                _status.value = ShizukuStatus.UNAVAILABLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Shizuku permission", e)
            _status.value = ShizukuStatus.UNAVAILABLE
        }
    }

    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        val trimmedCmd = command.trim()
        if (trimmedCmd.isEmpty()) {
            return@withContext CommandResult(0, "", "Empty command")
        }

        val isAuth = isAuthorized()

        if (!isAuth) {
            val fallbackMsg = "Shizuku Service is not active or unauthorized. You can run this command via ADB:\n\nadb shell $trimmedCmd"
            logEntry(trimmedCmd, -1, fallbackMsg)
            return@withContext CommandResult(-1, "", fallbackMsg)
        }

        try {
            val process: Process = try {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                ).apply {
                    isAccessible = true
                }
                newProcessMethod.invoke(null, arrayOf("sh", "-c", trimmedCmd), null, null) as Process
            } catch (e: Throwable) {
                Log.w(TAG, "Shizuku newProcess reflection failed: ${e.message}, falling back to Runtime exec")
                Runtime.getRuntime().exec(arrayOf("sh", "-c", trimmedCmd))
            }
            val stdout = StringBuilder()
            val stderr = StringBuilder()

            val outReader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))

            var line: String?
            while (outReader.readLine().also { line = it } != null) {
                stdout.append(line).append("\n")
            }
            while (errReader.readLine().also { line = it } != null) {
                stderr.append(line).append("\n")
            }

            val exitCode = process.waitFor()
            val output = if (stdout.isNotEmpty()) stdout.toString().trim() else stderr.toString().trim()

            logEntry(trimmedCmd, exitCode, output.ifEmpty { "Command executed with exit code $exitCode" })
            CommandResult(exitCode, stdout.toString().trim(), stderr.toString().trim())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute Shizuku command: $trimmedCmd", e)
            val err = e.message ?: "Unknown execution error"
            logEntry(trimmedCmd, 1, err)
            CommandResult(1, "", err)
        }
    }

    suspend fun executeBatch(commands: List<String>): List<CommandResult> = withContext(Dispatchers.IO) {
        commands.map { executeCommand(it) }
    }

    fun clearLogs() {
        _commandLogs.value = emptyList()
    }

    private fun logEntry(command: String, exitCode: Int, output: String) {
        val entry = CommandLogEntry(
            command = command,
            exitCode = exitCode,
            output = output,
            isSuccess = exitCode == 0
        )
        _commandLogs.value = (listOf(entry) + _commandLogs.value).take(100)
    }

    companion object {
        const val REQUEST_CODE_PERMISSION = 7001
        private const val TAG = "ShizukuManager"
    }
}

data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}
