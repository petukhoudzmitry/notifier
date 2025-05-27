package com.tlscontact

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tlscontact.repositories.PageRepository
import com.tlscontact.services.DataProcessingService
import com.tlscontact.services.GitHubUpdateService
import com.tlscontact.services.NetworkLiveDataService
import com.tlscontact.services.NotificationService
import com.tlscontact.services.URLService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var gitHubUpdateService: GitHubUpdateService
    @Inject lateinit var dataProcessingService: DataProcessingService
    @Inject lateinit var networkLiveDataService: NetworkLiveDataService
    @Inject lateinit var pageRepository: PageRepository
    @Inject lateinit var urlService: URLService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val htmlContent = mutableStateOf("")
        val internetConnection = mutableStateOf( false )
        val updateAvailable = mutableStateOf(false)
        val loading = mutableStateOf(true)

        lifecycleScope.launch {
            htmlContent.value = pageRepository.getPage()
        }

        networkLiveDataService.observe(this) { isConnected ->
            internetConnection.value = isConnected
            if (isConnected) {
                lifecycleScope.launch {
                    htmlContent.value = dataProcessingService.fetchData()
                    updateAvailable.value = gitHubUpdateService.checkGitHubUpdate()
                }
            } else {
                Toast.makeText(this, getString(R.string.error_message_no_internet), Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            if (htmlContent.value.isNotBlank()) {
                HtmlWebView(htmlContent, urlService.baseURL, loading)
            }
            if (loading.value) {
                Box (
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }

            if (updateAvailable.value) {
                gitHubUpdateService.ShowUpdateDialog(this)
            }
        }

        val intent = Intent(this, NotificationService::class.java)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startService(intent)
            } else {
                Toast.makeText(this, getString(R.string.error_message_notification_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startService(intent)
            }
        } else {
            startService(intent)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlWebView(htmlContent: MutableState<String>, baseURL: String, loading: MutableState<Boolean>) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        loading.value = true
                    }
                    override fun onPageFinished(view: WebView?, url: String?) {
                        loading.value = false
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(baseURL, htmlContent.value, "text/html", "UTF-8", null)
        }
    )
}
