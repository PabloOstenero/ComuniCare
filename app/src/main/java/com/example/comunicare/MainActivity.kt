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
 * MainActivity: Punto de entrada de la aplicación.
 * Gestiona la navegación principal, el menú lateral y los permisos de notificaciones (RA8).
 * Implementa persistencia de sesión para que la cuenta siga abierta tras cerrar la app (RA6.d).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicialización de la base de datos local Room (v10)
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
                
                // Observamos el estado de carga de la sesión
                val isSessionLoaded by viewModel.isSessionLoaded.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // RA8 - Manejo de permisos para notificaciones locales
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        // Opcional: Feedback si no acepta
                    }
                }

                LaunchedEffect(Unit) {
                    // Solicitar permiso en Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    
                    // Escucha activa de eventos de notificación disparados desde el ViewModel
                    viewModel.notificationEvent.collectLatest { (title, message) ->
                        NotificationHelper.showNotification(context, title, message)
                    }
                }

                if (!isSessionLoaded) {
                    // Pantalla de carga inicial (RA4.h)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        // El menú solo está disponible si el usuario está logueado y no está en el chat
                        gesturesEnabled = currentUser != null && !currentRoute.isNullOrEmpty() && 
                                         currentRoute != "login" && currentRoute != "register" && 
                                         !currentRoute.startsWith("chat"),
                        drawerContent = {
                            ModalDrawerSheet {
                                Text("ComuniCare", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                                if (currentUser != null) {
                                    Text(
                                        text = "Usuario: ${currentUser?.name} (${if(currentUser?.role == UserRole.ADMIN) "Admin" else "Beneficiario"})",
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
                                            navController.navigate("admin_dashboard") {
                                                popUpTo(navController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate("beneficiary_home") {
                                                popUpTo(navController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
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
                                        navController.navigate("login") {
                                            popUpTo(0)
                                        }
                                    }
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets.systemBars
                        ) { innerPadding ->
                            // El startDestination se decide según si hay una sesión guardada
                            val startDestination = if (currentUser != null) {
                                if (currentUser?.role == UserRole.ADMIN) "admin_dashboard" else "beneficiary_home"
                            } else {
                                "login"
                            }

                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                modifier = Modifier.fillMaxSize().padding(innerPadding)
                            ) {
                                composable("login") {
                                    LoginScreen(
                                        viewModel = viewModel,
                                        onNavigateToRegister = { navController.navigate("register") },
                                        onLoginSuccess = { role ->
                                            if (role == UserRole.ADMIN) {
                                                navController.navigate("admin_dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate("beneficiary_home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        }
                                    )
                                }
                                composable("register") {
                                    RegisterScreen(
                                        viewModel = viewModel,
                                        onBack = { navController.popBackStack() },
                                        onRegisterSuccess = { role ->
                                            if (role == UserRole.ADMIN) {
                                                navController.navigate("admin_dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate("beneficiary_home") {
                                                    popUpTo("login") { inclusive = true }
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
}
