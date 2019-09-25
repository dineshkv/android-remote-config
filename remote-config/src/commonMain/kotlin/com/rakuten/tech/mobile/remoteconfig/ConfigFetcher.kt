package com.rakuten.tech.mobile.remoteconfig

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.readText
import io.ktor.client.response.HttpResponse
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.Transient

internal class ConfigFetcher constructor(
    baseUrl: String,
    appId: String,
    private val subscriptionKey: String
) {

    private val client = HttpClient()
    var address = Url("$baseUrl/app/$appId/config")

    suspend fun fetch(): Map<String, String> {
        val response = client.get<HttpResponse>(address) {
            header("apiKey", "ras-$subscriptionKey")
        }

        if (response.status.value > 300) {
            throw ResponseException(response)
        }

        return ConfigResponse.fromJsonString(response.readText()).body
    }

    companion object {
        private const val CACHE_SIZE = 1024 * 1024 * 2L
    }
}

@Serializable
private data class ConfigResponse(val body: Map<String, String>) {

    companion object {
        fun fromJsonString(body: String) = Json.nonstrict.parse(serializer(), body)
    }
}

/**
 * Base for default response exceptions.
 * @param response: origin response
 */
open class ResponseException(
        @Transient val response: HttpResponse
) : IllegalStateException("Bad response: $response")