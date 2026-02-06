package com.avilesrodriguez.presentation.ext

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun getExtensionFromUri(contentResolver: ContentResolver, uri: Uri): String {
    return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
    } else {
        uri.path?.let { path ->
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(path)).toString())
        }
    } ?: "bin" // Valor por defecto si no se reconoce
}