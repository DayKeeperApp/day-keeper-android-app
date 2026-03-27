package com.jsamuelsen11.daykeeper.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

private const val PREFS_FILE_NAME = "daykeeper_auth_prefs"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"

/** [TokenStore] backed by [EncryptedSharedPreferences] for secure token persistence. */
class EncryptedTokenStore(context: Context) : TokenStore {

  private val prefs: SharedPreferences by lazy {
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    EncryptedSharedPreferences.create(
      PREFS_FILE_NAME,
      masterKeyAlias,
      context,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
  }

  override suspend fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

  override suspend fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

  override suspend fun saveTokens(accessToken: String, refreshToken: String) {
    prefs
      .edit()
      .putString(KEY_ACCESS_TOKEN, accessToken)
      .putString(KEY_REFRESH_TOKEN, refreshToken)
      .apply()
  }

  override suspend fun clear() {
    prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).apply()
  }
}
