package com.tlscontact.services

import java.net.URL
import javax.inject.Inject

class URLService @Inject constructor() {
    var baseURL: String = "https://petukhoudzmitry.github.io/notifier"
    var url: URL = URL("$baseURL/news.json")
}