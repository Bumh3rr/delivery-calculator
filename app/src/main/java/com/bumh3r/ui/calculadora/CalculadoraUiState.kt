package com.bumh3r.ui.calculadora

import com.bumh3r.domain.model.ResultadoCalculo
import com.bumh3r.domain.model.TipoTerreno

/**
 * Estado único e inmutable de la pantalla Calculadora (UDF): el
 * composable solo lee este estado y envía eventos al ViewModel.
 */
data class CalculadoraUiState(
    val pagoPedido: String = "",
    val kmTotales: String = "",
    val incluirRegreso: Boolean = false,
    val tipoTerreno: TipoTerreno = TipoTerreno.PLANO,
    val errorPago: String? = null,
    val errorKm: String? = null,
    val resultado: ResultadoCalculo? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)
