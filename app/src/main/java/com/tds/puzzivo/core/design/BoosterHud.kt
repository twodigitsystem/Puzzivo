package com.tds.puzzivo.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BoosterHud(
    hintsAvailable: Int,
    undosAvailable: Int,
    onUseHint: () -> Unit,
    onUseUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoosterButton(
            icon = "💡",
            label = "Hint",
            count = hintsAvailable,
            onClick = onUseHint
        )

        BoosterButton(
            icon = "↩️",
            label = "Undo",
            count = undosAvailable,
            onClick = onUseUndo
        )
    }
}

@Composable
private fun BoosterButton(
    icon: String,
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        color = NavySurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 16.sp)
            Text(
                text = "$label ($count)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) AccentYellow else TextSecondary
            )
        }
    }
}
