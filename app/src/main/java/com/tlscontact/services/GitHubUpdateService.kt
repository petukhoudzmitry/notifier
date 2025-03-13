package com.tlscontact.services

import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.tlscontact.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject


class GitHubUpdateService @Inject constructor() {
    private val repo = "petukhoudzmitry/notifier"
    private val apiUrl = "https://api.github.com/repos/$repo/releases/latest"
    private val downloadURL = "https://github.com/$repo/releases/latest"

    @Composable
    fun CheckGitHubUpdate(context: Context) {
        val latestVersion = runBlocking(Dispatchers.IO) {
            val response = URL(apiUrl).readText()
            JSONObject(response).getString("tag_name")
        }

        val currentVersion = BuildConfig.VERSION_NAME

        if (latestVersion > currentVersion) {
            ShowUpdateDialog(context, latestVersion)
        }
    }

    @Composable
    private fun ShowUpdateDialog(context: Context, latestVersion: String) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Update Available") },
            text = { Text("A new version ($latestVersion) is available.") },
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