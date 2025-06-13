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

class MemberViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).memberDao()

    val members: StateFlow<List<Members>> =
        dao.all().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // load one member by id
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
        // reload
        _selected.value = dao.findById(memberId)
    }
}
