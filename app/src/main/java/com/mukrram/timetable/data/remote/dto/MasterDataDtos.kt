package com.mukrram.timetable.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FacultyDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val subjects: List<String>? = emptyList(),
    val maxLoad: Int,
)

data class FacultyCreateRequest(
    val name: String,
    val subjects: List<String>,
    val maxLoad: Int,
)

data class FacultyUpdateRequest(
    val name: String? = null,
    val subjects: List<String>? = null,
    val maxLoad: Int? = null,
)

data class SubjectDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val lecturesPerWeek: Int,
)

data class SubjectCreateRequest(
    val name: String,
    val lecturesPerWeek: Int,
)

data class SubjectUpdateRequest(
    val name: String? = null,
    val lecturesPerWeek: Int? = null,
)

data class RoomDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val type: String,
)

data class RoomCreateRequest(
    val name: String,
    val type: String,
)

data class RoomUpdateRequest(
    val name: String? = null,
    val type: String? = null,
)

data class BatchDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val department: String,
)

data class BatchCreateRequest(
    val name: String,
    val department: String,
)

data class BatchUpdateRequest(
    val name: String? = null,
    val department: String? = null,
)

data class DeleteOkResponse(
    val ok: Boolean = false,
)
