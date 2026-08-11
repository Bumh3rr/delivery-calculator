package com.bumh3r.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.bumh3r.domain.model.TipoTerreno

private fun iconoDe(tipo: TipoTerreno): ImageVector = when (tipo) {
    TipoTerreno.PLANO -> Icons.AutoMirrored.Filled.TrendingFlat
    TipoTerreno.ALTO -> Icons.Filled.Terrain
    TipoTerreno.CERRO -> Icons.Filled.Landscape
}

/**
 * Selector de tipo de terreno con SegmentedButton. Sin estado propio: el
 * terreno seleccionado vive en el ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorTerreno(
    seleccionado: TipoTerreno,
    onSeleccionCambia: (TipoTerreno) -> Unit,
    modifier: Modifier = Modifier
) {
    val opciones = TipoTerreno.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        opciones.forEachIndexed { indice, tipo ->
            SegmentedButton(
                selected = tipo == seleccionado,
                onClick = { onSeleccionCambia(tipo) },
                shape = SegmentedButtonDefaults.itemShape(index = indice, count = opciones.size),
                icon = {
                    Icon(imageVector = iconoDe(tipo), contentDescription = null)
                }
            ) {
                Text(tipo.etiqueta)
            }
        }
    }
}
