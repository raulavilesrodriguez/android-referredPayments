package com.avilesrodriguez.winapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import com.avilesrodriguez.presentation.viewmodel.SharedAttachmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

/**
 * Actividad transparente que solo se usa para recibir archivos compartidos.
 * Guarda el archivo en un ViewModel compartido y luego se autodestruye,
 * trayendo la MainActivity al frente sin reiniciarla.
 */
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    private val sharedViewModel: SharedAttachmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        
        // Una vez procesado el archivo, trae la tarea principal al frente y finaliza esta actividad.
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(mainIntent)
        finish()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {
            val uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            uri?.let { sharedUri ->
                copyFileToCache(sharedUri)
            }
        }
    }

    private fun copyFileToCache(sharedUri: Uri) {
        try {
            // 1. Intentamos obtener el MIME type real
            val mimeType = contentResolver.getType(sharedUri)
            
            // 2. Buscamos la extensión en el mapa del sistema
            // Si el archivo es .xlsx, .docx, .jpg, etc., MimeTypeMap devolverá la correcta.
            var extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            
            // 3. Si MimeTypeMap falla, intentamos extraerla del path original
            if (extension == null) {
                extension = MimeTypeMap.getFileExtensionFromUrl(sharedUri.toString())
            }

            // 4. Si aún es nulo, usamos "file" como fallback genérico
            val finalExtension = extension ?: "file"
            
            val tempFile = File(cacheDir, "shared_item_${System.currentTimeMillis()}.$finalExtension")
            
            contentResolver.openInputStream(sharedUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            sharedViewModel.onFileReceived(Uri.fromFile(tempFile).toString())
            
        } catch (e: Exception) {
            Log.e("ShareReceiver", "Error al clonar archivo compartido", e)
            sharedViewModel.onFileReceived(sharedUri.toString())
        }
    }
}
