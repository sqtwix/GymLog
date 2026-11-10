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
import com.example.gymlogapp.repository.AuthRepository
import com.example.gymlogapp.repository.WorkoutRepository
import com.example.gymlogapp.ui.screen.LoginScreen
import com.example.gymlogapp.ui.screen.RegisterScreen
import com.example.gymlogapp.ui.screen.MainScreen
import com.example.gymlogapp.ui.theme.GymLogAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The system displays the starting theme until the first app frame.
        setTheme(R.style.Theme_GymLogApp)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GymLogApplication

        setContent {
            GymLogAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    GymLogApp(app.authRepository, app.workoutRepository)
                }
            }
        }
    }
}

@Composable
fun GymLogApp(authRepository: AuthRepository, workoutRepository: WorkoutRepository) {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.safeDrawingPadding(),
        navController = navController,
        startDestination = "login"  // always starts from login screen
    ) {
        composable("login") {
            LoginScreen(navController, authRepository)
        }

        composable("register") {
            RegisterScreen(navController, authRepository)
        }

        composable("main") {
            MainScreen(navController, authRepository, workoutRepository)
        }
    }
}
