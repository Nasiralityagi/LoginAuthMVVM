package com.nesstech.loginauthmvvm.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nesstech.loginauthmvvm.repository.LoginRepository

class ViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            LoginViewModel(LoginRepository.getInstance()) as T
        } else {
            //TODO for others view model repository
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
