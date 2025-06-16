package com.jlss.smartDairy.viewmodel

// File: com.jlss.mukeshdairy.viewmodel.MemberViewModel.kt


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.Members
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// imports at the top (ensure you have)
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class MemberViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).memberDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val allMembers: StateFlow<List<Members>> =
        dao.all().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Public filtered list exposed to UI
    val members: StateFlow<List<Members>> = combine(allMembers, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setSearch(query: String) {
        _searchQuery.value = query
    }

    private val _selected = MutableStateFlow<Members?>(null)
    val selected: StateFlow<Members?> = _selected

    fun findById(id: Long) = viewModelScope.launch {
        _selected.value = dao.findById(id)
    }

    fun add(name: String) = viewModelScope.launch {
        dao.insert(Members(name = name))
    }

    fun remove(m: Members) = viewModelScope.launch {
        dao.delete(m)
    }

    fun clearHistory(memberId: Long) = viewModelScope.launch {
        dao.clearHistory(memberId)
        _selected.value = dao.findById(memberId)
    }
}
