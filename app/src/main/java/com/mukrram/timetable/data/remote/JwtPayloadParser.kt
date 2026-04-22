package com.mukrram.timetable.data.remote

import android.util.Base64
import com.mukrram.timetable.data.model.UserRole
import org.json.JSONObject

object JwtPayloadParser {

    fun parseSub(token: String): String? = parsePayloadString(token)?.let { json ->
        runCatching {
            JSONObject(json).optString("sub").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    fun parseRole(token: String): UserRole? = parsePayloadString(token)?.let { json ->
        runCatching {
            when (JSONObject(json).optString("role", "").lowercase()) {
                "admin" -> UserRole.Admin
                "faculty" -> UserRole.Faculty
                else -> null
            }
        }.getOrNull()
    }

    private fun parsePayloadString(token: String): String? {
        val parts = token.split('.')
        if (parts.size < 2) return null
        var payload = parts[1]
        val pad = payload.length % 4
        if (pad != 0) payload += "=".repeat(4 - pad)
        return try {
            val bytes = Base64.decode(payload, Base64.URL_SAFE)
            String(bytes, Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
