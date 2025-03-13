package com.tlscontact.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tlscontact.repositories.ConfigurationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : Service() {
    @Inject lateinit var dataProcessingService: DataProcessingService
    @Inject lateinit var configurationRepository: ConfigurationRepository
    @Inject lateinit var notificationChannelService: NotificationChannelService

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startRequestLoop()
    }

    override fun onBind(intent: Intent?) = null

    private fun startForegroundService() {
        val channelId = "com.tlscontact.services.NotificationService"
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notification = Notification.Builder(this, channelId)
                .setContentTitle("Background Service")
                .setContentText("Running in the background...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
            startForeground(1, notification)
        }
    }

    private fun startRequestLoop() {
        coroutineScope.launch {
            while (true) {
                val latestNews = dataProcessingService.getLatestNews()
                val latestSavedNews = configurationRepository.getConfiguration()

                Log.i("FETCHED_DATA", latestNews)

                if (latestNews != latestSavedNews) {
                    configurationRepository.updateConfiguration(latestNews)
                    notificationChannelService.sendNotification(latestNews)
                }

                delay(60_000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}