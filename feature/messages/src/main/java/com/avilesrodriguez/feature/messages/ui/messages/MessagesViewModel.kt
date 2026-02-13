package com.avilesrodriguez.feature.messages.ui.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetMessagesByReferral
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.MarkAsDeleteBySenderMessage
import com.avilesrodriguez.domain.usecases.MarkAsDeletedByReceiverMessage
import com.avilesrodriguez.domain.usecases.MarkAsReadMessage
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralById: GetReferralById,
    private val getMessagesByReferral: GetMessagesByReferral,
    private val markAsReadMessage: MarkAsReadMessage,
    private val markAsDeletedBySenderMessage: MarkAsDeleteBySenderMessage,
    private val markAsDeletedByReceiverMessage: MarkAsDeletedByReceiverMessage
): BaseViewModel() {

    private val _allMessages = MutableStateFlow<List<Message>>(emptyList())
    private val _uiState = MutableStateFlow<List<Message>>(emptyList())
    val uiState: StateFlow<List<Message>> = _uiState.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    private var referralJob: Job? = null
    private var messagesJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()


    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
            _searchText
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    filterMessagesLocally(query)
                }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
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
                fetchAllMessages(referralId)
            } else {
                _referralState.value = Referral()
            }
        }
    }

    private fun fetchAllMessages(referralId: String){
        _isLoading.value = true
        messagesJob?.cancel()
        messagesJob = launchCatching {
            getMessagesByReferral(referralId)
                .collect { messages ->
                    val filteredByDeletion = messages.filter { msg ->
                        if (msg.senderId == currentUserId) !msg.isDeletedBySender
                        else !msg.isDeletedByReceiver
                    }
                    _allMessages.value = filteredByDeletion
                    filterMessagesLocally(_searchText.value)
                    _isLoading.value = false
                }
        }
    }

    private fun filterMessagesLocally(query: String) {
        val queryNormalized = query.normalizeName()
        val allMessages = _allMessages.value

        _uiState.value = if (queryNormalized.isEmpty()) {
             allMessages
        } else {
            // Busca en cualquier parte del nombre (contains) normalizando el subject
            allMessages.filter { message ->
                message.subject.normalizeName().contains(queryNormalized)
            }
        }
    }
    
    fun onDeleteMessage(message: Message){
        launchCatching { 
            if(message.senderId == currentUserId){
                markAsDeletedBySenderMessage(message.id)
            }
            if(message.receiverId == currentUserId){
                markAsDeletedByReceiverMessage(message.id)
            }
        }
    }

    fun onNewMessageClick(openScreen: (String) -> Unit){
        val referral = referralState.value
        openScreen(NavRoutes.NEW_MESSAGE.replace("{${NavRoutes.ReferralArgs.ID}}", referral.id))
    }

    fun onMessageClick(message: Message, openScreen: (String) -> Unit){
        launchCatching {
            markAsReadMessage(message.id)
        }
        openScreen(NavRoutes.MESSAGE_SCREEN.replace("{${NavRoutes.MessageArgs.ID}}", message.id))
    }

}