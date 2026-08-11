package com.bumh3r.ui.components

import java.text.NumberFormat
import java.util.Locale

private val formateadorMxn: NumberFormat by lazy {
    NumberFormat.getCurrencyInstance(Locale("es", "MX"))
}

/** Formatea un monto en pesos mexicanos, ej. 1234.5 -> "$1,234.50". */
fun formatearMoneda(monto: Double): String = formateadorMxn.format(monto)
