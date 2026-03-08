package com.avilesrodriguez.presentation.photo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.BundleCompat
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

class MyCropImageActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private var imageUri: Uri? = null
    private var options: CropImageOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear un contenedor lineal para poner el Toolbar y el CropImageView
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // 1. Configurar Toolbar manualmente para asegurar que aparezca
        val toolbar = Toolbar(this).apply {
            title = "Recortar Imagen"
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { 
                setResult(Activity.RESULT_CANCELED)
                finish() 
            }
        }
        setSupportActionBar(toolbar)
        root.addView(toolbar)

        // 2. Configurar el CropImageView
        cropImageView = CropImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(cropImageView)

        setContentView(root)

        // Recuperar datos
        intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)?.let { bundle ->
            imageUri = BundleCompat.getParcelable(bundle, CropImage.CROP_IMAGE_EXTRA_SOURCE, Uri::class.java)
            options = BundleCompat.getParcelable(bundle, CropImage.CROP_IMAGE_EXTRA_OPTIONS, CropImageOptions::class.java)
        }

        cropImageView.setImageUriAsync(imageUri)
        options?.let { cropImageView.setImageCropOptions(it) }

        cropImageView.setOnCropImageCompleteListener { _, result ->
            val data = Intent()
            data.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result as Parcelable)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Añadimos el botón de Aceptar con un icono si es posible o texto
        val item = menu.add(Menu.NONE, 1, Menu.NONE, "ACEPTAR")
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            cropImageView.croppedImageAsync() // Esto dispara el setOnCropImageCompleteListener
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
