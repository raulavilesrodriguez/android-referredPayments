package com.avilesrodriguez.presentation.attachment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage

@Composable
fun FileThumbnail(uriString: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uri = uriString.toUri()

    // Detectamos el tipo de archivo
    val mimeType = context.contentResolver.getType(uri)
    val isImage = mimeType?.startsWith("image") == true

    if (isImage) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Diseño para archivos que no son imágenes (PDF, etc.)
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val icon: ImageVector = when (mimeType) {
                "application/pdf" -> Icons.Default.PictureAsPdf
                else -> Icons.Default.Description
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                // mostrar extensión o texto pequeño
                if (mimeType == "application/pdf") {
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}