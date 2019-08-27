package com.nesstech.loginauthmvvm.interfaces

import com.nesstech.loginauthmvvm.model.AuthResponse
import io.reactivex.Observable
import retrofit2.http.*

interface BackEndApi {
    /*@POST("login")
    @Headers("Content-Type: application/json")
    fun loginAuth(@Body login: String): Response<AuthResponse>*///retro generic response

    @FormUrlEncoded
    @POST("login")
     fun loginAuth(
        @Field("email") email: String,
        @Field("password") password: String
    ) : Observable<AuthResponse>//rx generic response

    @FormUrlEncoded
    @POST("signup")
     fun signUpAuth(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ) : Observable<AuthResponse>
}