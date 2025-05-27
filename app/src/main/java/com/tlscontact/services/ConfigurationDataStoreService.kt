package com.tlscontact.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.tlscontact.models.Configuration
import com.tlscontact.models.Page
import com.tlscontact.serializers.ConfigurationSerializer
import com.tlscontact.serializers.PageSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

private const val DATA_STORE_FILE_NAME = "configuration.pb"
private const val PAGE_DATA_STORE_FILE_NAME = "page.pb"

private val Context.configurationDataStore: DataStore<Configuration> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = ConfigurationSerializer
)

private val Context.pageDataStore: DataStore<Page> by dataStore(
    fileName = PAGE_DATA_STORE_FILE_NAME,
    serializer = PageSerializer
)

@Module
@InstallIn(SingletonComponent::class)
class ConfigurationDataStoreService {
    @Provides
    fun provideConfigurationDataStore(@ApplicationContext context: Context): DataStore<Configuration> {
        return context.configurationDataStore
    }

    @Provides
    fun providePageDataStore(@ApplicationContext context: Context): DataStore<Page> {
        return context.pageDataStore
    }
}
