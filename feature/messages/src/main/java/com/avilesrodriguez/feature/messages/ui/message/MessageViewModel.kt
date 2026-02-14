package com.avilesrodriguez.feature.messages.ui.message

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetMessageById
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.core.net.toUri
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import kotlinx.coroutines.coroutineScope

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getMessageById: GetMessageById,
    private val getReferralById: GetReferralById,
    private val getUser: GetUser,
    @param:ApplicationContext private val context: Context  //to view the field. Se necesita para el intent
): BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _messageState = MutableStateFlow(Message())
    val messageState: StateFlow<Message> = _messageState.asStateFlow()
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var loadJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun loadMessage(messageId: String){
        loadJob?.cancel()
        _isLoading.value = true
        loadJob = launchCatching {
            val message = getMessageById(messageId)
            if(message != null){
                _messageState.value = message
                fetchReferralData(message.referralId) //al ser suspend se obliga a loadJob a esperar que se ejecute esta func
            }
            _isLoading.value = false
        }
    }

    private suspend fun fetchReferralData(referralId: String){
        val referral = getReferralById(referralId)
        if(referral != null){
            _referralState.value = referral
            // coroutineScope para poder usar async en la funcion suspend
            coroutineScope {
                val client = async { getUser(referral.clientId) }
                val provider = async { getUser(referral.providerId) }
                clientWhoReferred = client.await()
                providerThatReceived = provider.await()
            }
        }
    }

    fun downloadFile(uriString: String) {
        try {
            val cleanUrl = uriString.substringBefore("?")
            val decodedPath = Uri.decode(cleanUrl) // <--- DECODIFICA %2F a /
            val fileName = decodedPath.substringAfterLast("/")
            val uri = Uri.parse(uriString)

            // 1. Configuramos la descarga a través del sistema
            val request = DownloadManager.Request(uri)
                .setTitle(fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            SnackbarManager.showMessage(R.string.downloading)

            // 2. Registramos un receptor para abrir el archivo cuando termine la descarga
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context?, intent: Intent?) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        // 1. Obtenemos los índices de las columnas de forma segura
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                        // 2. Verificamos que los índices sean válidos antes de usarlos
                        if (statusIndex >= 0 && uriIndex >= 0) {
                            if (cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                                val localUriString = cursor.getString(uriIndex)
                                openDownloadedFile(localUriString)
                            }
                        }
                    }
                    cursor.close()
                    context.unregisterReceiver(this)
                }
            }

            context.registerReceiver(onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)

        } catch (e: Exception) {
            Log.e("MessageViewModel", "Error al iniciar descarga", e)
            SnackbarManager.showMessage(R.string.download_error)
        }
    }

    private fun openDownloadedFile(localUriString: String) {
        try {
            val localUri = Uri.parse(localUriString)
            val mimeType = context.contentResolver.getType(localUri)
                ?: android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    localUriString.substringAfterLast(".").lowercase()
                )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(localUri, mimeType)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Abrir con...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("MessageViewModel", "Error al abrir archivo descargado", e)
        }
    }

    fun replyMessage(openScreen: (String) -> Unit){
        val referralId = _referralState.value.id
        val route = NavRoutes.NEW_MESSAGE.replace("{${NavRoutes.ReferralArgs.ID}}", referralId)
        openScreen(route)
    }

}