// File: com.jlss.mukeshdairy.viewmodel.EntryListViewModel.kt
package com.jlss.smartDairy.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.ListOfEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.*

enum class FilterType { ALL, NIGHT, MORNING }
class EntryListViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).listEntryDao()

    // 1) Backing state flow, initially empty
    private val _allEntries = MutableStateFlow<List<ListOfEntry>>(emptyList())
    // 2) Exposed read-only view
    val allEntries: StateFlow<List<ListOfEntry>> = _allEntries

    init {
        // 3) Collect the DAO flow and feed our state flow
        viewModelScope.launch {
            dao.getAll().collect { list ->
                _allEntries.value = list
            }
        }
    }
    // search/filter inputs
    val searchQuery = MutableStateFlow("")
    val filterType  = MutableStateFlow(FilterType.ALL)

    // combined, filtered list
    val filteredEntries: StateFlow<List<ListOfEntry>> =
        combine(_allEntries, searchQuery, filterType) { list, query, filter ->
            list.filter { item ->
                // 1) filter by night/morning
                val shiftOk = when(filter) {
                    FilterType.ALL      -> true
                    FilterType.NIGHT    -> item.isNight
                    FilterType.MORNING  -> !item.isNight
                }
                // 2) search by formatted date string
                val formatted = android.text.format.DateFormat.format(
                    "d MMM yyyy", item.timestamp
                ).toString().lowercase()
                val searchOk = query.isBlank() || formatted.contains(query.lowercase())
                shiftOk && searchOk
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            dao.getAll().collect { _allEntries.value = it }
        }
    }

    fun setSearch(q: String) {
        searchQuery.value = q
    }

    fun setFilter(f: FilterType) {
        filterType.value = f
    }

    fun deleteEntry(entry: ListOfEntry) = viewModelScope.launch {
        dao.delete(entry)
    }



}

class SharedViewModel : ViewModel() {
    var selectedEntries by mutableStateOf<List<Entry>>(emptyList())
}