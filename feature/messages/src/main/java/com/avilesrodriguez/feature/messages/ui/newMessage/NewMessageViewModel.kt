package com.avilesrodriguez.feature.messages.ui.newMessage

import android.content.Context
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.referral.GetReferralById
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.domain.usecases.message.SaveMessage
import com.avilesrodriguez.domain.usecases.storage.UploadFile
import com.avilesrodriguez.presentation.ext.getExtensionFromUri
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.core.net.toUri
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.validationRules.ValidationRules
import com.avilesrodriguez.domain.usecases.user.GetUserFlow
import com.avilesrodriguez.domain.usecases.transactions.RejectReferralTransaction
import com.avilesrodriguez.domain.usecases.transactions.SendPayTransaction
import com.avilesrodriguez.presentation.banksPays.BanksEcuador
import com.avilesrodriguez.presentation.banksPays.getById
import kotlinx.coroutines.launch
import java.net.URLDecoder

@HiltViewModel
class NewMessageViewModel @Inject constructor(
    private val saveMessage: SaveMessage,
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getReferralById: GetReferralById,
    private val uploadFile: UploadFile,
    @param:ApplicationContext private val context: Context,
    private val getUserFlow: GetUserFlow,
    private val sendPayTransaction: SendPayTransaction,
    private val rejectReferralTransaction: RejectReferralTransaction
) : BaseViewModel() {
    private val _newMessageState = MutableStateFlow(Message())
    val newMessageState: StateFlow<Message> = _newMessageState.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    private val _initialStatus = MutableStateFlow(ReferralStatus.PROCESSING)
    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private val _localFiles = MutableStateFlow<List<String>>(emptyList())
    val localFiles = _localFiles.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _amountUsdState = MutableStateFlow("")
    val amountUsdState: StateFlow<String> = _amountUsdState.asStateFlow()
    private val _selectedOption = MutableStateFlow<BanksEcuador?>(null)
    val selectedOption: StateFlow<BanksEcuador?> = _selectedOption.asStateFlow()
    private var referralJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                getUserFlow(currentUserId).collect {
                    _userDataStore.value = it
                }
            }
        }
    }

    fun loadReferralInformation(referralId: String){
        referralJob?.cancel()
        referralJob = launchCatching {
            val referral = getReferralById(referralId)
            if(referral != null){
                _referralState.value = referral
                _initialStatus.value = referral.status
                launch {
                    getUserFlow(referral.clientId).collect {
                        clientWhoReferred = it
                    }
                }
                launch {
                    getUserFlow(referral.providerId).collect {
                        providerThatReceived = it
                    }
                }
            } else {
                _referralState.value = Referral()
            }
        }
    }

    fun onSubjectChange(newSubject: String){
        val filteredSubject = newSubject.take(ValidationRules.MAX_LENGTH_SUBJECT)
        _newMessageState.value = _newMessageState.value.copy(subject = filteredSubject)
    }

    fun onContentChange(newContent: String){
        val filteredContent = newContent.take(ValidationRules.MAX_LENGTH_CONTENT)
        _newMessageState.value = _newMessageState.value.copy(content = filteredContent)
    }

    fun onAttachFiles(uris: List<String>){
        _localFiles.value = (_localFiles.value + uris).distinct()
    }

    fun onRemoveFile(uri: String){
        _localFiles.value -= uri
    }

    private fun getFileNameFromUri(uriString: String): String {
        val uri = uriString.toUri()
        var name: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                Log.e("NewMessageViewModel", "Error querying content resolver: $e")
            }
        }
        name = name?.// Quitamos la extensión si ya la tiene para controlarla nosotros después
        substringBeforeLast(".")
            ?: try {
                val decoded = URLDecoder.decode(uriString.substringBefore("?"), "UTF-8")
                decoded.substringAfterLast("/").substringBeforeLast(".")
            } catch (e: Exception) {
                Log.e("NewMessageViewModel", "Error to extract file name: $e")
                "file"
            }
        return name
    }

    fun onSaveMessage(popUp: () -> Unit){
        val referral = _referralState.value
        if(referral.id.isEmpty()) return

        launchCatching {
            _isLoading.value = true

            val receiverId = if (currentUserId == referral.clientId) {
                referral.providerId
            } else {
                referral.clientId
            }

            val remoteUrls = _localFiles.value.mapIndexed { index, localUriString ->
                async {
                    val uri = localUriString.toUri()
                    val originalName = getFileNameFromUri(localUriString)
                    val extension = getExtensionFromUri(context.contentResolver, uri)
                    val timestamp = System.currentTimeMillis()

                    val remotePath = "messages/${referral.id}/${originalName}_${timestamp}_$index.$extension"
                    uploadFile(localUriString, remotePath)
                }
            }.awaitAll()

            val message = _newMessageState.value.copy(
                referralId = referral.id,
                senderId = currentUserId,
                receiverId = receiverId,
                attachmentsUrl = remoteUrls,
                createdAt = System.currentTimeMillis()
            )

            saveMessage(message)
            _isLoading.value = false
            resetValues()
            popUp()
        }
    }

    fun onAmountChange(amountUsd: String){
        val filtered = buildString {
            var dotUsed = false
            amountUsd.forEach { char ->
                when {
                    char.isDigit() -> append(char)
                    char == '.' && !dotUsed -> {
                        append(char)
                        dotUsed = true
                    }
                }
            }
        }
        _amountUsdState.value = filtered
    }

    fun onBankChange(bank: Int){
        val filteredBank = BanksEcuador.getById(bank)
        _selectedOption.value = filteredBank
    }

    fun resetValues(){
        _localFiles.value = emptyList()
        _newMessageState.value = Message()
        _amountUsdState.value = ""
        _selectedOption.value = null
    }

    fun onSendPay(subjectPaid:String, contentPaid: String, popUp: () -> Unit){
        val referral = _referralState.value
        if(referral.id.isEmpty()) return
        launchCatching {
            try{
                _isLoading.value = true
                val referral = _referralState.value
                val amountPaid = _amountUsdState.value.toDoubleOrNull() ?: 0.0

                val remoteUrls = _localFiles.value.mapIndexed { index, localUriString ->
                    async {
                        val uri = localUriString.toUri()
                        val originalName = getFileNameFromUri(localUriString)
                        val extension = getExtensionFromUri(context.contentResolver, uri)
                        val timestamp = System.currentTimeMillis()

                        val remotePath = "messages/${referral.id}/${originalName}_${timestamp}_$index.$extension"
                        uploadFile(localUriString, remotePath)
                    }
                }.awaitAll()

                val referralUpdates = mapOf(
                    "status" to ReferralStatus.PAID.name,
                    "amountPaid" to amountPaid,
                    "updatedAt" to System.currentTimeMillis()
                )

                val confirmationMessage = Message(
                    referralId = referral.id,
                    senderId = currentUserId,
                    receiverId = referral.clientId,
                    subject = subjectPaid,
                    content = contentPaid,
                    attachmentsUrl = remoteUrls,
                    createdAt = System.currentTimeMillis()
                )

                sendPayTransaction(
                    referralId = referral.id,
                    referralUpdates = referralUpdates,
                    message = confirmationMessage,
                    clientUid = referral.clientId,
                    providerUid = referral.providerId,
                    amountPaid = amountPaid
                )

                // Navigation
                resetValues()
                popUp()
            } catch (e:Exception){
                _isLoading.value = false
                Log.e("NewMessageViewModel", "Error en el registry del Pago", e)
            }
            finally {
                _isLoading.value = false
            }
        }
    }

    fun onReasonToReject(newReason: String){
        _newMessageState.value = _newMessageState.value.copy(content = newReason)
    }

    fun onRejectReferral(subjectReject:String, popUp: () -> Unit){
        launchCatching {
            try {
                _isLoading.value = true
                val referral = _referralState.value

                val remoteUrls = _localFiles.value.mapIndexed { index, localUriString ->
                    async {
                        val uri = localUriString.toUri()
                        val originalName = getFileNameFromUri(localUriString)
                        val extension = getExtensionFromUri(context.contentResolver, uri)
                        val timestamp = System.currentTimeMillis()

                        val remotePath = "messages/${referral.id}/${originalName}_${timestamp}_$index.$extension"
                        uploadFile(localUriString, remotePath)
                    }
                }.awaitAll()

                val updates = mapOf(
                    "status" to ReferralStatus.REJECTED.name,
                    "updatedAt" to System.currentTimeMillis()
                )

                val message = _newMessageState.value.copy(
                    referralId = referral.id,
                    senderId = currentUserId,
                    receiverId = referral.clientId,
                    subject = subjectReject,
                    attachmentsUrl = remoteUrls,
                    createdAt = System.currentTimeMillis()
                )

                rejectReferralTransaction(
                    referralId = referral.id,
                    referralUpdates = updates,
                    message = message,
                    providerUid = referral.providerId
                )

                // Navigation
                popUp()
            } catch (e:Exception){
                Log.e("NewMessageViewModel", "Error al rechazar el Referido", e)
            } finally {
            _isLoading.value = false
            }
        }
    }

}
