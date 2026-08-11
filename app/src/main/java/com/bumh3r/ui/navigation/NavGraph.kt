package com.bumh3r.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bumh3r.ui.ajustes.AjustesScreen
import com.bumh3r.ui.calculadora.CalculadoraScreen
import com.bumh3r.ui.historial.HistorialScreen

/** Grafo de navegación principal: Calculadora, Historial y Ajustes. */
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Rutas.Calculadora.ruta,
        modifier = modifier
    ) {
        composable(Rutas.Calculadora.ruta) { CalculadoraScreen() }
        composable(Rutas.Historial.ruta) { HistorialScreen() }
        composable(Rutas.Ajustes.ruta) { AjustesScreen() }
    }
}
