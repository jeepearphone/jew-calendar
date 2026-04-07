package com.example.jewcalendar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jewcalendar.ui.calendar.CalendarScreen


@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "mainScreen",
        modifier = modifier
    ) {


        composable("mainScreen") {
            CalendarScreen(

            )
        }


    }
}
