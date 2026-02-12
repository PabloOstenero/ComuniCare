package com.example.comunicare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.example.comunicare.ui.utils.NotificationHelper
import com.example.comunicare.ui.viewmodel.HelpViewModel
import com.example.comunicare.ui.viewmodel.HelpViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * MainActivity: Punto de entrada de la aplicación ComuniCare.
 * Gestiona la navegación centralizada, la persistencia de sesión y los permisos de notificación (RA8).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = HelpRepositoryImpl(
            helpRequestDao = database.helpRequestDao(),
            chatMessageDao = database.chatMessageDao(),
            userDao = database.userDao(),
            context = this
        )
        val factory = HelpViewModelFactory(repository)

        setContent {
            ComuniCareTheme {
                val navController = rememberNavController()
                val viewModel: HelpViewModel = viewModel(factory = factory)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                val isSessionLoaded by viewModel.isSessionLoaded.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // RA8 - Lanzador para solicitar permisos de notificación en Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    // Solicitar permiso de forma proactiva al arrancar (RA8.a)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    viewModel.notificationEvent.collectLatest { (title, message) ->
                        NotificationHelper.showNotification(context, title, message)
                    }
                }

                if (!isSessionLoaded) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentUser != null && !currentRoute.isNullOrEmpty() &&
                                         currentRoute != "login" && currentRoute != "register" &&
                                         currentRoute != "change_password" && !currentRoute.startsWith("chat"),
                        drawerContent = {
                            ModalDrawerSheet {
                                Text("ComuniCare", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                                if (currentUser != null) {
                                    Text(
                                        text = "Hola, ${currentUser?.name}",
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
                                        val dest = if (currentUser?.role == UserRole.ADMIN) "admin_dashboard" else "beneficiary_home"
                                        navController.navigate(dest) { popUpTo(navController.graph.startDestinationId); launchSingleTop = true }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Seguridad") },
                                    selected = false,
                                    icon = { Icon(Icons.Default.Security, contentDescription = null) },
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate("trusted_contact") }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Cambiar contraseña") },
                                    selected = false,
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate("change_password") }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Ayuda") },
                                    selected = false,
                                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate("help") }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                NavigationDrawerItem(
                                    label = { Text("Cerrar sesión") },
                                    selected = false,
                                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                                    onClick = { scope.launch { drawerState.close() }; viewModel.logout(); navController.navigate("login") { popUpTo(0) } }
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                        ) { innerPadding ->
                            val startDestination = if (currentUser != null) {
                                if (currentUser?.role == UserRole.ADMIN) "admin_dashboard" else "beneficiary_home"
                            } else "login"

                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                modifier = Modifier.fillMaxSize().padding(innerPadding)
                            ) {
                                composable("login") {
                                    LoginScreen(viewModel, { navController.navigate("register") }, { role, isRecovery ->
                                        if (isRecovery) {
                                            navController.navigate("change_password")
                                        } else {
                                            val dest = if (role == UserRole.ADMIN) "admin_dashboard" else "beneficiary_home"
                                            navController.navigate(dest) { popUpTo("login") { inclusive = true } }
                                        }
                                    })
                                }
                                composable("register") {
                                    RegisterScreen(viewModel, { navController.popBackStack() }, { role ->
                                        val dest = if (role == UserRole.ADMIN) "admin_dashboard" else "beneficiary_home"
                                        navController.navigate(dest) { popUpTo("login") { inclusive = true } }
                                    })
                                }
                                composable("change_password") {
                                    ChangePasswordScreen(viewModel) {
                                        val dest = if (currentUser?.role == UserRole.ADMIN) "admin_dashboard" else "beneficiary_home"
                                        navController.navigate(dest) { popUpTo("login") { inclusive = true } }
                                    }
                                }
                                composable("settings") { ChangePasswordScreen(viewModel) { navController.popBackStack() } }
                                composable("beneficiary_home") { BeneficiaryHomeScreen(viewModel, { scope.launch { drawerState.open() } }, { id -> navController.navigate("chat/$id") }) }
                                composable("admin_dashboard") { AdminDashboardScreen(viewModel, { navController.navigate("reports") }, { scope.launch { drawerState.open() } }, { id -> navController.navigate("chat/$id") }) }
                                composable("trusted_contact") { TrustedContactScreen(viewModel) { navController.popBackStack() } }
                                composable("reports") { ReportsScreen(viewModel) }
                                composable("help") { HelpScreen(viewModel) }

                                composable(
                                    "chat/{requestId}",
                                    arguments = listOf(navArgument("requestId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                                    ChatScreen(
                                        viewModel = viewModel,
                                        requestId = requestId,
                                        currentUserId = currentUser?.id ?: "",
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
}
