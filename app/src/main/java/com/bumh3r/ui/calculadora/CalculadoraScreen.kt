package com.bumh3r.ui.calculadora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.bumh3r.domain.model.textoMostrar
import com.bumh3r.ui.components.CampoNumerico
import com.bumh3r.ui.components.SelectorTerreno
import com.bumh3r.ui.components.formatearMoneda
import com.bumh3r.ui.theme.colorDeRecomendacion

/** Pantalla principal: ingresar pago, km y terreno, y ver la rentabilidad. */
@Composable
fun CalculadoraScreen(viewModel: CalculadoraViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            snackbarHostState.showSnackbar("Guardado en el historial")
            viewModel.onGuardadoMostrado()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Calculadora de Rentabilidad",
                style = MaterialTheme.typography.headlineSmall
            )

            CampoNumerico(
                valor = uiState.pagoPedido,
                onValorCambia = viewModel::onPagoCambia,
                etiqueta = "Pago del pedido ($)",
                mensajeError = uiState.errorPago
            )

            CampoNumerico(
                valor = uiState.kmTotales,
                onValorCambia = viewModel::onKmCambia,
                etiqueta = if (uiState.incluirRegreso) {
                    "Kilómetros de ida (se duplicará automáticamente)"
                } else {
                    "Kilómetros totales (ida + vuelta)"
                },
                mensajeError = uiState.errorKm
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Incluir regreso a zona (×2)")
                Switch(
                    checked = uiState.incluirRegreso,
                    onCheckedChange = viewModel::onIncluirRegresoCambia
                )
            }

            SelectorTerreno(
                seleccionado = uiState.tipoTerreno,
                onSeleccionCambia = viewModel::onTerrenoCambia
            )

            Button(onClick = viewModel::calcular, modifier = Modifier.fillMaxWidth()) {
                Text("Calcular")
            }

            uiState.resultado?.let { resultado ->
                val color = colorDeRecomendacion(resultado.recomendacion)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = resultado.recomendacion.textoMostrar(),
                            style = MaterialTheme.typography.labelLarge,
                            color = color
                        )
                        Text(
                            text = formatearMoneda(resultado.gananciaNeta),
                            style = MaterialTheme.typography.displaySmall,
                            color = color
                        )
                        Text("Ganancia neta estimada", style = MaterialTheme.typography.bodySmall)

                        DesgloseFila("Costo gasolina/km",
                            formatearMoneda(resultado.costoGasolinaPorKm)
                        )
                        DesgloseFila("Costo desgaste/km",
                            formatearMoneda(resultado.costoDesgastePorKm)
                        )
                        DesgloseFila("Costo real/km", formatearMoneda(resultado.costoRealPorKm))
                        DesgloseFila("Costo total del viaje", formatearMoneda(resultado.costoTotal))
                        DesgloseFila("Pago por km", formatearMoneda(resultado.pagoPorKm))
                    }
                }

                Button(
                    onClick = viewModel::guardarEnHistorial,
                    enabled = !uiState.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar en historial")
                }
            }
        }
    }
}

@Composable
private fun DesgloseFila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
