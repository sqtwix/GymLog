package com.example.gymlogapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlogapp.api.RetrofitClient
import com.example.gymlogapp.model.CreateTrainRequest
import com.example.gymlogapp.model.TrainResponse
import com.example.gymlogapp.utils.Preferences
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {

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
    LaunchedEffect(Unit) {
        try {
            trains = RetrofitClient.api.getTrains()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    val username = "Иван"

    Column(modifier = Modifier.fillMaxSize()) {

        // Верхняя панель
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Привет, $username!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = {
                        Preferences.clearToken(context)
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                ) {
                    Text("Выйти")
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
                showModeToggle = false
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
            Text("Открыть тренировку")
        }
    }

    // Диалог
    if (showDialog && selectedDateStr != null) {

        val dateStr = selectedDateStr!!

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Тренировка на $dateStr") },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Закрыть")
                }
            },
            text = {
                if (currentTrain != null) {
                    Column {
                        Text("Тип: ${currentTrain!!.type}")
                        Text("Длительность: ${currentTrain!!.duration} мин")

                        Spacer(Modifier.height(16.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                scope.launch {
                                    try {
                                        RetrofitClient.api.deleteTrain(dateStr)
                                        trains = RetrofitClient.api.getTrains()
                                        Toast.makeText(context, "Удалено", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                    } finally {
                                        showDialog = false
                                    }
                                }
                            }
                        ) {
                            Text("Удалить")
                        }
                    }
                } else {

                    var type by remember { mutableStateOf("") }
                    var durationStr by remember { mutableStateOf("") }
                    var description by remember { mutableStateOf("") }

                    Column {
                        OutlinedTextField(
                            value = type,
                            onValueChange = { type = it },
                            label = { Text("Тип тренировки") }
                        )

                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Длительность (мин)") }
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Описание") }
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val duration = durationStr.toIntOrNull() ?: 0
                                if (type.isBlank() || duration <= 0) {
                                    Toast.makeText(context, "Ошибка ввода", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                scope.launch {
                                    try {
                                        RetrofitClient.api.createTrain(
                                            CreateTrainRequest(
                                                type = type,
                                                description = description,
                                                date = dateStr,
                                                duration = duration,
                                                userId = 0
                                            )
                                        )
                                        trains = RetrofitClient.api.getTrains()
                                        Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                    } finally {
                                        showDialog = false
                                    }
                                }
                            }
                        ) {
                            Text("Сохранить")
                        }
                    }
                }
            }
        )
    }
}
