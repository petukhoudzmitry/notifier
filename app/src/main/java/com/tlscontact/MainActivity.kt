package com.tlscontact

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.tlscontact.model.Page
import com.tlscontact.repository.PageRepository
import com.tlscontact.services.DataProcessingService
import com.tlscontact.services.GitHubUpdateService
import com.tlscontact.services.NetworkLiveDataService
import com.tlscontact.services.NotificationService
import com.tlscontact.services.URLService
import com.tlscontact.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var gitHubUpdateService: GitHubUpdateService

    @Inject
    lateinit var dataProcessingService: DataProcessingService

    @Inject
    lateinit var networkLiveDataService: NetworkLiveDataService

    @Inject
    lateinit var urlService: URLService

    @Inject
    lateinit var pageRepository: PageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val newsContent = mutableStateOf<List<Page>>(emptyList())
        val internetConnection = mutableStateOf(false)
        val updateAvailable = mutableStateOf(false)
        val loading = mutableStateOf(true)
        val selectedTab = mutableIntStateOf(0)


        lifecycleScope.launch {
            newsContent.value = dataProcessingService.getLatestSavedNews()
        }

        networkLiveDataService.observe(this) { isConnected ->
            internetConnection.value = isConnected
            if (isConnected) {
                lifecycleScope.launch {
                    newsContent.value = dataProcessingService.getLatestNews()
                    updateAvailable.value = gitHubUpdateService.checkGitHubUpdate()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.error_message_no_internet),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContent {
            AppTheme {
                Surface {
                    if (newsContent.value.isNotEmpty()) {
                        NewsView(newsContent, selectedTab)
                        loading.value = false
                    }
                    if (loading.value) {
                        Box(
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
            }
        }

        val intent = Intent(this, NotificationService::class.java)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startService(intent)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.error_message_notification_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startService(intent)
            }
        } else {
            startService(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsView(
    newsContent: MutableState<List<Page>>,
    selectedTab: MutableIntState
) {
    val scrollStates = remember { mutableStateMapOf<Int, LazyListState>() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val newsContent = newsContent.value
    val pagerState =
        rememberPagerState(initialPage = selectedTab.intValue, pageCount = { newsContent.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedTab.intValue = page
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()) {
        PrimaryTabRow(
            selectedTab.intValue
        ) {
            newsContent.forEachIndexed { index, page ->
                Tab(
                    selected = selectedTab.intValue == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = page.name.split("-")[0].uppercase(),
                            style = if (selectedTab.intValue == index)
                                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            else
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            val currentPage = newsContent.getOrNull(index)
            currentPage?.let { page ->
                val listState = scrollStates.getOrPut(index) { LazyListState() }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(page.articles) { article ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, page.url.toUri())
                                    context.startActivity(intent)
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = article.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = article.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Read more",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
