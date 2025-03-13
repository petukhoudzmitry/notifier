package com.tlscontact.services

import android.util.Log
import com.tlscontact.repositories.ConfigurationRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject


class DataProcessingService @Inject constructor(private val httpClientService: HttpClientService,
                                                private val urlService: URLService,
                                                private val cssSelectorService: CSSSelectorService,
                                                private val configurationRepository: ConfigurationRepository) {
    suspend fun fetchData(): String {
        try {
            val response = httpClientService.httpClient.get(urlService.url).bodyAsText()
            return response
        } catch (e: Exception) {
            Log.e("FETCH_DATA", e.stackTraceToString())
        }
        return configurationRepository.getConfiguration()
    }

    suspend fun getLatestNews(): String {
        return withContext(Dispatchers.IO) {
            val document = Jsoup.parse(fetchData())
            document.select(cssSelectorService.cssSelector).firstOrNull()?.text() ?: ""
        }
    }
}