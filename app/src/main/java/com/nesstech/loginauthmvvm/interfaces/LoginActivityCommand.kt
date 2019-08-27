package com.nesstech.loginauthmvvm.interfaces

import com.nesstech.loginauthmvvm.model.AuthResponse

interface LoginActivityCommand {
    fun showToast(message: String)
    fun loginData(data: AuthResponse)
    fun pgVisibility(visibility: Int)
    fun setRememberMe(isRemember: Boolean)
    fun setError(isError: Boolean,inputType: String)
    fun loginFB()
    fun loginGoogle()
}