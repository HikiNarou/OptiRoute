package com.optiroute.com.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType
import com.optiroute.com.presentation.ui.auth.LoginScreen
import com.optiroute.com.presentation.ui.auth.RegisterScreen
import com.optiroute.com.presentation.ui.admin.AdminDashboardScreen
import com.optiroute.com.presentation.ui.admin.UserManagementScreen
import com.optiroute.com.presentation.ui.kurir.KurirDashboardScreen
import com.optiroute.com.presentation.ui.umkm.UmkmDashboardScreen
import com.optiroute.com.presentation.ui.maps.MapScreen
import com.optiroute.com.presentation.ui.profile.ProfileEditScreen
import com.optiroute.com.presentation.ui.settings.PermissionSettingsScreen
import com.optiroute.com.presentation.ui.tracking.DeliveryTrackingScreen
import com.optiroute.com.presentation.viewmodel.AuthViewModel

@Composable
fun OptiRouteNavigation(
    navController: NavHostController = rememberNavController(),
    authState: AuthViewModel.AuthState,
    currentUser: User?
) {
    val startDestination = when {
        currentUser != null -> when (currentUser.userType) {
            UserType.UMKM -> Screen.UmkmDashboard.route
            UserType.ADMIN -> Screen.AdminDashboard.route
            UserType.KURIR -> Screen.KurirDashboard.route
        }
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { user ->
                    navController.navigate(
                        when (user.userType) {
                            UserType.UMKM -> Screen.UmkmDashboard.route
                            UserType.ADMIN -> Screen.AdminDashboard.route
                            UserType.KURIR -> Screen.KurirDashboard.route
                        }
                    ) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // UMKM Screens
        composable(Screen.UmkmDashboard.route) {
            UmkmDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onNavigateToTracking = {
                    navController.navigate(Screen.DeliveryTracking.route)
                },
                onNavigateToPermissions = {
                    navController.navigate(Screen.PermissionSettings.route)
                }
            )
        }

        // Admin Screens
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToUserManagement = {
                    navController.navigate(Screen.UserManagement.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onNavigateToTracking = {
                    navController.navigate(Screen.DeliveryTracking.route)
                },
                onNavigateToPermissions = {
                    navController.navigate(Screen.PermissionSettings.route)
                }
            )
        }

        composable(Screen.UserManagement.route) {
            UserManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Kurir Screens
        composable(Screen.KurirDashboard.route) {
            KurirDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onNavigateToTracking = {
                    navController.navigate(Screen.DeliveryTracking.route)
                },
                onNavigateToPermissions = {
                    navController.navigate(Screen.PermissionSettings.route)
                }
            )
        }

        // Common Screens
        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DeliveryTracking.route) {
            DeliveryTrackingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PermissionSettings.route) {
            PermissionSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Map.route,
            arguments = listOf(
                navArgument("routeId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            MapScreen(
                route = null, // Will be loaded by routeId in the screen
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartNavigation = {
                    // Handle navigation start
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object UmkmDashboard : Screen("umkm_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object KurirDashboard : Screen("kurir_dashboard")
    object UserManagement : Screen("user_management")
    object ProfileEdit : Screen("profile_edit")
    object DeliveryTracking : Screen("delivery_tracking")
    object PermissionSettings : Screen("permission_settings")
    object Map : Screen("map?routeId={routeId}")
}