package com.tlscontact.repositories

import androidx.datastore.core.DataStore
import com.tlscontact.models.Configuration
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(private val configurationDataStore: DataStore<Configuration>) {
    suspend fun getConfiguration(): String {
        return configurationDataStore.data.map { it.title }.firstOrNull() ?: ""
    }

    suspend fun updateConfiguration(data: String) {
        configurationDataStore.updateData { configuration -> configuration.toBuilder().setTitle(data).build() }
    }
}