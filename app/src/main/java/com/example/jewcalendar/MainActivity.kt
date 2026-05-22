package com.example.jewcalendar

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.jewcalendar.data.LocationProvider
import com.example.jewcalendar.navigation.AppNavigation
import com.example.jewcalendar.navigation.Screen
import com.example.jewcalendar.notifications.NotificationScheduler
import com.example.jewcalendar.ui.theme.JewCalendarTheme
import kotlinx.coroutines.launch

data class NavItem(val screen: Screen, val label: String, val icon: ImageVector)

val NAV_ITEMS = listOf(
    NavItem(Screen.Calendar, "Календарь", Icons.Default.DateRange),
    NavItem(Screen.KosherCheck, "Кошерность", Icons.Default.ShoppingCart),
    NavItem(Screen.Settings, "Настройки",  Icons.Default.Settings)
)

class MainActivity : ComponentActivity() {
    private lateinit var appViewModel: AppViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchLocation()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
        fetchLocation()
        setContent {
            JewCalendarTheme {
                MainScaffold(appViewModel)
            }
        }
    }
    private fun fetchLocation() {
        lifecycleScope.launch {
            val location = LocationProvider.getCurrentLocation(this@MainActivity)
            if (location != null) {
                appViewModel.onLocationReceived(location.latitude, location.longitude)
                NotificationScheduler.scheduleAll(
                    context = this@MainActivity,
                    lat = location.latitude,
                    lon = location.longitude
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    appViewModel : AppViewModel,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute != null && !currentRoute.startsWith("day_details")
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "לוּחַ",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NAV_ITEMS.forEach { item ->
                    NavigationDrawerItem(
                        icon     = { Icon(item.icon, null) },
                        label    = { Text(item.label) },
                        selected = currentRoute == item.screen.route,
                        onClick  = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("לוּחַ") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Меню")
                        }
                    }
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter   = slideInVertically { it },
                    exit    = slideOutVertically { it }
                ) {
                    NavigationBar {
                        NAV_ITEMS.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.screen.route,
                                onClick  = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                },
                                icon  = { Icon(item.icon, item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavigation(navController, innerPadding, appViewModel)
        }
    }
}
