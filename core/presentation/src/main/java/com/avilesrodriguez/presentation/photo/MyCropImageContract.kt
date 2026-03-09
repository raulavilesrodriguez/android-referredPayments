package com.avilesrodriguez.presentation.photo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageOptions

data class MyCropImageInputs(
    val uri: Uri?,
    val options: CropImageOptions
)

/**
 * Contrato simplificado que devuelve directamente la Uri de la imagen recortada.
 */
class MyCropImageContract : ActivityResultContract<MyCropImageInputs, Uri?>() {

    override fun createIntent(context: Context, input: MyCropImageInputs): Intent {
        return Intent(context, MyCropImageActivity::class.java).apply {
            val bundle = Bundle()
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, input.options)
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, input.uri)
            putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) intent?.data else null
    }
}
