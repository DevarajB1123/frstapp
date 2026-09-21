package com.example.MainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.LostAndFoundViewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.ReportItemScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.UserDashboardScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface Screen {
    data object Home : Screen
    data class ReportItem(val type: String) : Screen
    data object Search : Screen
    data class Detail(val itemId: Long) : Screen
    data object Dashboard : Screen
    data object Admin : Screen
    data object Auth : Screen
}

class MainActivity : ComponentActivity() {
    private val viewModel: LostAndFoundViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: LostAndFoundViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Intercept back button when not on Home screen
    BackHandler(enabled = currentScreen !is Screen.Home) {
        currentScreen = Screen.Home
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    viewModel = viewModel,
                    onReportLost = { currentScreen = Screen.ReportItem("LOST") },
                    onReportFound = { currentScreen = Screen.ReportItem("FOUND") },
                    onSearchClick = { currentScreen = Screen.Search },
                    onItemClick = { itemId -> currentScreen = Screen.Detail(itemId) },
                    onOpenDashboard = {
                        if (currentUser == null) {
                            currentScreen = Screen.Auth
                        } else {
                            currentScreen = Screen.Dashboard
                        }
                    },
                    onOpenAdmin = { currentScreen = Screen.Admin },
                    onOpenAuth = { currentScreen = Screen.Auth }
                )
            }

            is Screen.ReportItem -> {
                ReportItemScreen(
                    viewModel = viewModel,
                    initialType = screen.type,
                    onNavigateBack = { currentScreen = Screen.Home },
                    onSuccessSubmitted = { newId -> currentScreen = Screen.Detail(newId) }
                )
            }

            is Screen.Search -> {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.Home },
                    onItemClick = { itemId -> currentScreen = Screen.Detail(itemId) }
                )
            }

            is Screen.Detail -> {
                ItemDetailScreen(
                    itemId = screen.itemId,
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.Home },
                    onNavigateToItem = { counterpartId -> currentScreen = Screen.Detail(counterpartId) }
                )
            }

            is Screen.Dashboard -> {
                UserDashboardScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.Home },
                    onReportLost = { currentScreen = Screen.ReportItem("LOST") },
                    onReportFound = { currentScreen = Screen.ReportItem("FOUND") },
                    onSearchItems = { currentScreen = Screen.Search },
                    onItemClick = { itemId -> currentScreen = Screen.Detail(itemId) },
                    onLogout = { currentScreen = Screen.Auth }
                )
            }

            is Screen.Admin -> {
                AdminPanelScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.Home },
                    onItemClick = { itemId -> currentScreen = Screen.Detail(itemId) }
                )
            }

            is Screen.Auth -> {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = { currentScreen = Screen.Home }
                )
            }
        }
    }
}
