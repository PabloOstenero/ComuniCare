package com.example.comunicare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.comunicare.data.local.database.AppDatabase
import com.example.comunicare.data.repository.HelpRepositoryImpl
import com.example.comunicare.domain.model.UserRole
import com.example.comunicare.ui.screens.*
import com.example.comunicare.ui.theme.ComuniCareTheme
import com.example.comunicare.ui.viewmodel.HelpViewModel
import com.example.comunicare.ui.viewmodel.HelpViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = HelpRepositoryImpl(
            helpRequestDao = database.helpRequestDao(),
            chatMessageDao = database.chatMessageDao(),
            userDao = database.userDao()
        )
        val factory = HelpViewModelFactory(repository)

        setContent {
            ComuniCareTheme {
                val navController = rememberNavController()
                val viewModel: HelpViewModel = viewModel(factory = factory)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                val currentUser by viewModel.currentUser.collectAsState()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = currentRoute != null && currentRoute != "login" && !currentRoute.startsWith("chat"),
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("ComuniCare", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                            if (currentUser != null) {
                                Text(
                                    text = "Usuario: ${currentUser?.name}",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            NavigationDrawerItem(
                                label = { Text("Inicio") },
                                selected = false,
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentUser?.role == UserRole.ADMIN) {
                                        navController.navigate("admin_dashboard") { popUpTo(0) }
                                    } else {
                                        navController.navigate("beneficiary_home") { popUpTo(0) }
                                    }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Contacto de Confianza") },
                                selected = false,
                                icon = { Icon(Icons.Default.Security, contentDescription = null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("trusted_contact")
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Ayuda / Manual") },
                                selected = false,
                                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("help")
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            NavigationDrawerItem(
                                label = { Text("Cerrar sesión") },
                                selected = false,
                                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.logout()
                                    navController.navigate("login") { popUpTo(0) }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.systemBars
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "login",
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        ) {
                            composable("login") {
                                LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccess = { username, password, role ->
                                        viewModel.login(username, password, role) { user ->
                                            if (user.role == UserRole.ADMIN) {
                                                navController.navigate("admin_dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate("beneficiary_home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            composable("beneficiary_home") {
                                BeneficiaryHomeScreen(
                                    viewModel = viewModel,
                                    onOpenMenu = { scope.launch { drawerState.open() } },
                                    onNavigateToChat = { requestId -> navController.navigate("chat/$requestId") }
                                )
                            }
                            composable("admin_dashboard") {
                                AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToReports = { navController.navigate("reports") },
                                    onOpenMenu = { scope.launch { drawerState.open() } },
                                    onNavigateToChat = { requestId -> navController.navigate("chat/$requestId") }
                                )
                            }
                            composable("trusted_contact") {
                                TrustedContactScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("reports") { ReportsScreen(viewModel = viewModel) }
                            composable("help") { HelpScreen() }
                            composable(
                                "chat/{requestId}",
                                arguments = listOf(navArgument("requestId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                                ChatScreen(
                                    viewModel = viewModel,
                                    requestId = requestId,
                                    currentUserId = currentUser?.id ?: "",
                                    currentUserName = currentUser?.name ?: "",
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
