package com.securitech.repartidor.domain.usecase

import com.bumh3r.domain.usecase.CalcularRentabilidadUseCase
import com.bumh3r.domain.model.Recomendacion
import com.bumh3r.domain.model.TipoTerreno
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalcularRentabilidadUseCaseTest {

    private lateinit var useCase: CalcularRentabilidadUseCase

    // Defaults de Configuracion, usados en todos los escenarios salvo que
    // se indique lo contrario.
    private val precioGasolina = 24.0
    private val costoDesgastePorKm = 0.50
    private val umbralRentabilidad = 2.50

    @Before
    fun antesDeCadaTest() {
        useCase = CalcularRentabilidadUseCase()
    }

    @Test
    fun `calcula CONVIENE cuando el pago por km supera el umbral`() {
        val resultado = useCase(
            pagoPedido = 50.0,
            kmTotales = 10.0,
            tipoTerreno = TipoTerreno.PLANO,
            precioGasolina = precioGasolina,
            costoDesgastePorKm = costoDesgastePorKm,
            umbralRentabilidad = umbralRentabilidad
        )

        assertEquals(0.69, resultado.costoGasolinaPorKm, 0.0)
        assertEquals(0.5, resultado.costoDesgastePorKm, 0.0)
        assertEquals(1.19, resultado.costoRealPorKm, 0.0)
        assertEquals(11.86, resultado.costoTotal, 0.0)
        assertEquals(38.14, resultado.gananciaNeta, 0.0)
        assertEquals(5.0, resultado.pagoPorKm, 0.0)
        assertEquals(Recomendacion.CONVIENE, resultado.recomendacion)
        assertTrue(resultado.rentable)
    }

    @Test
    fun `calcula AL LIMITE cuando el pago por km es exactamente igual al umbral`() {
        // Caso de verificación del spec: pago $35, 14 km, terreno CERRO.
        val resultado = useCase(
            pagoPedido = 35.0,
            kmTotales = 14.0,
            tipoTerreno = TipoTerreno.CERRO,
            precioGasolina = precioGasolina,
            costoDesgastePorKm = costoDesgastePorKm,
            umbralRentabilidad = umbralRentabilidad
        )

        assertEquals(0.96, resultado.costoGasolinaPorKm, 0.0)
        assertEquals(0.65, resultado.costoDesgastePorKm, 0.0)
        assertEquals(1.61, resultado.costoRealPorKm, 0.0)
        assertEquals(22.54, resultado.costoTotal, 0.0)
        assertEquals(12.46, resultado.gananciaNeta, 0.0)
        assertEquals(2.5, resultado.pagoPorKm, 0.0)
        assertEquals(Recomendacion.AL_LIMITE, resultado.recomendacion)
        assertFalse(resultado.rentable)
    }

    @Test
    fun `calcula NO CONVIENE cuando la ganancia neta es negativa`() {
        val resultado = useCase(
            pagoPedido = 10.0,
            kmTotales = 20.0,
            tipoTerreno = TipoTerreno.CERRO,
            precioGasolina = precioGasolina,
            costoDesgastePorKm = costoDesgastePorKm,
            umbralRentabilidad = umbralRentabilidad
        )

        assertEquals(-22.2, resultado.gananciaNeta, 0.0)
        assertEquals(Recomendacion.NO_CONVIENE, resultado.recomendacion)
        assertFalse(resultado.rentable)
    }

    @Test
    fun `lanza excepcion cuando los kilometros totales no son positivos`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                pagoPedido = 20.0,
                kmTotales = 0.0,
                tipoTerreno = TipoTerreno.PLANO,
                precioGasolina = precioGasolina,
                costoDesgastePorKm = costoDesgastePorKm,
                umbralRentabilidad = umbralRentabilidad
            )
        }
    }
}
