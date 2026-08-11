package com.bumh3r.domain.model

/**
 * Tipos de terreno que puede recorrer el repartidor. Cada uno afecta el
 * rendimiento de gasolina de la moto y el desgaste mecánico por
 * kilómetro. Sin valores mágicos: cualquier lugar que necesite estos
 * números los lee de aquí.
 */
enum class TipoTerreno(
    val etiqueta: String,
    val rendimientoKmPorLitro: Double,
    val factorDesgaste: Double
) {
    PLANO(etiqueta = "Plano", rendimientoKmPorLitro = 35.0, factorDesgaste = 1.0),
    ALTO(etiqueta = "Alto", rendimientoKmPorLitro = 30.0, factorDesgaste = 1.1),
    CERRO(etiqueta = "Cerro", rendimientoKmPorLitro = 25.0, factorDesgaste = 1.3)
}
