package com.mukrram.timetable.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String,
)

/** Matches POST /auth/register — optional [facultyId] links a faculty user to a Faculty document. */
data class RegisterRequest(
    val username: String,
    val password: String,
    val facultyId: String? = null,
)

data class LoginResponse(
    val token: String,
    @SerializedName("tokenType") val tokenType: String? = null,
    val expiresIn: String? = null,
    val role: String? = null,
    val username: String? = null,
)

data class MeResponse(
    val sub: String? = null,
    val role: String? = null,
)
