package com.example.gymlogapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymlogapp.api.RetrofitClient
import com.example.gymlogapp.ui.screen.LoginScreen
import com.example.gymlogapp.ui.screen.RegisterScreen
import com.example.gymlogapp.ui.screen.MainScreen
import com.example.gymlogapp.ui.theme.GymLogAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        RetrofitClient.init(this)

        setContent {
            GymLogAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    GymLogApp()
                }
            }
        }
    }
}

@Composable
fun GymLogApp() {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.safeDrawingPadding(),
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
