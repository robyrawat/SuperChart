package com.example.myapplication.demo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.demo.screens.AccessibilityDemoScreen
import com.example.myapplication.demo.screens.ChartListScreen
import com.example.myapplication.demo.screens.ChartPlaygroundScreen
import com.example.myapplication.demo.screens.ExportDemoScreen

sealed class Screen(val route: String) {
    data object ChartList : Screen("chart_list")
    data object ChartPlayground : Screen("chart_playground/{chartType}") {
        fun createRoute(chartType: String) = "chart_playground/$chartType"
    }
    data object ExportDemo : Screen("export_demo")
    data object AccessibilityDemo : Screen("accessibility_demo")
}

@Composable
fun DemoNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ChartList.route
    ) {
        composable(Screen.ChartList.route) {
            ChartListScreen(
                onChartSelected = { chartType ->
                    navController.navigate(Screen.ChartPlayground.createRoute(chartType))
                },
                onNavigateToExport = {
                    navController.navigate(Screen.ExportDemo.route)
                },
                onNavigateToAccessibility = {
                    navController.navigate(Screen.AccessibilityDemo.route)
                }
            )
        }

        composable(Screen.ChartPlayground.route) { backStackEntry ->
            val chartType = backStackEntry.arguments?.getString("chartType") ?: "line"
            ChartPlaygroundScreen(
                chartType = chartType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ExportDemo.route) {
            ExportDemoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AccessibilityDemo.route) {
            AccessibilityDemoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

