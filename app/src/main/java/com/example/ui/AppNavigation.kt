package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.viewmodel.MainViewModel

const val ROUTE_SPLASH = "splash"
const val ROUTE_LOGIN = "login"
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_CAMERA = "camera"
const val ROUTE_MAP = "map"
const val ROUTE_GALLERY = "gallery"
const val ROUTE_PROJECTS = "projects"
const val ROUTE_SETTINGS = "settings"

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_SPLASH
    ) {
        composable(ROUTE_SPLASH) {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        composable(ROUTE_LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToCamera = { navController.navigate(ROUTE_CAMERA) },
                onNavigateToGallery = { navController.navigate(ROUTE_GALLERY) },
                onNavigateToProjects = { navController.navigate(ROUTE_PROJECTS) },
                onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) },
                onNavigateToMap = { navController.navigate(ROUTE_MAP) }
            )
        }

        composable(ROUTE_CAMERA) {
            CameraScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_MAP) {
            MapScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_GALLERY) {
            GalleryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_PROJECTS) {
            ProjectManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
