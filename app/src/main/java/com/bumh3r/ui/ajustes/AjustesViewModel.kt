package com.bumh3r.ui.ajustes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumh3r.data.local.entity.Configuracion
import com.bumh3r.data.repository.RepartidorRepository
import com.bumh3r.service.BubbleService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val repository: RepartidorRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AjustesUiState())
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    // Corrección (hallazgo de la revisión final): antes, el collector
    // pisaba los TRES campos de texto en cada emisión de Configuracion —
    // incluida la que dispara persistirBurbujaActiva()/BubbleService al
    // encender o apagar la burbuja. Si el usuario estaba escribiendo un
    // valor nuevo sin guardar y en ese momento tocaba el switch de la
    // burbuja (o la detenía desde la notificación), su edición se perdía
    // en silencio. Ahora solo la primera emisión siembra los campos de
    // texto; después, únicamente burbujaActiva se mantiene sincronizado.
    init {
        viewModelScope.launch {
            var primeraCarga = true
            repository.getConfiguracion().collect { config ->
                val actual = config ?: Configuracion()
                _uiState.value = if (primeraCarga) {
                    primeraCarga = false
                    _uiState.value.copy(
                        precioGasolina = actual.precioGasolina.toString(),
                        costoDesgastePorKm = actual.costoDesgastePorKm.toString(),
                        umbralRentabilidad = actual.umbralRentabilidad.toString(),
                        burbujaActiva = actual.burbujaActiva
                    )
                } else {
                    _uiState.value.copy(burbujaActiva = actual.burbujaActiva)
                }
            }
        }
    }

    fun onPrecioGasolinaCambia(valor: String) {
        _uiState.value = _uiState.value.copy(precioGasolina = valor)
    }

    fun onCostoDesgasteCambia(valor: String) {
        _uiState.value = _uiState.value.copy(costoDesgastePorKm = valor)
    }

    fun onUmbralCambia(valor: String) {
        _uiState.value = _uiState.value.copy(umbralRentabilidad = valor)
    }

    /** Valida y guarda los tres parámetros numéricos en Room. */
    fun guardarParametros() {
        val estado = _uiState.value
        val precio = estado.precioGasolina.replace(',', '.').toDoubleOrNull()
        val desgaste = estado.costoDesgastePorKm.replace(',', '.').toDoubleOrNull()
        val umbral = estado.umbralRentabilidad.replace(',', '.').toDoubleOrNull()

        if (precio == null || precio <= 0 || desgaste == null || desgaste < 0 || umbral == null || umbral < 0) {
            _uiState.value = estado.copy(mensaje = "Revisa que los valores sean números válidos")
            return
        }

        viewModelScope.launch {
            repository.guardarConfiguracion(
                Configuracion(
                    precioGasolina = precio,
                    costoDesgastePorKm = desgaste,
                    umbralRentabilidad = umbral,
                    burbujaActiva = estado.burbujaActiva
                )
            )
            _uiState.value = _uiState.value.copy(mensaje = "Parámetros guardados")
        }
    }

    /** Restaura precioGasolina, costoDesgastePorKm y umbralRentabilidad a sus valores por defecto. */
    fun restaurarValoresPorDefecto() {
        val defaults = Configuracion(burbujaActiva = _uiState.value.burbujaActiva)
        _uiState.value = _uiState.value.copy(
            precioGasolina = defaults.precioGasolina.toString(),
            costoDesgastePorKm = defaults.costoDesgastePorKm.toString(),
            umbralRentabilidad = defaults.umbralRentabilidad.toString()
        )
        viewModelScope.launch { repository.guardarConfiguracion(defaults) }
    }

    /** El usuario tocó el switch de la burbuja flotante. */
    fun onBurbujaToggle(activar: Boolean) {
        if (!activar) {
            desactivarBurbuja()
            return
        }
        if (!Settings.canDrawOverlays(context)) {
            _uiState.value = _uiState.value.copy(solicitarPermisoOverlay = true, activandoBurbuja = true)
            return
        }
        if (faltaPermisoNotificaciones()) {
            _uiState.value = _uiState.value.copy(solicitarPermisoNotificaciones = true, activandoBurbuja = true)
            return
        }
        activarBurbuja()
    }

    /**
     * Se llama tras volver de la pantalla de ajustes de superposición del
     * sistema. Si el permiso sigue sin concederse, se desiste en vez de
     * reintentar en bucle.
     */
    fun onRegresoDePermisoOverlay() {
        _uiState.value = _uiState.value.copy(solicitarPermisoOverlay = false)
        if (!_uiState.value.activandoBurbuja) return
        if (!Settings.canDrawOverlays(context)) {
            _uiState.value = _uiState.value.copy(
                activandoBurbuja = false,
                mensaje = "Se necesita el permiso de superposición para activar la burbuja"
            )
            return
        }
        onBurbujaToggle(activar = true)
    }

    /** Resultado del diálogo de permiso de notificaciones (Android 13+). */
    fun onPermisoNotificacionesResultado(concedido: Boolean) {
        _uiState.value = _uiState.value.copy(solicitarPermisoNotificaciones = false)
        if (concedido && _uiState.value.activandoBurbuja) {
            activarBurbuja()
        } else if (!concedido) {
            _uiState.value = _uiState.value.copy(
                activandoBurbuja = false,
                mensaje = "Se necesita el permiso de notificaciones para activar la burbuja"
            )
        }
    }

    /** Intent para abrir la pantalla del sistema "Mostrar sobre otras apps". */
    fun crearIntentPermisoOverlay(): Intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun onMensajeMostrado() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }

    private fun faltaPermisoNotificaciones(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun activarBurbuja() {
        ContextCompat.startForegroundService(context, Intent(context, BubbleService::class.java))
        persistirBurbujaActiva(true)
        _uiState.value = _uiState.value.copy(burbujaActiva = true, activandoBurbuja = false)
    }

    private fun desactivarBurbuja() {
        context.stopService(Intent(context, BubbleService::class.java))
        persistirBurbujaActiva(false)
        _uiState.value = _uiState.value.copy(burbujaActiva = false)
    }

    private fun persistirBurbujaActiva(activa: Boolean) {
        viewModelScope.launch {
            val actual = repository.getConfiguracion().first() ?: Configuracion()
            repository.guardarConfiguracion(actual.copy(burbujaActiva = activa))
        }
    }
}
