package com.avilesrodriguez.presentation.photo

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.avilesrodriguez.presentation.R
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

class MyCropImageActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private var imageUri: Uri? = null
    private var options: CropImageOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenedor raíz con fondo NEGRO
        val root = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            setBackgroundColor(Color.BLACK)
        }

        // Toolbar con fondo NEGRO y texto BLANCO
        val toolbar = Toolbar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Usamos el string del recurso crop_image
            title = getString(R.string.crop_image)
            setBackgroundColor(Color.BLACK)
            setTitleTextColor(Color.WHITE)

            // Cambiar color de la flecha de atrás a blanco de forma segura
            val navigationIcon = ContextCompat.getDrawable(context, androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            if (navigationIcon != null) {
                val wrappedDrawable = DrawableCompat.wrap(navigationIcon)
                DrawableCompat.setTint(wrappedDrawable, Color.WHITE)
                setNavigationIcon(wrappedDrawable)
            }
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        root.addView(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Ajustamos el padding superior y lateral para evitar desbordes en horizontal (landscape)
            view.updatePadding(
                top = insets.top,
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        // Visor de recorte con fondo NEGRO
        cropImageView = CropImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setBackgroundColor(Color.BLACK)
        }
        root.addView(cropImageView)

        setContentView(root)

        intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)?.let { bundle ->
            imageUri = BundleCompat.getParcelable(bundle, CropImage.CROP_IMAGE_EXTRA_SOURCE, Uri::class.java)
            options = BundleCompat.getParcelable(bundle, CropImage.CROP_IMAGE_EXTRA_OPTIONS, CropImageOptions::class.java)
        }

        cropImageView.setImageUriAsync(imageUri)
        options?.let { cropImageView.setImageCropOptions(it) }

        cropImageView.setOnCropImageCompleteListener { _, result ->
            if (result.isSuccessful) {
                val data = Intent()
                data.data = result.uriContent
                setResult(Activity.RESULT_OK, data)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Usamos el string del recurso ok ("OK") y forzamos el color BLANCO
        val okText = getString(R.string.ok)
        val title = SpannableString(okText)
        title.setSpan(ForegroundColorSpan(Color.WHITE), 0, title.length, 0)
        
        val item = menu.add(Menu.NONE, 1, Menu.NONE, title)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                cropImageView.croppedImageAsync()
                true
            }
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
