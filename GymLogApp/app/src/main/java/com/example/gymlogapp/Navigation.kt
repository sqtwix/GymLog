package com.example.gymlogapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gymlogapp.data.ServiceLocator
import com.example.gymlogapp.ui.auth.LoginScreen
import com.example.gymlogapp.ui.auth.RegisterScreen
import com.example.gymlogapp.ui.workout.CalendarScreen

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val authManager = remember { ServiceLocator.getAuthManager(context) }
    
    // Auto-login check
    val startDestination = if (authManager.hasToken()) Calendar else Login
    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        backStack.add(Calendar)
                    },
                    onNavigateToRegister = {
                        backStack.add(Register)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Registration success auto-logins or redirects to login
                        backStack.removeLastOrNull()
                    },
                    onNavigateToLogin = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<Calendar> {
                CalendarScreen(
                    onLogout = {
                        // Clear token and pop back to Login
                        backStack.removeLastOrNull()
                        backStack.add(Login)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
    )
}
