package com.tlscontact.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import javax.inject.Inject

class HttpClientService @Inject constructor() {
    val httpClient: HttpClient = HttpClient(CIO)
}