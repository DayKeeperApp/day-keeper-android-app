package com.jsamuelsen11.daykeeper.core.network.auth

/** Secure storage for authentication tokens. */
interface TokenStore {
  suspend fun getAccessToken(): String?

  suspend fun getRefreshToken(): String?

  suspend fun saveTokens(accessToken: String, refreshToken: String)

  suspend fun clear()
}
