package com.aeswox.arcmusic.data.network

object ApiKeys {
    // Note: In production, these should be securely injected via BuildConfig or a secure keystore,
    // rather than hardcoded here. LastFM is currently overridden by SettingsRepository at runtime
    // if configured by the user.
    const val LAST_FM_API_KEY = "YOUR_LAST_FM_API_KEY"
    const val THE_AUDIO_DB_API_KEY = "123" // Free tier testing key
}
