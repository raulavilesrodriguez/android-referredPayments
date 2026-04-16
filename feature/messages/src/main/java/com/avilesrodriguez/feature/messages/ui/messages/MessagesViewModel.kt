package com.avilesrodriguez.feature.messages.ui.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.referral.GetReferralById
import com.avilesrodriguez.domain.usecases.user.GetUser
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.domain.usecases.message.GetMessagesByReferralPaged
import com.avilesrodriguez.domain.usecases.message.GetMessagesByReferralSince
import com.avilesrodriguez.domain.usecases.message.MarkAsDeleteBySenderMessage
import com.avilesrodriguez.domain.usecases.message.MarkAsDeletedByReceiverMessage
import com.avilesrodriguez.domain.usecases.message.MarkAsReadMessage
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralById: GetReferralById,
    private val markAsReadMessage: MarkAsReadMessage,
    private val markAsDeletedBySenderMessage: MarkAsDeleteBySenderMessage,
    private val markAsDeletedByReceiverMessage: MarkAsDeletedByReceiverMessage,
    private val getMessagesByReferralSince: GetMessagesByReferralSince,
    private val getMessagesByReferralPaged: GetMessagesByReferralPaged
): BaseViewModel() {
    private val _uiState = MutableStateFlow<List<Message>>(emptyList())
    val uiState: StateFlow<List<Message>> = _uiState.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var allMessagesLoaded = false
    private var lastMessageViewModel: Message? = null
    private val pageSize: Long = 20L
    private val pageSizeLoadMore: Long = 20L
    private var paginationJob: Job? = null
    private var realTimeJob: Job? = null
    private var referralJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()


    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
            launch {
                _referralState.collect { referral ->
                    if(referral.id.isNotEmpty()){
                        launch {
                            _searchText
                                .debounce(300)
                                .distinctUntilChanged()
                                .collect {
                                    lastMessageViewModel = null
                                    allMessagesLoaded = false
                                    loadInitialMessages(referral.id)
                                }
                        }
                    }
                }
            }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
        lastMessageViewModel = null
        allMessagesLoaded = false
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
                loadInitialMessages(referralId)
            } else {
                _referralState.value = Referral()
            }
        }
    }

    /**
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
                    _initialMessages.value = filteredByDeletion
                    filterMessagesLocally(_searchText.value)
                    _isLoading.value = false
                }
        }
    } */

    private fun loadInitialMessages(referralId: String){
        _isLoading.value = true
        paginationJob?.cancel()
        if (_searchText.value.isNotEmpty()) {
            realTimeJob?.cancel()
        }
        paginationJob = launchCatching {
            try {
                val(messages, lastMessage) = getMessagesByReferralPaged(
                    referralId = referralId,
                    currentUserId = currentUserId,
                    pageSize = pageSize,
                    lastMessage = null,
                    subjectPrefix = _searchText.value
                )
                _uiState.value = messages
                lastMessageViewModel = lastMessage
                allMessagesLoaded = messages.size < pageSize
                val searchText = _searchText.value
                if(searchText.isEmpty()){
                    val newestTime = messages.firstOrNull()?.createdAt ?: System.currentTimeMillis()
                    listenForNewMessages(referralId, newestTime)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForNewMessages(referralId: String, since: Long){
        realTimeJob?.cancel()
        realTimeJob = launchCatching {
            getMessagesByReferralSince(referralId, since)
                .flowOn(Dispatchers.IO)
                .collect { newMessages ->
                    val filteredByDeletion = newMessages.filter { msg ->
                        if (msg.senderId == currentUserId) !msg.isDeletedBySender
                        else !msg.isDeletedByReceiver
                    }
                    if(filteredByDeletion.isNotEmpty()){
                        val currentMessages = _uiState.value
                        val updatedList = (filteredByDeletion + currentMessages)
                            .distinctBy { it.id }
                            .sortedByDescending { it.createdAt }

                        _uiState.value = updatedList
                    }
                }
        }
    }

    fun loadMoreMessages(referralId: String) {
        if (allMessagesLoaded || paginationJob?.isActive == true || lastMessageViewModel == null) return

        _isLoading.value = true
        paginationJob = launchCatching {
            try {
                val (moreMessages, lastMessage) = getMessagesByReferralPaged(
                    referralId = referralId,
                    currentUserId = currentUserId,
                    pageSize = pageSizeLoadMore,
                    lastMessage = lastMessageViewModel,
                    subjectPrefix = _searchText.value
                )

                if (moreMessages.isNotEmpty()) {
                    val currentMessages = _uiState.value.toMutableList()
                    currentMessages.addAll(moreMessages)
                    // Evitamos duplicados y mantenemos el orden
                    _uiState.value = currentMessages.distinctBy { it.id }.sortedByDescending { it.createdAt }
                    lastMessageViewModel = lastMessage
                }
                allMessagesLoaded = moreMessages.size < pageSize
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
    private fun filterMessagesLocally(query: String) {
        val queryNormalized = query.normalizeName()
        val allMessages = _initialMessages.value

        _uiState.value = if (queryNormalized.isEmpty()) {
             allMessages
        } else {
            // Busca en cualquier parte del nombre (contains) normalizando el subject
            allMessages.filter { message ->
                message.subject.normalizeName().contains(queryNormalized)
            }
        }
    } */
    
    fun onDeleteMessage(message: Message){
        launchCatching { 
            if(message.senderId == currentUserId){
                markAsDeletedBySenderMessage(message.id)
            } else if(message.receiverId == currentUserId){
                markAsDeletedByReceiverMessage(message.id)
            }
            _uiState.value = _uiState.value.filter { it.id != message.id }
        }
    }

    fun onMessageClick(message: Message){
        if(message.receiverId == currentUserId && !message.isRead){
            launchCatching {
                markAsReadMessage(message.id)
            }
        }
    }

}