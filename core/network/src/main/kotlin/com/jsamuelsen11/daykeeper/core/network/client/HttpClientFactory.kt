package com.jsamuelsen11.daykeeper.core.network.client

import com.jsamuelsen11.daykeeper.core.network.auth.TokenStore
import com.jsamuelsen11.daykeeper.core.network.config.SyncConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

  fun create(config: SyncConfig, tokenStore: TokenStore): HttpClient =
    HttpClient(OkHttp) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
          }
        )
      }

      install(Logging) { level = LogLevel.HEADERS }

      install(Auth) {
        bearer {
          loadTokens {
            val access = tokenStore.getAccessToken()
            val refresh = tokenStore.getRefreshToken()
            if (access != null && refresh != null) {
              BearerTokens(access, refresh)
            } else {
              null
            }
          }
          refreshTokens {
            // Stub: auth not implemented on backend yet.
            // When backend adds auth, implement refresh logic here.
            null
          }
        }
      }

      defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)
      }

      engine {
        config {
          connectTimeout(config.connectTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
          readTimeout(config.requestTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        }
      }
    }
}
