package com.avilesrodriguez.feature.messages.ui.newMessage

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SaveMessage
import com.avilesrodriguez.domain.usecases.UploadFile
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
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_CONTENT
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_SUBJECT

@HiltViewModel
class NewMessageViewModel @Inject constructor(
    private val saveMessage: SaveMessage,
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralById: GetReferralById,
    private val uploadFile: UploadFile,
    @param:ApplicationContext private val context: Context
) : BaseViewModel() {
    private val _newMessageState = MutableStateFlow(Message())
    val newMessageState: StateFlow<Message> = _newMessageState.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private val _localFiles = MutableStateFlow<List<String>>(emptyList())
    val localFiles = _localFiles.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var referralJob: Job? = null
    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun loadReferralInformation(referralId: String){
        referralJob?.cancel()
        referralJob = launchCatching {
            val referral = getReferralById(referralId)
            if(referral != null){
                _referralState.value = referral
                val clientDeferred = async { getUser(referral.clientId) }
                val providerDeferred = async { getUser(referral.providerId) }
                clientWhoReferred = clientDeferred.await()
                providerThatReceived = providerDeferred.await()
            } else {
                _referralState.value = Referral()
            }
        }
    }

    fun onSubjectChange(newSubject: String){
        val filteredSubject = newSubject.take(MAX_LENGTH_SUBJECT)
        _newMessageState.value = _newMessageState.value.copy(subject = filteredSubject)
    }

    fun onContentChange(newContent: String){
        val filteredContent = newContent.take(MAX_LENGTH_CONTENT)
        _newMessageState.value = _newMessageState.value.copy(content = filteredContent)
    }

    fun onAttachFiles(uris: List<String>){
        _localFiles.value += uris
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
                    // para obtener la extensión real (.pdf, .jpg, etc.)
                    val extension = getExtensionFromUri(context.contentResolver, uri)

                    val remotePath = "messages/${referral.id}/${System.currentTimeMillis()}_$index.$extension"
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
            popUp()

        }
    }
}