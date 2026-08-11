package com.bumh3r.ui.navigation

/** Rutas de navegación de la app, como constantes tipadas. */
sealed class Rutas(val ruta: String) {
    data object Calculadora : Rutas("calculadora")
    data object Historial : Rutas("historial")
    data object Ajustes : Rutas("ajustes")
}
