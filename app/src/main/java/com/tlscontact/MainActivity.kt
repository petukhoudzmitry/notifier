package com.tlscontact

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.tlscontact.services.DataProcessingService
import com.tlscontact.services.HttpClientService
import com.tlscontact.ui.theme.NotifierTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var httpClientService: HttpClientService
    @Inject lateinit var dataProcessingService: DataProcessingService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotifierTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "world",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }


        lifecycleScope.launch {
            Log.i("FETCHED_DATA", dataProcessingService.getLatestNews())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        httpClientService.httpClient.close()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotifierTheme {
        Greeting("Android")
    }
}