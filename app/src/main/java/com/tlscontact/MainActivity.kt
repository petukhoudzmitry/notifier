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
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import com.tlscontact.models.Configuration
import com.tlscontact.services.DataProcessingService
import com.tlscontact.services.HttpClientService
import com.tlscontact.ui.theme.NotifierTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var httpClientService: HttpClientService
    @Inject lateinit var dataProcessingService: DataProcessingService
    @Inject lateinit var configurationDataStore: DataStore<Configuration>

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
            val latestNews = dataProcessingService.getLatestNews()
            val latestSavedNews = configurationDataStore.data.map { it.title }.first()
            Log.i("FETCHED_DATA", latestNews)
            Log.i("SAVED_DATA", latestSavedNews)
            Log.i("COMPARED_DATA", "${latestNews == latestSavedNews}")
            if (latestNews != latestSavedNews) {
                saveData(latestNews)
                Log.i("UPDATED_DATA", latestNews)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        httpClientService.httpClient.close()
    }

    suspend fun saveData(data: String) {
        configurationDataStore.updateData { configuration ->
            configuration.toBuilder().setTitle(data).build()
        }
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