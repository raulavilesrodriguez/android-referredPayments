package com.avilesrodriguez.presentation.photo

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun pickImageLauncher(
    context: Context,
    updatePhotoUri: (String) -> Unit,
    @StringRes errorCropping: Int,
    @StringRes errorSaving: Int
) : ActivityResultLauncher<PickVisualMediaRequest> {

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary.toArgb()

    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            if (resultUri != null) {
                updatePhotoUri(resultUri.toString())
            } else {
                SnackbarManager.showMessage(errorCropping)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            val errorMessage = context.getString(
                errorSaving, cropError?.message ?: "")
            SnackbarManager.showMessage(errorMessage)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Archivo de salida temporal
            val destinationUri = Uri.fromFile(
                File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
            )

            val options = UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(90)
                setToolbarTitle("Recortar Imagen")

                setToolbarColor(primaryColor)
                setStatusBarLight(true)
                setToolbarWidgetColor(onPrimaryColor) // las letras del toolbar son blancas
                setActiveControlsWidgetColor(Color.Magenta.toArgb())
                setHideBottomControls(false) // 👈 muestra controles de rotación/escala
                setFreeStyleCropEnabled(true) // 👈 permite redimensionar libremente
                setRootViewBackgroundColor(primaryColor) // a veces no basta
                setDimmedLayerColor(Color(0x8C000000).toArgb())
            }

            val intent = UCrop.of(selectedUri, destinationUri)
                .withAspectRatio(1f, 1f) // 👈 cuadrado, puedes cambiarlo
                .withOptions(options)
                .getIntent(context)

            uCropLauncher.launch(intent)
        }
    }
    return pickImageLauncher
}