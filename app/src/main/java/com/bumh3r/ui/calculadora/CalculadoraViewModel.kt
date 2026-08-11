package com.bumh3r.ui.calculadora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumh3r.data.local.entity.Configuracion
import com.bumh3r.data.local.entity.RegistroPedido
import com.bumh3r.data.repository.RepartidorRepository
import com.bumh3r.domain.model.TipoTerreno
import com.bumh3r.domain.model.textoMostrar
import com.bumh3r.domain.usecase.CalcularRentabilidadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalculadoraViewModel @Inject constructor(
    private val repository: RepartidorRepository,
    private val calcularRentabilidad: CalcularRentabilidadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculadoraUiState())
    val uiState: StateFlow<CalculadoraUiState> = _uiState.asStateFlow()

    fun onPagoCambia(valor: String) {
        _uiState.value = _uiState.value.copy(pagoPedido = valor, errorPago = null, resultado = null)
    }

    fun onKmCambia(valor: String) {
        _uiState.value = _uiState.value.copy(kmTotales = valor, errorKm = null, resultado = null)
    }

    fun onIncluirRegresoCambia(valor: Boolean) {
        _uiState.value = _uiState.value.copy(incluirRegreso = valor, resultado = null)
    }

    fun onTerrenoCambia(tipo: TipoTerreno) {
        _uiState.value = _uiState.value.copy(tipoTerreno = tipo, resultado = null)
    }

    /** Valida los campos, calcula la rentabilidad y actualiza el estado. */
    fun calcular() {
        val estado = _uiState.value
        val pago = estado.pagoPedido.replace(',', '.').toDoubleOrNull()
        val kmIngresados = estado.kmTotales.replace(',', '.').toDoubleOrNull()

        val errorPago = when {
            pago == null -> "Ingresa un pago válido"
            pago <= 0 -> "El pago debe ser mayor a cero"
            else -> null
        }
        val errorKm = when {
            kmIngresados == null -> "Ingresa los kilómetros"
            kmIngresados <= 0 -> "Los kilómetros deben ser mayores a cero"
            else -> null
        }

        if (errorPago != null || errorKm != null) {
            _uiState.value = estado.copy(errorPago = errorPago, errorKm = errorKm, resultado = null)
            return
        }

        val kmReales = if (estado.incluirRegreso) kmIngresados!! * 2 else kmIngresados!!

        viewModelScope.launch {
            val config = repository.getConfiguracion().first() ?: Configuracion()
            val resultado = calcularRentabilidad(
                pagoPedido = pago!!,
                kmTotales = kmReales,
                tipoTerreno = estado.tipoTerreno,
                precioGasolina = config.precioGasolina,
                costoDesgastePorKm = config.costoDesgastePorKm,
                umbralRentabilidad = config.umbralRentabilidad
            )
            _uiState.value = _uiState.value.copy(resultado = resultado, errorPago = null, errorKm = null)
        }
    }

    /** Guarda el cálculo actual en el historial. */
    fun guardarEnHistorial() {
        val resultado = _uiState.value.resultado ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            val registro = RegistroPedido(
                fecha = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                timestamp = System.currentTimeMillis(),
                pagoPedido = resultado.pagoPedido,
                kmTotales = resultado.kmTotales,
                tipoTerreno = resultado.tipoTerreno.name,
                costoRealPorKm = resultado.costoRealPorKm,
                costoTotal = resultado.costoTotal,
                gananciaNeta = resultado.gananciaNeta,
                pagoPorKm = resultado.pagoPorKm,
                rentable = resultado.rentable,
                recomendacion = resultado.recomendacion.textoMostrar()
            )
            repository.guardarRegistro(registro)
            _uiState.value = _uiState.value.copy(guardando = false, guardadoExitoso = true)
        }
    }

    /** Limpia la bandera de guardado exitoso tras mostrar el Snackbar. */
    fun onGuardadoMostrado() {
        _uiState.value = _uiState.value.copy(guardadoExitoso = false)
    }
}
