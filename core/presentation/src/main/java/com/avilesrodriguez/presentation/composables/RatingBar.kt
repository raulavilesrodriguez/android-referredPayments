package com.avilesrodriguez.presentation.composables

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    rating: Double,
    maxRating: Int = 5,
    starSize: Dp = 14.dp,
    activeColor: Color = Color(0xFFFFC107),
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row {
        for (i in 1..maxRating) {
            when {
                rating >= i -> {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(starSize)
                    )
                }
                rating >= i - 0.5f -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.StarHalf,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(starSize)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = inactiveColor,
                        modifier = Modifier.size(starSize)
                    )
                }
            }
        }
    }
}