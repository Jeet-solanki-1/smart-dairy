package com.jlss.smartDairy.data

// com.jlss.smartDairy.language.LanguagePreferenceHelper.kt
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object LanguagePreferenceHelper {
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    suspend fun saveLanguage(context: Context, code: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = code
        }
    }

    suspend fun getSavedLanguage(context: Context): String {
        return context.dataStore.data
            .map { prefs -> prefs[LANGUAGE_KEY] ?: "en" }
            .first()
    }


}
