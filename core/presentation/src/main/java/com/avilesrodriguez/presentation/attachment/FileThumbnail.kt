package com.avilesrodriguez.presentation.attachment

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
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
    val cleanUrl = uriString.substringBefore("?").lowercase()
    val mimeType = context.contentResolver.getType(uri)

    val isPdf = mimeType == "application/pdf" || cleanUrl.endsWith(".pdf")
    val isImage = mimeType?.startsWith("image") == true ||
            cleanUrl.let { it.contains(".jpg") || it.contains(".jpeg") || it.contains(".png") }
    val isWord = mimeType?.contains("word") == true || cleanUrl.endsWith(".doc") || cleanUrl.endsWith(".docx")
    val isExcel = mimeType?.contains("excel") == true || mimeType?.contains("sheet") == true || cleanUrl.endsWith(".xls") || cleanUrl.endsWith(".xlsx")

    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(uriString) {
        if (isImage) return@LaunchedEffect
        isProcessing = true
        withContext(Dispatchers.IO) {
            try {
                if (isPdf) {
                    val pfd: ParcelFileDescriptor? = if (uri.scheme == "https") {
                        val cacheFile = File(context.cacheDir, "pdf_thumb_${uriString.hashCode()}.pdf")
                        if (!cacheFile.exists()) {
                            URL(uriString).openStream().use { input -> FileOutputStream(cacheFile).use { output -> input.copyTo(output) } }
                        }
                        ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    } else {
                        context.contentResolver.openFileDescriptor(uri, "r")
                    }
                    pfd?.use { descriptor ->
                        val renderer = PdfRenderer(descriptor)
                        val page = renderer.openPage(0)
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        thumbnail = bitmap
                        page.close()
                        renderer.close()
                    }
                } else if ((isWord || isExcel) && uri.scheme != "https" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Intento de miniatura nativa para Word/Excel LOCALES (Android 10+)
                    thumbnail = context.contentResolver.loadThumbnail(uri,
                        Size(300, 300), null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isProcessing = false
            }
        }
    }

    // Renderizado de la UI
    Box(modifier = modifier) {
        if (isImage) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (isProcessing) {
            Box(Modifier
                .fillMaxSize()
                .background(Color.LightGray), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        } else {
            // FALLBACKS DE ICONOS (Cuando es remoto o falla la miniatura)
            val (bgColor, iconColor, label, icon) = when {
                isPdf -> listOf(Color(0xFFFDE7E9), Color(0xFFB71C1C), "PDF", Icons.Default.PictureAsPdf)
                isWord -> listOf(Color(0xFFE3F2FD), Color(0xFF1565C0), "DOCX", Icons.Default.Description)
                isExcel -> listOf(Color(0xFFE8F5E9), Color(0xFF2E7D32), "XLSX", Icons.Default.Description)
                else -> listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary, "FILE", Icons.Default.Description)
            }

            Box(Modifier
                .fillMaxSize()
                .background(bgColor as Color), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon as ImageVector, null, tint = iconColor as Color, modifier = Modifier.size(32.dp))
                    Text(text = label as String, style = MaterialTheme.typography.labelSmall, color = iconColor, fontWeight = Bold)
                }
            }
        }
    }
}
