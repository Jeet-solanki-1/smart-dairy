package com.jlss.smartDairy.viewmodel



import android.app.Application
import androidx.lifecycle.*
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.model.User
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    val user = userDao.getUser().asLiveData()

    fun createUser(userName: String,name: String, mobile: String, village: String) {
        viewModelScope.launch {
            userDao.insertUser(User(userName= userName,name = name, mobile = mobile, village = village))

        }
    }


}

