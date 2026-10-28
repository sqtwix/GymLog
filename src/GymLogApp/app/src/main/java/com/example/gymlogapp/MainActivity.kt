package com.example.gymlogapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymlogapp.api.RetrofitClient
import com.example.gymlogapp.ui.screen.LoginScreen
import com.example.gymlogapp.ui.screen.RegisterScreen
import com.example.gymlogapp.ui.screen.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)

        setContent {
            GymLogApp()
        }
    }
}

@Composable
fun GymLogApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"  // always starts from login screen
    ) {
        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("main") {
            MainScreen(navController)
        }
    }
}