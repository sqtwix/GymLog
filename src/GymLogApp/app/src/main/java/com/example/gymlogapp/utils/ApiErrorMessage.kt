package com.example.gymlogapp.utils

import android.content.Context
import com.example.gymlogapp.R
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.serialization.SerializationException
import java.io.IOException

fun getErrorMessage(context: Context, error: Throwable): String = when (error) {
    is ClientRequestException -> when (error.response.status.value) {
        400 -> context.getString(R.string.error_bad_request)
        401 -> context.getString(R.string.error_unauthorized)
        403 -> context.getString(R.string.error_forbidden)
        404 -> context.getString(R.string.error_not_found)
        409 -> context.getString(R.string.error_conflict)
        else -> context.getString(R.string.error_http_client, error.response.status.value)
    }
    is ServerResponseException ->
        context.getString(R.string.error_server, error.response.status.value)
    is HttpRequestTimeoutException -> context.getString(R.string.error_timeout)
    is IOException -> context.getString(R.string.error_network)
    is SerializationException -> context.getString(R.string.error_invalid_response)
    else -> context.getString(R.string.error_unknown)
}
