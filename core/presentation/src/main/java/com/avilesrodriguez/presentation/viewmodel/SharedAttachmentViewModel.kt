package com.avilesrodriguez.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedAttachmentViewModel @Inject constructor() : ViewModel() {
    
    // Usamos un companion object para que sea un "Buzón Global" 
    // que persista aunque Android intente crear instancias separadas.
    companion object {
        var sharedFileUri by mutableStateOf<String?>(null)
            private set

        fun setSharedFile(uri: String?) {
            sharedFileUri = uri
        }

        fun consumeFile() {
            sharedFileUri = null
        }
    }

    fun onFileReceived(uri: String?) {
        setSharedFile(uri)
    }

    fun consumeFile() {
        Companion.consumeFile()
    }
    
    // Getter para Compose
    val currentFileUri get() = sharedFileUri
}
