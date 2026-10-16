package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class AppReleaseInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseDate: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSizeBytes: Long = 0L,
    val isMandatory: Boolean = false
)

sealed interface UpdateCheckState {
    data object Idle : UpdateCheckState
    data object Checking : UpdateCheckState
    data class Available(val release: AppReleaseInfo) : UpdateCheckState
    data class UpToDate(val currentVersionName: String) : UpdateCheckState
    data class Error(val message: String) : UpdateCheckState
}

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : DownloadState
    data class ReadyToInstall(val apkFile: File, val apkUri: Uri) : DownloadState
    data class Error(val message: String) : DownloadState
}

class AppUpdateManager(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Default public endpoint or raw GitHub/hosting URL.
    // If not reachable, fallback to mock demo or user-configured URL.
    companion object {
        const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/alldav/Meus-Apps/main/version.json"
        const val PREFS_NAME = "app_update_prefs"
        const val KEY_AUTO_CHECK = "auto_check_updates"
        const val KEY_CUSTOM_URL = "custom_update_url"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isAutoCheckEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CHECK, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CHECK, value).apply()

    var updateCheckUrl: String
        get() {
            val stored = prefs.getString(KEY_CUSTOM_URL, DEFAULT_UPDATE_URL) ?: DEFAULT_UPDATE_URL
            return if (stored.contains("alldav-am/plantao-folga")) {
                prefs.edit().putString(KEY_CUSTOM_URL, DEFAULT_UPDATE_URL).apply()
                DEFAULT_UPDATE_URL
            } else {
                stored
            }
        }
        set(value) = prefs.edit().putString(KEY_CUSTOM_URL, value).apply()

    val currentVersionName: String
        get() = BuildConfig.VERSION_NAME

    val currentVersionCode: Int
        get() = BuildConfig.VERSION_CODE

    /**
     * Checks online endpoint for newer versions.
     */
    suspend fun checkForUpdates(url: String = updateCheckUrl): UpdateCheckState = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "PlantaoFolga-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext UpdateCheckState.Error("Servidor retornou código ${response.code} ao verificar atualizações.")
            }

            val body = response.body?.string()
                ?: return@withContext UpdateCheckState.Error("Resposta vazia do servidor de atualizações.")

            val json = JSONObject(body)
            val remoteVersionCode = json.optInt("versionCode", 1)
            val remoteVersionName = json.optString("versionName", "1.0.0")
            val releaseDate = json.optString("releaseDate", "")
            val releaseNotes = json.optString("releaseNotes", "Melhorias de estabilidade e novas opções de escalas.")
            val downloadUrl = json.optString("downloadUrl", "")
            val fileSizeBytes = json.optLong("fileSizeBytes", 0L)
            val isMandatory = json.optBoolean("isMandatory", false)

            if (remoteVersionCode > BuildConfig.VERSION_CODE) {
                UpdateCheckState.Available(
                    AppReleaseInfo(
                        versionCode = remoteVersionCode,
                        versionName = remoteVersionName,
                        releaseDate = releaseDate,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        fileSizeBytes = fileSizeBytes,
                        isMandatory = isMandatory
                    )
                )
            } else {
                UpdateCheckState.UpToDate(BuildConfig.VERSION_NAME)
            }
        } catch (e: Exception) {
            UpdateCheckState.Error("Não foi possível conectar ao servidor de atualizações: ${e.localizedMessage ?: "Sem conexão"}")
        }
    }

    /**
     * Generates a simulated update for immediate in-app testing and verification.
     */
    fun createSimulatedUpdate(): AppReleaseInfo {
        return AppReleaseInfo(
            versionCode = BuildConfig.VERSION_CODE + 1,
            versionName = "1.1.0",
            releaseDate = "14/10/2026",
            releaseNotes = "• Atualizador automático via internet integrado\n• Suporte completo a escalas personalizadas (3x2, 2x2, etc.)\n• Assistente inicial de configuração passo a passo\n• Marcação flexível de férias com períodos quebrados\n• Melhorias na visualização e leitura para todas as idades",
            downloadUrl = "https://github.com/alldav-am/plantao-folga/releases/download/v1.1.0/plantao-folga-v1.1.0.apk",
            fileSizeBytes = 18_500_000L,
            isMandatory = false
        )
    }

    /**
     * Downloads the APK file to the app's cache directory and reports progress.
     */
    suspend fun downloadApk(
        release: AppReleaseInfo,
        onProgress: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()

        val fileName = "plantao_folga_v${release.versionName}.apk"
        val outputFile = File(updatesDir, fileName)

        // If the URL is a real APK link, download it; if mock or simulated link fails, create a valid APK placeholder
        try {
            onProgress(DownloadState.Downloading(5, 0, release.fileSizeBytes))

            val request = Request.Builder()
                .url(release.downloadUrl)
                .header("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")
                .build()

            val response = try {
                httpClient.newCall(request).execute()
            } catch (netEx: Exception) {
                null
            }

            if (response == null || !response.isSuccessful || response.body == null) {
                val errorCode = response?.code?.toString() ?: "Falha de conexão"
                onProgress(DownloadState.Error("Não foi possível baixar o instalador ($errorCode). Toque em 'Navegador' para baixar."))
                return@withContext
            }

            val body = response.body!!
            val contentLength = if (body.contentLength() > 0) body.contentLength() else release.fileSizeBytes
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                val progress = if (contentLength > 0) {
                    ((totalRead * 100) / contentLength).toInt().coerceIn(0, 99)
                } else 50
                onProgress(DownloadState.Downloading(progress, totalRead, contentLength))
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Verify file integrity (must be a valid APK zip file)
            val isValidApk = outputFile.exists() && outputFile.length() > 50_000 && runCatching {
                val header = ByteArray(4)
                outputFile.inputStream().use { it.read(header) }
                // Check ZIP magic header: PK\x03\x04 (0x50, 0x4B, 0x03, 0x04)
                header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
                header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
            }.getOrDefault(false)

            if (!isValidApk) {
                outputFile.delete()
                onProgress(DownloadState.Error("O arquivo baixado não é um instalador APK válido. Use o botão 'Navegador' para baixar."))
                return@withContext
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )

            onProgress(DownloadState.ReadyToInstall(outputFile, apkUri))
        } catch (e: Exception) {
            onProgress(DownloadState.Error("Falha no download: ${e.localizedMessage ?: "Erro desconhecido"}"))
        }
    }

    /**
     * Checks if the app has permission to request package installation.
     */
    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Triggers the Android system package installer for the given APK.
     */
    fun installApk(apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    /**
     * Opens the Unknown App Sources settings if permission was not yet granted.
     */
    fun openUnknownSourcesSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Opens download link directly in the device's web browser.
     */
    fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
