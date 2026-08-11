package com.bumh3r.ui.historial

import com.bumh3r.data.local.entity.RegistroPedido
import com.bumh3r.domain.model.ResumenDia

/** Un grupo del historial: el resumen de un día y sus registros. */
data class GrupoHistorialDia(
    val resumen: ResumenDia,
    val registros: List<RegistroPedido>
)

data class HistorialUiState(
    val grupos: List<GrupoHistorialDia> = emptyList(),
    val cargando: Boolean = true
)
