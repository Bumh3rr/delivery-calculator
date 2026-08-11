package com.bumh3r.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumh3r.data.local.entity.RegistroPedido
import com.bumh3r.domain.model.TipoTerreno
import com.bumh3r.ui.components.formatearFechaLegible
import com.bumh3r.ui.components.formatearMoneda
import kotlinx.coroutines.launch
import kotlin.text.format

/** Pantalla de Historial: registros agrupados por día, con resumen y swipe-to-delete. */
@Composable
fun HistorialScreen(viewModel: HistorialViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (uiState.grupos.isEmpty() && !uiState.cargando) {
            EstadoVacioHistorial(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.grupos.forEach { grupo ->
                    item(key = "resumen-${grupo.resumen.fecha}") {
                        ResumenDiaCard(grupo)
                    }
                    items(grupo.registros, key = { it.id }) { registro ->
                        RegistroSwipeable(
                            registro = registro,
                            onEliminar = {
                                viewModel.eliminarRegistro(registro)
                                scope.launch {
                                    val resultado = snackbarHostState.showSnackbar(
                                        message = "Registro eliminado",
                                        actionLabel = "Deshacer"
                                    )
                                    if (resultado == SnackbarResult.ActionPerformed) {
                                        viewModel.restaurarRegistro(registro)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenDiaCard(grupo: GrupoHistorialDia) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(formatearFechaLegible(grupo.resumen.fecha), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Ganancia neta del día: ${formatearMoneda(grupo.resumen.gananciaNetaTotal)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${grupo.resumen.totalPedidos} pedidos · ${grupo.resumen.pedidosQueConvenian} convenían · " +
                    "${"%.1f".format(grupo.resumen.kmTotales)} km",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistroSwipeable(registro: RegistroPedido, onEliminar: () -> Unit) {
    val estadoSwipe = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = estadoSwipe,
        onDismiss = { onEliminar() },
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar registro",
                    tint = Color.White
                )
            }
        }
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(registro.recomendacion, style = MaterialTheme.typography.labelLarge)
                    Text(formatearMoneda(registro.gananciaNeta), style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "${TipoTerreno.valueOf(registro.tipoTerreno).etiqueta} · " +
                        "${"%.1f".format(registro.kmTotales)} km · ${formatearMoneda(registro.pagoPedido)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EstadoVacioHistorial(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Filled.Inbox, contentDescription = null)
            Text("Aún no has guardado ningún cálculo", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
