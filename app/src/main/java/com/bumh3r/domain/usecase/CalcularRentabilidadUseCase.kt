package com.bumh3r.domain.usecase

import com.bumh3r.domain.model.Recomendacion
import com.bumh3r.domain.model.ResultadoCalculo
import com.bumh3r.domain.model.TipoTerreno
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Calcula la rentabilidad de un pedido: cuánto cuesta realmente recorrerlo
 * (gasolina + desgaste de la moto) y si conviene aceptarlo según el pago
 * ofrecido. Lógica pura, sin dependencias de Android ni de Room — fácil
 * de testear.
 */
class CalcularRentabilidadUseCase @Inject constructor() {

    operator fun invoke(
        pagoPedido: Double,
        kmTotales: Double,
        tipoTerreno: TipoTerreno,
        precioGasolina: Double,
        costoDesgastePorKm: Double,
        umbralRentabilidad: Double
    ): ResultadoCalculo {
        require(pagoPedido > 0) { "El pago del pedido debe ser mayor a cero" }
        require(kmTotales > 0) { "Los kilómetros totales deben ser mayores a cero" }

        val costoGasolinaPorKm = precioGasolina / tipoTerreno.rendimientoKmPorLitro
        val costoDesgasteReal = costoDesgastePorKm * tipoTerreno.factorDesgaste
        val costoRealPorKm = costoGasolinaPorKm + costoDesgasteReal

        val costoTotal = costoRealPorKm * kmTotales
        val gananciaNeta = pagoPedido - costoTotal
        val pagoPorKm = pagoPedido / kmTotales

        // Regla de negocio confirmada con el usuario: en el umbral exacto
        // todavía NO conviene (zona gris, sin margen). Solo estrictamente
        // por encima del umbral es CONVIENE.
        val recomendacion = when {
            gananciaNeta <= 0 -> Recomendacion.NO_CONVIENE
            pagoPorKm <= umbralRentabilidad -> Recomendacion.AL_LIMITE
            else -> Recomendacion.CONVIENE
        }
        val rentable = pagoPorKm > umbralRentabilidad && gananciaNeta > 0

        return ResultadoCalculo(
            pagoPedido = pagoPedido.redondear(),
            kmTotales = kmTotales.redondear(),
            tipoTerreno = tipoTerreno,
            costoGasolinaPorKm = costoGasolinaPorKm.redondear(),
            costoDesgastePorKm = costoDesgasteReal.redondear(),
            costoRealPorKm = costoRealPorKm.redondear(),
            costoTotal = costoTotal.redondear(),
            gananciaNeta = gananciaNeta.redondear(),
            pagoPorKm = pagoPorKm.redondear(),
            rentable = rentable,
            recomendacion = recomendacion
        )
    }

    private fun Double.redondear(): Double =
        BigDecimal(this.toString()).setScale(2, RoundingMode.HALF_UP).toDouble()
}
