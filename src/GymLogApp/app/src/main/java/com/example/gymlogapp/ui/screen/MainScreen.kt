package com.example.gymlogapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlogapp.R
import com.example.gymlogapp.repository.AuthRepository
import com.example.gymlogapp.repository.WorkoutRepository
import com.example.gymlogapp.model.CreateTrainRequest
import com.example.gymlogapp.model.TrainResponse
import com.example.gymlogapp.utils.getErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authRepository: AuthRepository,
    workoutRepository: WorkoutRepository
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var trains by remember { mutableStateOf<List<TrainResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedDateStr by remember { mutableStateOf<String?>(null) }
    var currentTrain by remember { mutableStateOf<TrainResponse?>(null) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val datePickerState = rememberDatePickerState()

    // Загрузка тренировок
    LaunchedEffect(workoutRepository) {
        try {
            trains = workoutRepository.getWorkouts()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Toast.makeText(context, getErrorMessage(context, e), Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    val username = stringResource(R.string.placeholder_user_name)

    Column(modifier = Modifier.fillMaxSize()) {

        // Верхняя панель
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.greeting, username),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = {
                        authRepository.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_logout))
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Card(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {
                val millis = datePickerState.selectedDateMillis ?: return@Button
                val date = Date(millis)
                val dateStr = dateFormat.format(date)

                selectedDateStr = dateStr
                currentTrain = trains.find { it.date.startsWith(dateStr) }
                showDialog = true
            }
        ) {
            Text(stringResource(R.string.action_open_workout))
        }
    }

    // Диалог
    if (showDialog && selectedDateStr != null) {

        val dateStr = selectedDateStr!!

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.workout_date_title, dateStr)) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            },
            text = {
                if (currentTrain != null) {
                    Column {
                        Text(stringResource(R.string.workout_type_detail, currentTrain!!.type))
                        Text(stringResource(R.string.workout_duration_detail, currentTrain!!.durationInMinutes))
                    }
                } else {

                    var typeId by remember { mutableStateOf("") }
                    var durationStr by remember { mutableStateOf("") }
                    var description by remember { mutableStateOf("") }

                    Column {
                        OutlinedTextField(
                            value = typeId,
                            onValueChange = { typeId = it.filter(Char::isDigit) },
                            label = { Text(stringResource(R.string.workout_type_label)) }
                        )

                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text(stringResource(R.string.workout_duration_label)) }
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.workout_description_label)) }
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val workoutTypeId = typeId.toLongOrNull()
                                val duration = durationStr.toIntOrNull() ?: 0
                                if (workoutTypeId == null || workoutTypeId <= 0 || duration <= 0) {
                                    Toast.makeText(context, R.string.workout_validation_error, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                scope.launch {
                                    try {
                                        workoutRepository.createWorkout(
                                            CreateTrainRequest(
                                                type_id = workoutTypeId,
                                                description = description,
                                                date = "${dateStr}T00:00:00",
                                                durationInMinutes = duration
                                            )
                                        )
                                        trains = workoutRepository.getWorkouts()
                                        Toast.makeText(context, R.string.workout_saved, Toast.LENGTH_SHORT).show()
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        Toast.makeText(context, getErrorMessage(context, e), Toast.LENGTH_LONG).show()
                                    } finally {
                                        showDialog = false
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
            }
        )
    }
}
