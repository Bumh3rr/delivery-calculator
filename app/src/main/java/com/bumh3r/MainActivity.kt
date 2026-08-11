package com.bumh3r

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bumh3r.service.BubbleService
import com.bumh3r.ui.navigation.NavGraph
import com.bumh3r.ui.navigation.Rutas
import com.bumh3r.ui.theme.RepartidorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var intentPendiente by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intentPendiente = intent
        setContent {
            RepartidorTheme {
                val navController = rememberNavController()
                val destinos = listOf(
                    Triple(Rutas.Calculadora, "Calculadora", Icons.Filled.Calculate),
                    Triple(Rutas.Historial, "Historial", Icons.Filled.History),
                    Triple(Rutas.Ajustes, "Ajustes", Icons.Filled.Settings)
                )

                LaunchedEffect(intentPendiente) {
                    if (intentPendiente?.getBooleanExtra(
                            BubbleService.EXTRA_ABRIR_CALCULADORA,
                            false
                        ) == true
                    ) {
                        navController.navigate(Rutas.Calculadora.ruta) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        intent.removeExtra(BubbleService.EXTRA_ABRIR_CALCULADORA)
                        intentPendiente = null
                    }
                }

                Scaffold(
                    bottomBar = {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val rutaActual = backStackEntry?.destination?.route
                        NavigationBar {
                            destinos.forEach { (ruta, etiqueta, icono) ->
                                NavigationBarItem(
                                    selected = rutaActual == ruta.ruta,
                                    onClick = {
                                        navController.navigate(ruta.ruta) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(icono, contentDescription = etiqueta) },
                                    label = { Text(etiqueta) }
                                )
                            }
                        }
                    }
                ) { paddingInterno ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(paddingInterno)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentPendiente = intent
    }
}
