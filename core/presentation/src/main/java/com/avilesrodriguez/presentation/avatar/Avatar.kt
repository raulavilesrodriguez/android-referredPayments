package com.avilesrodriguez.presentation.avatar

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    photoUri: Any,
    size: Dp,
    contentDescription: String? = "User Avatar"
){
    AsyncImage(
        model = photoUri,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}