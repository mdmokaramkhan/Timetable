package com.mukrram.timetable.data.repository

import com.mukrram.timetable.data.local.AuthTokenStore
import com.mukrram.timetable.data.local.TimetablePreferences
import com.mukrram.timetable.data.model.AppThemeMode
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.JwtPayloadParser
import com.mukrram.timetable.data.remote.SessionState
import com.mukrram.timetable.data.remote.TimetableApi
import com.mukrram.timetable.data.remote.dto.BatchCreateRequest
import com.mukrram.timetable.data.remote.dto.BatchUpdateRequest
import com.mukrram.timetable.data.remote.dto.FacultyCreateRequest
import com.mukrram.timetable.data.remote.dto.FacultyUpdateRequest
import com.mukrram.timetable.data.remote.dto.LoginRequest
import com.mukrram.timetable.data.remote.dto.RegisterRequest
import com.mukrram.timetable.data.remote.dto.RoomCreateRequest
import com.mukrram.timetable.data.remote.dto.RoomUpdateRequest
import com.mukrram.timetable.data.remote.dto.SubjectCreateRequest
import com.mukrram.timetable.data.remote.dto.SubjectUpdateRequest
import com.mukrram.timetable.data.remote.dto.GenerateTimetableRequest
import com.mukrram.timetable.data.remote.dto.SaveTimetableRequest
import com.mukrram.timetable.data.remote.dto.SavedTimetableResponse
import com.mukrram.timetable.data.remote.dto.FacultyTimetableMeResponse
import com.mukrram.timetable.data.remote.dto.SubstituteRequest
import com.mukrram.timetable.data.remote.dto.SubstituteResponse
import com.mukrram.timetable.data.remote.dto.AnalyticsResponseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimetableRepository(
    private val api: TimetableApi,
    private val preferences: TimetablePreferences,
    private val tokenStore: AuthTokenStore,
    applicationScope: CoroutineScope,
) {

    init {
        applicationScope.launch {
            tokenStore.setToken(preferences.authToken.first())
            preferences.authToken.collect { tokenStore.setToken(it) }
        }
    }

    val sessionState: Flow<SessionState> = preferences.authToken.map { token ->
        if (token.isNullOrBlank()) {
            SessionState.LoggedOut
        } else {
            val role = JwtPayloadParser.parseRole(token) ?: UserRole.Faculty
            val username = JwtPayloadParser.parseSub(token).orEmpty()
            SessionState.LoggedIn(role = role, username = username)
        }
    }

    val lastTimetableSummary: Flow<LastTimetableSummary?> =
        preferences.lastTimetableJson.map { json ->
            val parsed = preferences.parseCachedTimetable(json) ?: return@map null
            LastTimetableSummary(
                batch = parsed.batch,
                updatedAt = parsed.updatedAt,
            )
        }

    val hasCompletedOnboarding: Flow<Boolean> = preferences.hasCompletedOnboarding

    suspend fun setHasCompletedOnboarding(completed: Boolean) =
        withContext(Dispatchers.IO) {
            preferences.setHasCompletedOnboarding(completed)
        }

    suspend fun cacheLastTimetable(response: SavedTimetableResponse) =
        withContext(Dispatchers.IO) {
            preferences.cacheLastTimetable(response)
        }

    fun getCachedTimetable(): Flow<SavedTimetableResponse?> =
        preferences.lastTimetableJson.map { preferences.parseCachedTimetable(it) }

    suspend fun getDashboardCounts(): DashboardCounts = withContext(Dispatchers.IO) {
        val faculty = async { api.getFaculty() }
        val subjects = async { api.getSubjects() }
        val rooms = async { api.getRooms() }
        val batches = async { api.getBatches() }
        DashboardCounts(
            facultyCount = faculty.await().size,
            subjectsCount = subjects.await().size,
            roomsCount = rooms.await().size,
            batchesCount = batches.await().size,
        )
    }

    suspend fun getFaculty() = withContext(Dispatchers.IO) { api.getFaculty() }

    suspend fun createFaculty(body: FacultyCreateRequest) =
        withContext(Dispatchers.IO) { api.createFaculty(body) }

    suspend fun updateFaculty(id: String, body: FacultyUpdateRequest) =
        withContext(Dispatchers.IO) { api.updateFaculty(id, body) }

    suspend fun deleteFaculty(id: String) =
        withContext(Dispatchers.IO) { api.deleteFaculty(id) }

    suspend fun getSubjects() = withContext(Dispatchers.IO) { api.getSubjects() }

    suspend fun createSubject(body: SubjectCreateRequest) =
        withContext(Dispatchers.IO) { api.createSubject(body) }

    suspend fun updateSubject(id: String, body: SubjectUpdateRequest) =
        withContext(Dispatchers.IO) { api.updateSubject(id, body) }

    suspend fun deleteSubject(id: String) =
        withContext(Dispatchers.IO) { api.deleteSubject(id) }

    suspend fun getRooms() = withContext(Dispatchers.IO) { api.getRooms() }

    suspend fun createRoom(body: RoomCreateRequest) =
        withContext(Dispatchers.IO) { api.createRoom(body) }

    suspend fun updateRoom(id: String, body: RoomUpdateRequest) =
        withContext(Dispatchers.IO) { api.updateRoom(id, body) }

    suspend fun deleteRoom(id: String) =
        withContext(Dispatchers.IO) { api.deleteRoom(id) }

    suspend fun getBatches() = withContext(Dispatchers.IO) { api.getBatches() }

    suspend fun createBatch(body: BatchCreateRequest) =
        withContext(Dispatchers.IO) { api.createBatch(body) }

    suspend fun updateBatch(id: String, body: BatchUpdateRequest) =
        withContext(Dispatchers.IO) { api.updateBatch(id, body) }

    suspend fun deleteBatch(id: String) =
        withContext(Dispatchers.IO) { api.deleteBatch(id) }

    suspend fun generateTimetable(body: GenerateTimetableRequest) =
        withContext(Dispatchers.IO) { api.generateTimetable(body) }

    suspend fun saveTimetable(body: SaveTimetableRequest) =
        withContext(Dispatchers.IO) { api.saveTimetable(body) }

    suspend fun getTimetable(batchName: String) =
        withContext(Dispatchers.IO) { api.getTimetable(batchName) }

    suspend fun getAnalytics(): AnalyticsResponseDto =
        withContext(Dispatchers.IO) { api.getAnalytics() }

    suspend fun getFacultyTimetableMe(): FacultyTimetableMeResponse =
        withContext(Dispatchers.IO) { api.getFacultyTimetableMe() }

    suspend fun substitute(body: SubstituteRequest): SubstituteResponse =
        withContext(Dispatchers.IO) { api.substitute(body) }

    suspend fun login(username: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.login(LoginRequest(username = username, password = password))
                tokenStore.setToken(response.token)
                preferences.setAuthToken(response.token)
            }
        }

    suspend fun register(
        username: String,
        password: String,
        facultyId: String?,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.register(
                    RegisterRequest(
                        username = username,
                        password = password,
                        facultyId = facultyId?.trim()?.takeIf { it.isNotEmpty() },
                    ),
                )
                tokenStore.setToken(response.token)
                preferences.setAuthToken(response.token)
            }
        }

    /**
     * Confirms the token with the server and can refresh claims if the backend adds more later.
     */
    suspend fun refreshMe(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                api.getMe()
                Unit
            }
        }

    suspend fun logout() =
        withContext(Dispatchers.IO) {
            tokenStore.setToken(null)
            preferences.setAuthToken(null)
        }

    val themePreference: Flow<AppThemeMode> = preferences.themePreference

    suspend fun setThemeMode(mode: AppThemeMode) =
        withContext(Dispatchers.IO) {
            preferences.setThemeMode(mode)
        }

    suspend fun clearTimetableCache() =
        withContext(Dispatchers.IO) {
            preferences.clearCachedTimetable()
        }

    val displayName: Flow<String> = preferences.displayName
}

data class LastTimetableSummary(
    val batch: String,
    val updatedAt: String?,
)

data class DashboardCounts(
    val facultyCount: Int,
    val subjectsCount: Int,
    val roomsCount: Int,
    val batchesCount: Int,
)
