package com.bumh3r.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Parámetros configurables del cálculo. Fila única (id = 1): no hay
 * múltiples configuraciones, solo la actual.
 */
@Entity(tableName = "configuracion")
data class Configuracion(
    @PrimaryKey val id: Int = 1,
    val precioGasolina: Double = 24.0,
    val costoDesgastePorKm: Double = 0.50,
    val umbralRentabilidad: Double = 2.50,
    val burbujaActiva: Boolean = false
)
