package com.nesstech.loginauthmvvm.model

data class AuthResponse(
	val isSuccessful: Boolean? = null,
	val message: String? = null,
	val user: User? = null
)
