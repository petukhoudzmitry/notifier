package com.tlscontact.repositories

import androidx.datastore.core.DataStore
import com.tlscontact.models.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PageRepository @Inject constructor(private val pageDataStore: DataStore<Page>) {
    suspend fun getPage(): String {
        return withContext(Dispatchers.IO) {
            pageDataStore.data.map { it.content }.firstOrNull() ?: ""
        }
    }

    suspend fun updatePage(data: String) {
        withContext(Dispatchers.IO) {
            pageDataStore.updateData { page -> page.toBuilder().setContent(data).build() }
        }
    }
}