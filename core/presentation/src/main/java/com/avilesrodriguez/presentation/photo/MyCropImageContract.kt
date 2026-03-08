package com.avilesrodriguez.presentation.photo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

/**
 * Datos de entrada personalizados para evitar el uso de CropImageContractOptions (deprecado).
 */
data class MyCropImageInputs(
    val uri: Uri?,
    val options: CropImageOptions
)

class MyCropImageContract : ActivityResultContract<MyCropImageInputs, CropImageView.CropResult>() {

    override fun createIntent(context: Context, input: MyCropImageInputs): Intent {
        // Apuntamos a NUESTRA propia actividad
        return Intent(context, MyCropImageActivity::class.java).apply {
            val bundle = Bundle()
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, input.options)
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, input.uri)
            putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): CropImageView.CropResult {
        if (intent == null) return CropImage.CancelledResult

        val result = IntentCompat.getParcelableExtra(
            intent,
            CropImage.CROP_IMAGE_EXTRA_RESULT,
            CropImageView.CropResult::class.java
        )

        return result ?: CropImage.CancelledResult
    }
}
