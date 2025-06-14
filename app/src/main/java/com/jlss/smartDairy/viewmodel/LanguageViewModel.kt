package com.jlss.smartDairy.viewmodel

// com.jlss.smartDairy.language.LanguageViewModel.kt
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.LanguagePreferenceHelper
import com.jlss.smartDairy.data.UserPreferences
import com.jlss.smartDairy.language.AppLanguage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class LanguageViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val userPreferences = UserPreferences(context)

    private val _selectedLang = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLang: StateFlow<AppLanguage> = _selectedLang

    init {
        viewModelScope.launch {
            userPreferences.getAppLanguage.collect { code ->
                val lang = AppLanguage.fromCode(code)
                _selectedLang.value = lang
                setAppLocale(code)
            }
        }
    }

    fun changeLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            userPreferences.setAppLanguage(lang.code)
            _selectedLang.value = lang
            setAppLocale(lang.code)
        }
    }

    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
