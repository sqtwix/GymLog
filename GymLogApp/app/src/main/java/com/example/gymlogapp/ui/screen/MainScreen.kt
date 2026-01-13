package com.example.gymlogapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlogapp.utils.Preferences
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Главный экран (пока заглушка)",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Ты успешно вошёл! Токен сохранён.")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            Preferences.clearToken(context)
            navController.navigate("login") {
                popUpTo("main") { inclusive = true }
            }
        }) {
            Text("Выйти")
        }
    }
}