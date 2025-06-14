// File: com.jlss.mukeshdairy.viewmodel.AppLockViewModel.kt
package com.jlss.smartDairy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.dao.AppLockDao
import com.jlss.smartDairy.data.model.AppLockEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppLockViewModel(
    private val appLockDao: AppLockDao
) : ViewModel() {

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> get() = _pin


    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> get() = _isUnlocked

    fun loadPin() {
        viewModelScope.launch {
            val savedPin = appLockDao.getPin()
            _pin.value = savedPin?.pin ?: ""
        }
    }
    fun setUnlocked() {
        _isUnlocked.value = true
    }


    fun verifyPin(enteredPin: String) {
        _isUnlocked.value = (enteredPin == _pin.value)
    }

    fun setNewPin(newPin: String) {
        viewModelScope.launch {
            appLockDao.setPin(AppLockEntity(pin = newPin))
            _pin.value = newPin
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            appLockDao.clearPin()
            _pin.value = ""
            _isUnlocked.value = false
        }
    }
}
