package com.tlscontact.services

import com.tlscontact.repository.PageRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DataProcessingServiceTest {
    private val httpClientService = HttpClientService()
    private val urlService = URLService()

    @Mock
    private lateinit var pageRepository: PageRepository

    private lateinit var dataProcessingService: DataProcessingService

    @Before
    fun setup() {
        pageRepository = mock()

        dataProcessingService = DataProcessingService(
            httpClientService,
            urlService,
            pageRepository
        )
    }

    @Test
    fun `fetch news list from github is correct`() = runTest {
        val result = dataProcessingService.fetchNewsListFromGitHub()

        assert(result.isNotEmpty())
    }

    @Test
    fun `data fetch is correct`() = runTest {
        val newsListToFetch = dataProcessingService.fetchNewsListFromGitHub()
        val result = dataProcessingService.fetchData()

        assert(newsListToFetch.isNotEmpty())
        assert(result.isNotEmpty())
        assert(result.size == newsListToFetch.size)
    }
}