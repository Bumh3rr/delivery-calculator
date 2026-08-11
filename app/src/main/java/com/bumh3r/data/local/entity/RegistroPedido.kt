package com.bumh3r.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un cálculo de rentabilidad guardado por el repartidor. `fecha` se
 * guarda por separado de `timestamp` para poder agrupar por día sin tener
 * que derivarla de un epoch cada vez.
 */
@Entity(tableName = "registros")
data class RegistroPedido(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,          // "yyyy-MM-dd", para agrupar por día
    val timestamp: Long,        // epoch millis, para ordenar
    val pagoPedido: Double,
    val kmTotales: Double,
    val tipoTerreno: String,    // TipoTerreno.name
    val costoRealPorKm: Double,
    val costoTotal: Double,
    val gananciaNeta: Double,
    val pagoPorKm: Double,
    val rentable: Boolean,
    val recomendacion: String
)
