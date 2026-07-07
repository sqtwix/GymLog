package com.example.gymlogapp.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlogapp.data.ServiceLocator
import com.example.gymlogapp.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed interface WorkoutUiState {
    object Idle : WorkoutUiState
    object Loading : WorkoutUiState
    object Success : WorkoutUiState
    data class Error(val message: String) : WorkoutUiState
}

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val authManager = ServiceLocator.getAuthManager(application)

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Idle)
    val uiState: StateFlow<WorkoutUiState> = _uiState

    // Selected date in the calendar
    val selectedDate = MutableStateFlow(LocalDate.now())

    // Fetched data lists
    private val _workouts = MutableStateFlow<List<WorkoutResponse>>(emptyList())
    val workouts: StateFlow<List<WorkoutResponse>> = _workouts

    val types = MutableStateFlow<List<WorkoutType>>(emptyList())
    val locations = MutableStateFlow<List<WorkoutLocation>>(emptyList())
    val bodyParts = MutableStateFlow<List<BodyPart>>(emptyList())

    init {
        loadMetadata()
        loadWorkouts()
    }

    fun loadMetadata() {
        viewModelScope.launch {
            val apiService = ServiceLocator.getApiService(getApplication())
            
            // 1. Fetch Types
            try {
                types.value = apiService.getAllWorkoutTypes()
            } catch (e: Exception) {
                // Fallback
                types.value = listOf(
                    WorkoutType(1, "Strength"),
                    WorkoutType(2, "Stretching"),
                    WorkoutType(3, "Jogging"),
                    WorkoutType(4, "Fitness"),
                    WorkoutType(5, "Yoga"),
                    WorkoutType(6, "Cardio")
                )
            }

            // 2. Fetch Locations
            try {
                locations.value = apiService.getAllLocations()
            } catch (e: Exception) {
                // Fallback
                locations.value = listOf(
                    WorkoutLocation(1, "Home"),
                    WorkoutLocation(2, "Gym"),
                    WorkoutLocation(3, "Outside")
                )
            }

            // 3. Fetch Body Parts
            try {
                bodyParts.value = apiService.getAllBodyParts()
            } catch (e: Exception) {
                // Fallback
                bodyParts.value = listOf(
                    BodyPart(1, "Chest"),
                    BodyPart(2, "Legs"),
                    BodyPart(3, "Biceps"),
                    BodyPart(4, "Forearm"),
                    BodyPart(5, "Triceps"),
                    BodyPart(6, "Abs"),
                    BodyPart(7, "Back")
                )
            }
        }
    }

    fun loadWorkouts() {
        _uiState.value = WorkoutUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ServiceLocator.getApiService(getApplication())
                val list = apiService.getAllWorkouts()
                _workouts.value = list
                _uiState.value = WorkoutUiState.Success
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.localizedMessage ?: "Failed to fetch workouts")
            }
        }
    }

    fun createWorkout(
        typeId: Long,
        locationId: Long?,
        description: String,
        dateTime: LocalDateTime,
        duration: Int,
        bodyPartIds: List<Long>?
    ) {
        _uiState.value = WorkoutUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ServiceLocator.getApiService(getApplication())
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val dateStr = dateTime.format(formatter)

                val request = CreateWorkoutRequest(
                    type_id = typeId,
                    location_id = locationId,
                    description = description,
                    date = dateStr,
                    durationInMinutes = duration,
                    bodyPartIds = bodyPartIds
                )
                apiService.createWorkout(request)
                loadWorkouts()
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.localizedMessage ?: "Failed to create workout")
            }
        }
    }

    fun updateWorkout(
        workoutId: Long,
        typeId: Long,
        locationId: Long?,
        description: String,
        dateTime: LocalDateTime,
        duration: Int,
        bodyPartIds: List<Long>?
    ) {
        _uiState.value = WorkoutUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ServiceLocator.getApiService(getApplication())
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val dateStr = dateTime.format(formatter)

                val request = UpdateWorkoutRequest(
                    type_id = typeId,
                    description = description,
                    location_id = locationId,
                    date = dateStr,
                    durationInMinutes = duration,
                    bodyPartIds = bodyPartIds
                )
                apiService.updateWorkout(workoutId, request)
                loadWorkouts()
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.localizedMessage ?: "Failed to update workout")
            }
        }
    }

    fun deleteWorkout(workoutId: Long) {
        _uiState.value = WorkoutUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ServiceLocator.getApiService(getApplication())
                apiService.deleteWorkout(workoutId)
                loadWorkouts()
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.localizedMessage ?: "Failed to delete workout")
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        authManager.clearToken()
        onLogoutSuccess()
    }
}
