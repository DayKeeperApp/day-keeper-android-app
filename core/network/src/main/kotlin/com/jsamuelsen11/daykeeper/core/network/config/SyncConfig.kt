package com.jsamuelsen11.daykeeper.core.network.config

private const val DEFAULT_REQUEST_TIMEOUT_MS = 30_000L
private const val DEFAULT_CONNECT_TIMEOUT_MS = 10_000L

data class SyncConfig(
  val baseUrl: String,
  val requestTimeoutMs: Long = DEFAULT_REQUEST_TIMEOUT_MS,
  val connectTimeoutMs: Long = DEFAULT_CONNECT_TIMEOUT_MS,
)
