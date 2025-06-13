package com.jlss.smartDairy.navigation


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.viewmodel.AppLockViewModel
import com.jlss.smartDairy.viewmodel.AppStateViewModel

class AppLockViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppLockViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(application).appLockDao()
            @Suppress("UNCHECKED_CAST")
            return AppLockViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



class AppStateViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppStateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppStateViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}