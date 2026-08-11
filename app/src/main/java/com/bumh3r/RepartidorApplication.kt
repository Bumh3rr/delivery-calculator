package com.bumh3r

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de Hilt: genera el componente de dependencias raíz de
 * toda la app.
 */
@HiltAndroidApp
class RepartidorApplication : Application()
