package com.bumh3r.ui.theme

import androidx.compose.ui.graphics.Color
import com.bumh3r.domain.model.Recomendacion

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Colores semánticos de marca: siempre verde/ámbar/rojo para que la
// recomendación se lea igual sin importar el dynamic color del sistema.
val VerdeConviene = Color(0xFF2E7D32)
val AmbarAlLimite = Color(0xFFF9A825)
val RojoNoConviene = Color(0xFFC62828)

/** Color semántico asociado a cada nivel de recomendación. */
fun colorDeRecomendacion(recomendacion: Recomendacion): Color = when (recomendacion) {
    Recomendacion.CONVIENE -> VerdeConviene
    Recomendacion.AL_LIMITE -> AmbarAlLimite
    Recomendacion.NO_CONVIENE -> RojoNoConviene
}
