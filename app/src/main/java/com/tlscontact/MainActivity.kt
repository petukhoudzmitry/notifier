package com.tlscontact

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.tlscontact.services.NotificationService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, NotificationService::class.java)
        startService(intent)
    }
}