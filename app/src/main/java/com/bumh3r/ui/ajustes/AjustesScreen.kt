package com.bumh3r.ui.ajustes

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumh3r.ui.components.CampoNumerico

@Composable
fun AjustesScreen(viewModel: AjustesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val lanzadorOverlay = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.onRegresoDePermisoOverlay() }

    val lanzadorNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> viewModel.onPermisoNotificacionesResultado(concedido) }

    LaunchedEffect(uiState.solicitarPermisoOverlay) {
        if (uiState.solicitarPermisoOverlay) {
            lanzadorOverlay.launch(viewModel.crearIntentPermisoOverlay())
        }
    }

    LaunchedEffect(uiState.solicitarPermisoNotificaciones) {
        if (uiState.solicitarPermisoNotificaciones && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lanzadorNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ajustes", style = MaterialTheme.typography.headlineSmall)

            CampoNumerico(
                valor = uiState.precioGasolina,
                onValorCambia = viewModel::onPrecioGasolinaCambia,
                etiqueta = "Precio de la gasolina ($/litro)",
                mensajeError = null
            )
            CampoNumerico(
                valor = uiState.costoDesgastePorKm,
                onValorCambia = viewModel::onCostoDesgasteCambia,
                etiqueta = "Costo de desgaste base ($/km)",
                mensajeError = null
            )
            CampoNumerico(
                valor = uiState.umbralRentabilidad,
                onValorCambia = viewModel::onUmbralCambia,
                etiqueta = "Umbral de rentabilidad ($/km)",
                mensajeError = null
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::guardarParametros, modifier = Modifier.weight(1f)) {
                    Text("Guardar")
                }
                OutlinedButton(onClick = viewModel::restaurarValoresPorDefecto, modifier = Modifier.weight(1f)) {
                    Text("Restaurar valores por defecto")
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Burbuja flotante activa", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Ícono de acceso rápido sobre otras apps. Pide permiso de superposición" +
                            " y de notificaciones.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = uiState.burbujaActiva, onCheckedChange = viewModel::onBurbujaToggle)
            }
        }
    }
}
