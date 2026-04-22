package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mukrram.timetable.data.repository.TimetableRepository

class AppViewModelFactory(
    private val repository: TimetableRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(FacultyManageViewModel::class.java) ->
                FacultyManageViewModel(repository) as T
            modelClass.isAssignableFrom(SubjectManageViewModel::class.java) ->
                SubjectManageViewModel(repository) as T
            modelClass.isAssignableFrom(RoomManageViewModel::class.java) ->
                RoomManageViewModel(repository) as T
            modelClass.isAssignableFrom(BatchManageViewModel::class.java) ->
                BatchManageViewModel(repository) as T
            modelClass.isAssignableFrom(GenerateViewModel::class.java) ->
                GenerateViewModel(repository) as T
            modelClass.isAssignableFrom(TimetableViewerViewModel::class.java) ->
                TimetableViewerViewModel(repository) as T
            modelClass.isAssignableFrom(SubstitutionViewModel::class.java) ->
                SubstitutionViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(repository) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repository) as T
            modelClass.isAssignableFrom(AnalyticsViewModel::class.java) ->
                AnalyticsViewModel(repository) as T
            modelClass.isAssignableFrom(ExportViewModel::class.java) ->
                ExportViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
