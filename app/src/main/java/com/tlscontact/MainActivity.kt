package com.tlscontact

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.tlscontact.services.DataProcessingService
import com.tlscontact.services.GitHubUpdateService
import com.tlscontact.services.NetworkLiveDataService
import com.tlscontact.services.NotificationChannelService
import com.tlscontact.services.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var gitHubUpdateService: GitHubUpdateService
    @Inject lateinit var dataProcessingService: DataProcessingService
    @Inject lateinit var networkLiveDataService: NetworkLiveDataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val htmlContent = mutableStateOf("")
        val internetConnection = mutableStateOf( false )

        networkLiveDataService.observe(this) { isConnected ->
            internetConnection.value = isConnected
            if (isConnected) {
                runBlocking {
                    htmlContent.value = dataProcessingService.fetchData()
                }
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            HtmlWebView(htmlContent)
            if (internetConnection.value) {
                gitHubUpdateService.CheckGitHubUpdate(this)
            }
        }


        val intent = Intent(this, NotificationService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED).not()) {
                NotificationChannelService.requestNotificationPermission(this)
                Toast.makeText(this, "For service to work properly, please grant notification permission", Toast.LENGTH_LONG).show()
            }

            while (true) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    startService(intent)
                    break
                }
            }
        } else {
            startService(intent)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlWebView(htmlContent: MutableState<String>) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                }
                loadDataWithBaseURL(null, htmlContent.value, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent.value, "text/html", "UTF-8", null)
        }
    )
}
