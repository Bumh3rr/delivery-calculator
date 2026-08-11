package com.bumh3r.ui.ajustes

data class AjustesUiState(
    val precioGasolina: String = "24.0",
    val costoDesgastePorKm: String = "0.50",
    val umbralRentabilidad: String = "2.50",
    val burbujaActiva: Boolean = false,
    val activandoBurbuja: Boolean = false,
    val solicitarPermisoOverlay: Boolean = false,
    val solicitarPermisoNotificaciones: Boolean = false,
    val mensaje: String? = null
)
