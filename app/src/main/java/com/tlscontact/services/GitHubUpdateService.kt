package com.tlscontact.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.tlscontact.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject


class GitHubUpdateService @Inject constructor() {
    private val repo = "petukhoudzmitry/notifier"
    private val apiUrl = "https://api.github.com/repos/$repo/releases/latest"
    private val downloadURL = "https://github.com/$repo/releases/latest"

    private var currentVersion = BuildConfig.VERSION_NAME

    suspend fun checkGitHubUpdate() : Boolean {
        return withContext(Dispatchers.IO) {
            val latestVersion =
            try {
                val response = URL(apiUrl).readText()
                JSONObject(response).getString("tag_name")
            } catch (e: Exception) {
                Log.e("CheckGitHubUpdate", e.message.toString())
                currentVersion
            }
            isNewerVersion(latestVersion, currentVersion).also { isNewer ->
                currentVersion = if (isNewer) latestVersion else currentVersion
            }
        }
    }

    private fun normalizeVersion(version: String) : List<Int> {
        return version.trimStart('v', 'V').split(".").mapNotNull {
            it.toIntOrNull()
        }
    }

    private fun isNewerVersion(latest: String, current: String) : Boolean {
        val latestVersion = normalizeVersion(latest)
        val currentVersion = normalizeVersion(current)

        return currentVersion.zip(latestVersion).find {
            it.first != it.second
        }?.let {
            it.first < it.second
        } ?: (latestVersion.size > currentVersion.size)
    }

    @Composable
    fun ShowUpdateDialog(context: Context) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Update Available") },
            text = { Text("A new version ($currentVersion) is available.") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, downloadURL.toUri())
                    context.startActivity(intent)
                }) {
                    Text("Update")
                }
            }
        )
    }
}