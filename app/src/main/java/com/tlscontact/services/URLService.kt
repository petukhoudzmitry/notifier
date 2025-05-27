package com.tlscontact.services

import java.net.URL
import javax.inject.Inject

class URLService @Inject constructor() {
    var url: URL = URL("https://it.tlscontact.com/by/msq/page.php?pid=news")
    var baseURL: String = "https://it.tlscontact.com"
}