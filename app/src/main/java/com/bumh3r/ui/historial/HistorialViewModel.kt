package com.bumh3r.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumh3r.data.local.entity.RegistroPedido
import com.bumh3r.data.repository.RepartidorRepository
import com.bumh3r.domain.model.ResumenDia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val repository: RepartidorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRegistros().collect { registros ->
                _uiState.value = HistorialUiState(
                    grupos = agruparPorDia(registros),
                    cargando = false
                )
            }
        }
    }

    /** Agrupa los registros por fecha y calcula el resumen de cada día. */
    private fun agruparPorDia(registros: List<RegistroPedido>): List<GrupoHistorialDia> =
        registros
            .groupBy { it.fecha }
            .map { (fecha, registrosDelDia) ->
                GrupoHistorialDia(
                    resumen = ResumenDia(
                        fecha = fecha,
                        gananciaNetaTotal = registrosDelDia.sumOf { it.gananciaNeta },
                        totalPedidos = registrosDelDia.size,
                        pedidosQueConvenian = registrosDelDia.count { it.rentable },
                        kmTotales = registrosDelDia.sumOf { it.kmTotales }
                    ),
                    registros = registrosDelDia
                )
            }
            .sortedByDescending { it.resumen.fecha }

    /** Elimina un registro. Se puede deshacer mientras el Snackbar esté visible. */
    fun eliminarRegistro(registro: RegistroPedido) {
        viewModelScope.launch { repository.eliminarRegistro(registro) }
    }

    /** Restaura un registro eliminado (acción "Deshacer" del Snackbar). */
    fun restaurarRegistro(registro: RegistroPedido) {
        viewModelScope.launch { repository.guardarRegistro(registro.copy(id = 0)) }
    }
}
