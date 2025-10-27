package com.example.test.network

import retrofit2.http.Body
import retrofit2.http.POST

// 서버와 통신할 로그인 / 회원가입 API
interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse
}

// 🔹 데이터 클래스들
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String)

data class SignupRequest(val email: String, val password: String, val nickname: String)
data class SignupResponse(val success: Boolean, val message: String)
