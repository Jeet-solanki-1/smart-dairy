// File: com.jlss.smartDairy.viewmodel.EntryViewModel.kt
package com.jlss.smartDairy.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.UserPreferences
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.ListOfEntry
import com.jlss.smartDairy.data.model.Members
import com.jlss.smartDairy.data.model.Rates
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime

class EntryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val fatDao = db.fatRateDao()
    private val memberDao = db.memberDao()
    private val entryDao = db.entryDao()
    private val listEntryDao = db.listEntryDao()
    private val userPrefs = UserPreferences(application.applicationContext)

    // Rows and computed totals (unchanged)
    private val _rows = MutableStateFlow<List<EntryRowState>>(emptyList())
    val rows: StateFlow<List<EntryRowState>> = _rows

    val totalMilk: StateFlow<Double> = rows
        .map { it.sumOf { r -> r.milkQty.toDoubleOrNull() ?: 0.0 } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val avgFat: StateFlow<Double> = rows
        .map { list ->
            list.mapNotNull { it.fatRate.toDoubleOrNull() }
                .average()
                .takeIf { !it.isNaN() } ?: 0.0
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalAmount: StateFlow<Double> = rows
        .map { it.sumOf { r -> r.amount } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    // Expose the raw Rates Flow from the DAO
    val rates: StateFlow<Rates?> = fatDao.get()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Flag: true when no fat rate is set (null or zero)
    val showMissingFatRate: StateFlow<Boolean> = rates
        .map { it?.ratePerFat == null || it.ratePerFat == 0.0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _wasInitializedByMembers = MutableStateFlow(false)
    val wasInitializedByMembers: StateFlow<Boolean> = _wasInitializedByMembers

    init {
            viewModelScope.launch {
                val saved = userPrefs.getUnsavedRows.first()  // Use `.first()` not `.collect`
                if (saved.isNotEmpty()) {
                    _rows.value = saved
                    _wasInitializedByMembers.value = true
                } else {
                    _rows.value = emptyList()
                    _wasInitializedByMembers.value = false
                }
            }

            viewModelScope.launch {
                rows.collect { userPrefs.saveUnsavedRows(it) }
            }
        }




    fun initRowsFromMembers(members: List<Members>) {
        _rows.value = members.mapIndexed { idx, m ->
            EntryRowState(serialNo = idx + 1, name = m.name)
        }
        _wasInitializedByMembers.value = true
    }


    fun clearRows() {
        _rows.value = emptyList()
    }

    fun addEmptyRow() {
        val next = _rows.value.size + 1
        _rows.value = _rows.value + EntryRowState(serialNo = next)
    }

    /** Updates a single row by fetching the latest fat rate from DB */
    fun updateRow(index: Int, update: EntryRowState.() -> EntryRowState) {
        viewModelScope.launch {
            val currentList = _rows.value.toMutableList()
            val newRow = update(currentList[index])

            // fetch fresh ratePerFat
            val ratePerFat = fatDao.get().first()?.ratePerFat ?: 0.0
            val fat = newRow.fatRate.toDoubleOrNull() ?: 0.0
            val qty = newRow.milkQty.toDoubleOrNull() ?: 0.0

            val amount = BigDecimal(fat.toString())
                .multiply(BigDecimal(ratePerFat.toString()))
                .multiply(BigDecimal(qty.toString()))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()

            currentList[index] = newRow.copy(amount = amount)
            _rows.value = currentList
        }
    }
    fun setInitializedByMembersFalse() {
        _wasInitializedByMembers.value = false
    }

    suspend fun getById(id: Long): ListOfEntry? =
        listEntryDao.getById(id)

    fun setFactoryEntry(entryId: Long, factoryEntry: Entry) {
        viewModelScope.launch {
            listEntryDao.getById(entryId)?.let { group ->
                listEntryDao.update(group.copy(factoryEntry = factoryEntry))
            }
        }
    }

    fun clearFactoryEntryFromGroup(entryGroupId: Long) {
        viewModelScope.launch {
            getById(entryGroupId)?.let { group ->
                listEntryDao.update(group.copy(factoryEntry = null))
            }
        }
    }

    /** Saves all rows as entries, always using fresh ratePerFat from DB */
    fun saveAll() = viewModelScope.launch {
        // fetch latest rate
        val ratePerFat = fatDao.get().first()?.ratePerFat ?: return@launch
        val isNight = LocalTime.now().hour >= 12

        val entries = _rows.value.map { row ->
            val fat = row.fatRate.toDoubleOrNull() ?: 0.0
            val qty = row.milkQty.toDoubleOrNull() ?: 0.0
            val amount = BigDecimal(fat.toString())
                .multiply(BigDecimal(ratePerFat.toString()))
                .multiply(BigDecimal(qty.toString()))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()

            Entry(
                name = row.name,
                fat = fat,
                milkQty = qty,
                amountToPay = amount,
                isNight = isNight
            )
        }

        entries.forEach { entry ->
            try {
                entryDao.insert(entry)
                memberDao.findByName(entry.name)?.let { m ->
                    memberDao.update(m.copy(history = m.history + entry))
                }
            } catch (e: Exception) {
                Log.e("EntryVM", "Insert failed: $entry", e)
            }
        }

        // record group
        try {
            listEntryDao.insert(ListOfEntry(listOfEntry = entries, isNight = isNight))
        } catch (e: Exception) {
            Log.e("EntryVM", "List insert failed", e)
        }

        // clear UI state
        _rows.value = emptyList()
        userPrefs.clearUnsavedRows()
    }

    suspend fun generateReport(group: ListOfEntry, rates: Rates): ProductionReport {
        val collectedMilk = group.listOfEntry.sumOf { it.milkQty }
        val totalPaid = group.listOfEntry.sumOf { it.amountToPay }
        val factoryMilk = group.factoryEntry?.milkQty ?: 0.0
        val factoryFat = group.factoryEntry?.fat ?: 0.0
        val localMilk = collectedMilk - factoryMilk

        val factoryEarned = factoryMilk * factoryFat * rates.sellingFatRate
        val localEarned = localMilk * rates.yourMilkRates
        val finalEarning = factoryEarned + localEarned - totalPaid

        return ProductionReport(
            totalMilkCollected  = collectedMilk,
            totalMilkSent       = factoryMilk,
            localSoldMilk       = localMilk,
            totalPaidToMembers  = totalPaid,
            factoryIncome       = factoryEarned,
            localIncome         = localEarned,
            finalEarning        = finalEarning
        )
    }
}

data class ProductionReport(
    val totalMilkCollected: Double,
    val totalMilkSent: Double,
    val localSoldMilk: Double,
    val totalPaidToMembers: Double,
    val factoryIncome: Double,
    val localIncome: Double,
    val finalEarning: Double
)