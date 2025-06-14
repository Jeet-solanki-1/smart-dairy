package com.jlss.smartDairy.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).userDao()

    val user: Flow<User?> = flow {
        emit(dao.getUser().firstOrNull())
    }


    fun saveUser(name: String, mobile: String, village: String) = viewModelScope.launch {
        val existingUser = dao.getUser().firstOrNull()
        if (existingUser != null) {
            dao.updateUser(existingUser.copy(name = name, mobile = mobile, village = village))
        } else {
            dao.insertUser(User(name = name, mobile = mobile, village = village))
        }
    }

}
