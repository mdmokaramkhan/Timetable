package com.mukrram.timetable.data.remote

import com.mukrram.timetable.data.remote.dto.BatchCreateRequest
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.remote.dto.BatchUpdateRequest
import com.mukrram.timetable.data.remote.dto.DeleteOkResponse
import com.mukrram.timetable.data.remote.dto.FacultyCreateRequest
import com.mukrram.timetable.data.remote.dto.FacultyDto
import com.mukrram.timetable.data.remote.dto.FacultyUpdateRequest
import com.mukrram.timetable.data.remote.dto.RoomCreateRequest
import com.mukrram.timetable.data.remote.dto.RoomDto
import com.mukrram.timetable.data.remote.dto.RoomUpdateRequest
import com.mukrram.timetable.data.remote.dto.SubjectCreateRequest
import com.mukrram.timetable.data.remote.dto.SubjectDto
import com.mukrram.timetable.data.remote.dto.SubjectUpdateRequest
import com.mukrram.timetable.data.remote.dto.GenerateTimetableRequest
import com.mukrram.timetable.data.remote.dto.GenerateTimetableResponse
import com.mukrram.timetable.data.remote.dto.SaveTimetableRequest
import com.mukrram.timetable.data.remote.dto.SaveTimetableResponse
import com.mukrram.timetable.data.remote.dto.SavedTimetableResponse
import com.mukrram.timetable.data.remote.dto.FacultyTimetableMeResponse
import com.mukrram.timetable.data.remote.dto.LoginRequest
import com.mukrram.timetable.data.remote.dto.LoginResponse
import com.mukrram.timetable.data.remote.dto.RegisterRequest
import com.mukrram.timetable.data.remote.dto.MeResponse
import com.mukrram.timetable.data.remote.dto.SubstituteRequest
import com.mukrram.timetable.data.remote.dto.SubstituteResponse
import com.mukrram.timetable.data.remote.dto.AnalyticsResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TimetableApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): LoginResponse

    @GET("auth/me")
    suspend fun getMe(): MeResponse

    @GET("analytics")
    suspend fun getAnalytics(): AnalyticsResponseDto

    @GET("faculty")
    suspend fun getFaculty(): List<FacultyDto>

    @POST("faculty")
    suspend fun createFaculty(@Body body: FacultyCreateRequest): FacultyDto

    @PUT("faculty/{id}")
    suspend fun updateFaculty(
        @Path("id") id: String,
        @Body body: FacultyUpdateRequest,
    ): FacultyDto

    @DELETE("faculty/{id}")
    suspend fun deleteFaculty(@Path("id") id: String): DeleteOkResponse

    @GET("subjects")
    suspend fun getSubjects(): List<SubjectDto>

    @POST("subjects")
    suspend fun createSubject(@Body body: SubjectCreateRequest): SubjectDto

    @PUT("subjects/{id}")
    suspend fun updateSubject(
        @Path("id") id: String,
        @Body body: SubjectUpdateRequest,
    ): SubjectDto

    @DELETE("subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: String): DeleteOkResponse

    @GET("rooms")
    suspend fun getRooms(): List<RoomDto>

    @POST("rooms")
    suspend fun createRoom(@Body body: RoomCreateRequest): RoomDto

    @PUT("rooms/{id}")
    suspend fun updateRoom(
        @Path("id") id: String,
        @Body body: RoomUpdateRequest,
    ): RoomDto

    @DELETE("rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: String): DeleteOkResponse

    @GET("batches")
    suspend fun getBatches(): List<BatchDto>

    @POST("batches")
    suspend fun createBatch(@Body body: BatchCreateRequest): BatchDto

    @PUT("batches/{id}")
    suspend fun updateBatch(
        @Path("id") id: String,
        @Body body: BatchUpdateRequest,
    ): BatchDto

    @DELETE("batches/{id}")
    suspend fun deleteBatch(@Path("id") id: String): DeleteOkResponse

    @POST("timetable/generate")
    suspend fun generateTimetable(@Body body: GenerateTimetableRequest): GenerateTimetableResponse

    @POST("timetable/save")
    suspend fun saveTimetable(@Body body: SaveTimetableRequest): SaveTimetableResponse

    @POST("timetable/substitute")
    suspend fun substitute(@Body body: SubstituteRequest): SubstituteResponse

    @GET("timetable/faculty/me")
    suspend fun getFacultyTimetableMe(): FacultyTimetableMeResponse

    @GET("timetable/{batch}")
    suspend fun getTimetable(@Path(value = "batch", encoded = true) batch: String): SavedTimetableResponse
}
