package com.example.gymlogapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlogapp.api.RetrofitClient
import com.example.gymlogapp.model.TrainResponse
import com.example.gymlogapp.utils.Preferences
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.gymlogapp.model.*;
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.CalendarView as AndroidCalendarView

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var trains by remember { mutableStateOf<List<TrainResponse>>(emptyList()) }
    var selectedDateStr by remember { mutableStateOf<String?>(null) }  // "2026-01-13"
    var showDialog by remember { mutableStateOf(false) }
    var currentTrain by remember { mutableStateOf<TrainResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Загрузка тренировок
    LaunchedEffect(Unit) {
        try {
            trains = RetrofitClient.api.getTrains()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    val username = "Иван" // ← позже замени на реальное из токена или prefs

    Column(modifier = Modifier.fillMaxSize()) {
        // Верхняя панель
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Привет, $username!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            Button(onClick = {
                Preferences.clearToken(context)
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
            }) {
                Text("Выйти")
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Простой календарь
            AndroidView(
                factory = { ctx ->
                    AndroidCalendarView(ctx).apply {
                        setOnDateChangeListener { _, year, month, dayOfMonth ->
                            // Формируем строку "yyyy-MM-dd"
                            val selected = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            selectedDateStr = selected

                            // Ищем тренировку на эту дату (сравниваем только дату, без времени)
                            currentTrain = trains.find { train ->
                                train.date.startsWith(selected)
                            }

                            showDialog = true
                        }

                        // Здесь можно позже добавить цвета для дней с тренировками
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            )
        }
    }

    // Диалог при выборе дня
    if (showDialog && selectedDateStr != null) {
        val dateStr = selectedDateStr!!

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Тренировка на $dateStr") },
            text = {
                if (currentTrain != null) {
                    // Просмотр существующей тренировки
                    Column {
                        Text("Тип: ${currentTrain!!.type}")
                        Text("Длительность: ${currentTrain!!.duration} мин")
                        if (!currentTrain!!.description.isNullOrBlank()) {
                            Text("Описание: ${currentTrain!!.description}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.api.deleteTrain(dateStr)
                                        trains = RetrofitClient.api.getTrains() // обновляем список
                                        Toast.makeText(context, "Тренировка удалена", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        showDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Удалить")
                        }
                    }
                } else {
                    // Форма создания новой тренировки
                    var type by remember { mutableStateOf("") }
                    var description by remember { mutableStateOf("") }
                    var durationStr by remember { mutableStateOf("") }
                    var selectedCategory by remember { mutableStateOf("") }

                    val types = listOf("Тренажерный зал", "Бег", "Йога", "Бассейн", "Боевые искусства", "Велоспорт", "Другое")
                    val categories = listOf("Грудь", "Плечи", "Бицепс", "Трицепс", "Пресс", "Ноги", "Спина", "Ягодицы", "Другое")

                    Column {
                        // Выбор типа тренировки (пока простой текст, позже можно Dropdown)
                        OutlinedTextField(
                            value = type,
                            onValueChange = { type = it },
                            label = { Text("Тип тренировки") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (type == "Тренажерный зал") {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = { selectedCategory = it },
                                label = { Text("Группа мышц") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Описание (опционально)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Длительность (минуты)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            val duration = durationStr.toIntOrNull() ?: 0
                            if (duration < 1 || duration > 1440) {
                                Toast.makeText(context, "Длительность от 1 до 1440 мин", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (type.isBlank()) {
                                Toast.makeText(context, "Выберите тип", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            val finalDesc = if (type == "Тренажерный зал") selectedCategory else description

                            coroutineScope.launch {
                                try {
                                    val dto = CreateTrainRequest(
                                        type = type,
                                        description = finalDesc,
                                        date = "$dateStr" + "T00:00:00", // сервер ожидает полный ISO или хотя бы дату
                                        duration = duration,
                                        userId = 0 // сервер берёт из токена
                                    )
                                    RetrofitClient.api.createTrain(dto)
                                    trains = RetrofitClient.api.getTrains()
                                    Toast.makeText(context, "Тренировка добавлена!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    showDialog = false
                                }
                            }
                        }) {
                            Text("Сохранить")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}