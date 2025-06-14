package com.jlss.smartDairy.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jlss.smartDairy.viewmodel.EntryRowState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_prefs")

object PrefKeys {
    val ACCOUNT_CREATED = booleanPreferencesKey("account_created")
    val APP_LOCKED = booleanPreferencesKey("app_locked")
    val UNSAVED_ROWS = stringPreferencesKey("unsaved_rows")
    val LANGUAGE = stringPreferencesKey("app_language")
}

class UserPreferences(private val context: Context) {

    val isAccountCreated: Flow<Boolean> = context.dataStore.data
        .map { it[PrefKeys.ACCOUNT_CREATED] ?: false }

    val isAppLocked: Flow<Boolean> = context.dataStore.data
        .map { it[PrefKeys.APP_LOCKED] ?: true }

    suspend fun setAccountCreated(value: Boolean) {
        context.dataStore.edit { it[PrefKeys.ACCOUNT_CREATED] = value }
    }

    suspend fun setAppLocked(value: Boolean) {
        context.dataStore.edit { it[PrefKeys.APP_LOCKED] = value }
    }

    // rows unsaved data
    private val gson = Gson()

    suspend fun saveUnsavedRows(rows: List<EntryRowState>) {
        val json = gson.toJson(rows)
        context.dataStore.edit {
            it[PrefKeys.UNSAVED_ROWS] = json
        }
    }

    suspend fun clearUnsavedRows() {
        context.dataStore.edit {
            it.remove(PrefKeys.UNSAVED_ROWS)
        }
    }

    val getUnsavedRows: Flow<List<EntryRowState>> = context.dataStore.data.map {
        val json = it[PrefKeys.UNSAVED_ROWS]
        if (json.isNullOrBlank()) emptyList()
        else {
            val type = object : TypeToken<List<EntryRowState>>() {}.type
            gson.fromJson(json, type)
        }
    }
    // app language get set -- currently not using db just shared preferences based choice
    suspend fun setAppLanguage(languageCode: String) {
        context.dataStore.edit {
            it[PrefKeys.LANGUAGE] = languageCode
        }
    }

    val getAppLanguage: Flow<String> = context.dataStore.data
        .map { it[PrefKeys.LANGUAGE] ?: "en" }

    fun getAppLanguageSync(): String {
        val preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return preferences.getString("app_language", "en") ?: "en"
    }


}

