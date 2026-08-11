package com.bumh3r.domain.model

/**
 * Resumen agregado de todos los pedidos calculados en un día, usado como
 * encabezado de cada grupo en la pantalla de Historial.
 */
data class ResumenDia(
    val fecha: String,             // "yyyy-MM-dd"
    val gananciaNetaTotal: Double,
    val totalPedidos: Int,
    val pedidosQueConvenian: Int,
    val kmTotales: Double
)
