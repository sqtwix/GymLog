package com.example.gymlogapp.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymlogapp.R
import com.example.gymlogapp.repository.AuthRepository
import com.example.gymlogapp.repository.WorkoutRepository

private enum class HomeTab(val route: String) {
    Calendar("calendar"),
    Analysis("analysis")
}

@Composable
fun HomeScreen(
    appNavController: NavHostController,
    authRepository: AuthRepository,
    workoutRepository: WorkoutRepository
) {
    val tabNavController = rememberNavController()
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == HomeTab.Calendar.route,
                    onClick = { tabNavController.navigateToTab(HomeTab.Calendar.route) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.tab_calendar)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_calendar)) }
                )
                NavigationBarItem(
                    selected = currentRoute == HomeTab.Analysis.route,
                    onClick = { tabNavController.navigateToTab(HomeTab.Analysis.route) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.tab_analysis)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_analysis)) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = HomeTab.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeTab.Calendar.route) {
                MainScreen(appNavController, authRepository, workoutRepository)
            }
            composable(HomeTab.Analysis.route) {
                AnalysisScreen()
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(HomeTab.Calendar.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
