package com.jlss.smartDairy.viewmodel

import com.jlss.smartDairy.data.model.FatRate
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.AppDatabase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).fatRateDao()

    /**
     * Expose the current rate (or null if never set) as StateFlow<Double?>
     */
    val rate: StateFlow<Double?> = dao
        .get()                                 // Flow<FatRate?>
        .map { it?.ratePerFat }               // Flow<Double?>
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    /**
     * Upsert the single FatRate row (id=0) with the new value.
     */
    fun setRate(newRate: Double) {
        viewModelScope.launch {
            dao.set(FatRate(id = 0, ratePerFat = newRate))
        }
    }
}
