package com.example.model

// TODO finish or delete

data class RegisterRequest(
    val email: String
)

data class VerifyEmailRequest(val token: String)
