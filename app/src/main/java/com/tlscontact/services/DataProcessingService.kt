package com.tlscontact.services

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tlscontact.model.Article
import com.tlscontact.model.NewsItem
import com.tlscontact.model.Page
import com.tlscontact.repository.PageRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject


class DataProcessingService @Inject constructor(
    private val httpClientService: HttpClientService,
    private val urlService: URLService,
    private val pageRepository: PageRepository
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(Array<NewsItem>::class.java)

    suspend fun fetchNewsListFromGitHub(): List<NewsItem> {
        val result =
            try {
                val response = httpClientService.httpClient.get(urlService.url).bodyAsText()

                jsonAdapter.fromJson(response)?.toList() ?: emptyList()
            } catch (e: Exception) {
                Log.e("FETCH_SITES_EXCEPTION", e.message.toString())
                emptyList()
            }

        return result
    }

    suspend fun fetchData(): List<Page> {
        val pages = mutableListOf<Page>()
        try {
            val newsListToFetch = fetchNewsListFromGitHub()
            newsListToFetch.forEachIndexed { index, it ->
                val html = httpClientService.httpClient.get("${urlService.baseURL}/${it.name}")
                    .bodyAsText()
                val articles = extractArticlesFromHTML(html)

                pages += Page(index, it.name, it.url, articles)
            }
        } catch (e: Exception) {
            Log.e("FETCH_DATA_EXCEPTION", e.message.toString())
        }
        return pages
    }

    suspend fun getLatestNews(): List<Page> {
        return withContext(Dispatchers.IO) {
            val pages = fetchData()
            pages.ifEmpty { pageRepository.getAll() }
        }
    }

    suspend fun getLatestSavedNews(): List<Page> {
        return withContext(Dispatchers.IO) {
            pageRepository.getAll()
        }
    }

    fun Element.findAncestorArticle(): Element? {
        var el: Element? = this
        while (el != null && el.tagName() != "article") el = el.parent()
        return el
    }

    fun extractArticlesFromHTML(html: String): List<Article> {
        val articles = mutableListOf<Article>()

        val document = Jsoup.parse(html)
        val titleElements = document.select("h2[data-testid=title]")

        for (titleElement in titleElements) {
            val article = titleElement.findAncestorArticle() ?: continue

            val title = titleElement.text().trim().ifEmpty { "" }
            val subtitle = article.selectFirst("[data-testid=subtitle]")?.text()?.trim()
            val contentElement = article.selectFirst("[data-testid=content]")
            val contentHtml = contentElement?.html()?.trim()
            val contentText = contentElement?.text()?.trim()

            articles += Article(
                title = title,
                subtitle = subtitle ?: "",
                contentSnippet = contentHtml ?: contentText ?: ""
            )
        }

        return articles
    }
}