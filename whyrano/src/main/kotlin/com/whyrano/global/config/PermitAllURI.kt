package com.whyrano.global.config

import org.springframework.http.HttpMethod

/**
 * Created by ShinD on 2022/08/17.
 */
enum class PermitAllURI(val method: HttpMethod?, val uri: Array<String>,) {

    ALL_METHOD(null, arrayOf(URI.H2_URI.uri, URI.ERROR_URI.uri,URI.LOGIN_URI.uri)),

    GET(HttpMethod.GET, arrayOf(URI.POST_SEARCH_URI.uri)),

    POST(HttpMethod.POST, arrayOf(URI.SIGNUP_URI.uri)),
    ;

    companion object {
        fun permitAllMap(): Map<HttpMethod?, Array<String>> {
            return values().toList().associate { it.method to it.uri }
        }
    }
    enum class URI(val uri: String) {
        LOGIN_URI("/login"),
        SIGNUP_URI("/signup"),
        H2_URI("/h2-console/**"),
        ERROR_URI("/error"),
        POST_SEARCH_URI("/post"),
        ;
    }
}
