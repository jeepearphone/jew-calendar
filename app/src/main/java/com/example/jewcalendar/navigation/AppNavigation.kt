package com.example.jewcalendar.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jewcalendar.ui.*
import com.example.jewcalendar.ui.calendar.CalendarScreen

sealed class Screen(val route: String) {
    object Calendar    : Screen("calendar")
    object Settings    : Screen("settings")
    object DayDetails  : Screen("day_details/{eventId}") {
        fun route(eventId: String) = "day_details/$eventId"
        const val ARG = "eventId"
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Calendar.route,
        modifier         = Modifier.padding(innerPadding),
    ) {
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onDayClick    = {  },
                onHolidayClick = { id ->
                    navController.navigate(Screen.DayDetails.route(id))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onHolidayClick = { id ->
                    navController.navigate(Screen.DayDetails.route(id))
                }
            )
        }
        composable(
            route = Screen.DayDetails.route,
            arguments = listOf(navArgument(Screen.DayDetails.ARG) {
                type = NavType.StringType
            })
        ) { back ->
            val eventId = back.arguments?.getString(Screen.DayDetails.ARG) ?: ""
            DayDetailsScreen(
                eventId = eventId,
                onBack  = { navController.popBackStack() }
            )
        }
    }
}