package com.avilesrodriguez.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    rating: Double,
    maxRating: Int = 5,
    starSize: Dp = 14.dp,
    activeColor: Color = Color(0xFFFFC107),
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onRatingChanged: (Double) -> Unit = {}
) {
    Row {
        for (i in 1..maxRating) {
            val icon = when {
                rating >= i -> Icons.Default.Star
                rating >= i - 0.5 -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarBorder
            }

            val tint = when {
                rating >= i -> activeColor
                rating >= i - 0.5 -> activeColor
                else -> inactiveColor
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(starSize)
                    .clickable(
                        role = Role.Button,
                        onClick = { onRatingChanged(i.toDouble()) }
                    )
            )
        }
    }
}