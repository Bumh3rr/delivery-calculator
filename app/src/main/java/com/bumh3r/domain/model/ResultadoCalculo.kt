package com.bumh3r.domain.model

/**
 * Resultado completo de calcular la rentabilidad de un pedido, con el
 * desglose de costos necesario para mostrarlo en la UI. Todos los campos
 * numéricos vienen redondeados a 2 decimales.
 */
data class ResultadoCalculo(
    val pagoPedido: Double,
    val kmTotales: Double,
    val tipoTerreno: TipoTerreno,
    val costoGasolinaPorKm: Double,
    val costoDesgastePorKm: Double,
    val costoRealPorKm: Double,
    val costoTotal: Double,
    val gananciaNeta: Double,
    val pagoPorKm: Double,
    val rentable: Boolean,
    val recomendacion: Recomendacion
)

/** Nivel de recomendación según qué tan rentable resulta el pedido. */
enum class Recomendacion {
    CONVIENE,
    AL_LIMITE,
    NO_CONVIENE
}

/** Texto a mostrar en la UI y a guardar en `RegistroPedido.recomendacion`. */
fun Recomendacion.textoMostrar(): String = when (this) {
    Recomendacion.CONVIENE -> "CONVIENE"
    Recomendacion.AL_LIMITE -> "AL LÍMITE"
    Recomendacion.NO_CONVIENE -> "NO CONVIENE"
}
