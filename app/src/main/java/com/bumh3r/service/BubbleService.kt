package com.bumh3r.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.bumh3r.MainActivity
import com.bumh3r.data.local.entity.Configuracion
import com.securitech.repartidor.R
import com.bumh3r.data.repository.RepartidorRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

/**
 * Service en segundo plano que dibuja la burbuja flotante sobre cualquier
 * app. Al tocarla (sin arrastrarla) abre [MainActivity] directo en la
 * Calculadora. No usa Compose: es una vista clásica añadida por
 * [WindowManager] — más simple y robusta para este caso de uso que
 * hospedar Compose en un overlay, ya que la burbuja solo necesita mostrar
 * un ícono y detectar tap/arrastre, no una UI interactiva.
 */
@AndroidEntryPoint
class BubbleService : Service() {

    @Inject
    lateinit var repository: RepartidorRepository

    private var windowManager: WindowManager? = null
    private var vistaBurbuja: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanalSiHaceFalta(this)
        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, ID_NOTIFICACION, NotificationHelper.crearNotificacion(this), tipo)
        mostrarBurbuja()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACCION_DETENER) {
            detenerServicio()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        ocultarBurbuja()
        super.onDestroy()
    }

    private fun mostrarBurbuja() {
        if (vistaBurbuja != null) return

        // Corrección (hallazgo de la revisión final): re-verificar el
        // permiso aquí, no solo en AjustesViewModel. Si el usuario lo
        // revoca desde Ajustes del sistema mientras la burbuja sigue
        // activa, START_STICKY puede reiniciar este Service — sin este
        // chequeo, addView() lanzaría BadTokenException y tumbaría el
        // proceso en vez de simplemente apagarse.
        if (!Settings.canDrawOverlays(this)) {
            detenerServicio()
            return
        }

        val manager = ContextCompat.getSystemService(this, WindowManager::class.java) ?: return
        windowManager = manager

        val tamanoPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            TAMANO_BURBUJA_DP,
            resources.displayMetrics
        ).toInt()

        val imagen = ImageView(this).apply {
            setImageResource(R.drawable.ic_launcher_foreground)
            setBackgroundResource(R.drawable.fondo_burbuja)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            elevation = 8f
        }

        val tipoVentana = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val parametros = WindowManager.LayoutParams(
            tamanoPx,
            tamanoPx,
            tipoVentana,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        // Umbral de arrastre real del sistema (respeta la densidad de pantalla
        // y la configuración de accesibilidad del dispositivo) en vez de un
        // valor fijo en píxeles crudos.
        val umbralArrastrePx = ViewConfiguration.get(this).scaledTouchSlop

        var xInicial = 0
        var yInicial = 0
        var xTocado = 0f
        var yTocado = 0f
        var seMovio = false

        imagen.setOnTouchListener { vista, evento ->
            when (evento.action) {
                MotionEvent.ACTION_DOWN -> {
                    xInicial = parametros.x
                    yInicial = parametros.y
                    xTocado = evento.rawX
                    yTocado = evento.rawY
                    seMovio = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (evento.rawX - xTocado).toInt()
                    val deltaY = (evento.rawY - yTocado).toInt()
                    if (abs(deltaX) > umbralArrastrePx || abs(deltaY) > umbralArrastrePx) {
                        seMovio = true
                    }
                    parametros.x = xInicial + deltaX
                    parametros.y = yInicial + deltaY
                    windowManager?.updateViewLayout(vista, parametros)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!seMovio) abrirCalculadora()
                    true
                }
                else -> false
            }
        }

        // runCatching: addView puede fallar con BadTokenException si el
        // permiso se revoca justo entre el chequeo de arriba y esta línea
        // (condición de carrera del sistema, poco común pero posible) — en
        // vez de tumbar el Service, simplemente nos detenemos.
        runCatching { windowManager?.addView(imagen, parametros) }
            .onFailure {
                detenerServicio()
                return
            }
        vistaBurbuja = imagen
    }

    private fun ocultarBurbuja() {
        vistaBurbuja?.let { vista -> runCatching { windowManager?.removeView(vista) } }
        vistaBurbuja = null
    }

    private fun abrirCalculadora() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ABRIR_CALCULADORA, true)
        }
        startActivity(intent)
    }

    private fun detenerServicio() {
        // Persiste burbujaActiva = false para que el switch de Ajustes no
        // se quede mostrando "activo" cuando el usuario detiene la burbuja
        // desde la notificación en vez de desde el switch. Se usa un scope
        // nuevo (no atado al ciclo de vida del Service) para que el
        // guardado no se cancele si onDestroy() se dispara antes de que
        // termine.
        CoroutineScope(Dispatchers.IO).launch {
            val actual = repository.getConfiguracion().first() ?: Configuracion()
            repository.guardarConfiguracion(actual.copy(burbujaActiva = false))
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val ACCION_DETENER = "com.securitech.repartidor.ACCION_DETENER_BURBUJA"
        const val EXTRA_ABRIR_CALCULADORA = "extra_abrir_calculadora"
        const val ID_NOTIFICACION = 1001
        private const val TAMANO_BURBUJA_DP = 56f
    }
}
