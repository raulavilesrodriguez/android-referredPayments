package com.avilesrodriguez.presentation.attachment

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun FileThumbnail(uriString: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uri = uriString.toUri()

    // Detectamos el tipo de archivo de forma más flexible para URLs remotas
    val mimeType = context.contentResolver.getType(uri)
    val isPdf = mimeType == "application/pdf" || uriString.lowercase().substringBefore("?").endsWith(".pdf")
    val isImage = mimeType?.startsWith("image") == true ||
            uriString.lowercase().let { it.contains(".jpg") || it.contains(".jpeg") || it.contains(".png") }

    if (isImage) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else if (isPdf) {
        var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
        var isProcessing by remember { mutableStateOf(false) }

        LaunchedEffect(uriString) {
            isProcessing = true
            withContext(Dispatchers.IO) {
                try {
                    val pfd: ParcelFileDescriptor? = if (uri.scheme == "https") {
                        // Para PDFs remotos, descargamos una copia temporal al cache
                        val cacheFile = File(context.cacheDir, "pdf_thumb_${uriString.hashCode()}.pdf")
                        if (!cacheFile.exists()) {
                            URL(uriString).openStream().use { input ->
                                FileOutputStream(cacheFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                        ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    } else {
                        // Para archivos locales (content:// o file://)
                        context.contentResolver.openFileDescriptor(uri, "r")
                    }

                    pfd?.use { descriptor ->
                        val renderer = PdfRenderer(descriptor)
                        if (renderer.pageCount > 0) {
                            val page = renderer.openPage(0)
                            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            thumbnail = bitmap
                            page.close()
                        }
                        renderer.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        }

        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        } else if (isProcessing) {
            Box(modifier = modifier.background(Color.LightGray), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else {
            // Diseño de respaldo si falla la carga del preview
            Box(
                modifier = modifier.background(Color(0xFFFDE7E9)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFB71C1C),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB71C1C),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    } else {
        // Documento Genérico para otros tipos
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
