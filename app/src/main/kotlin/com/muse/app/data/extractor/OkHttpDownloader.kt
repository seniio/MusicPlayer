package com.muse.app.data.extractor

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException

class OkHttpDownloader(private val client: OkHttpClient) : Downloader() {

    @Throws(IOException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val body = if (httpMethod == "POST" || httpMethod == "PUT" || httpMethod == "PATCH") {
            val contentType = headers["Content-Type"]?.firstOrNull() ?: "application/json; charset=utf-8"
            (dataToSend ?: ByteArray(0)).toRequestBody(contentType.toMediaType())
        } else {
            null
        }

        val okRequestBuilder = okhttp3.Request.Builder()
            .url(url)
            .method(httpMethod, body)

        if (!headers.containsKey("User-Agent")) {
            okRequestBuilder.header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
            )
        }

        for ((key, values) in headers) {
            for (value in values) {
                okRequestBuilder.addHeader(key, value)
            }
        }

        val okResponse = client.newCall(okRequestBuilder.build()).execute()

        val responseBody = okResponse.body?.string()
        val responseCode = okResponse.code
        val responseMessage = okResponse.message
        val responseHeaders = okResponse.headers.toMultimap()
        val latestUrl = okResponse.request.url.toString()

        return Response(
            responseCode,
            responseMessage,
            responseHeaders,
            responseBody,
            latestUrl
        )
    }
}
