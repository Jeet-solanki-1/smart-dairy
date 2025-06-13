package com.jlss.smartDairy.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.UserPreferences
import kotlinx.coroutines.launch

class AppStateViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)

    val isAccountCreated = prefs.isAccountCreated
    val isAppLocked = prefs.isAppLocked

    init {
        // App always starts in locked state
        viewModelScope.launch {
            prefs.setAppLocked(true)
        }
    }


    fun markAccountCreated() {
        viewModelScope.launch {
            prefs.setAccountCreated(true)
        }
    }

    fun unlockApp() {
        viewModelScope.launch {
            prefs.setAppLocked(false)
        }
    }

    fun lockApp() {
        viewModelScope.launch {
            prefs.setAppLocked(true)
        }
    }

}
