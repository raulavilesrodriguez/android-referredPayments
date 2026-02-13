package com.avilesrodriguez.feature.messages.ui.message

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.GetMessageById
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MessageViewModel(
    private val getMessageById: GetMessageById,
    private val getReferralById: GetReferralById,
    private val getUser: GetUser,
): BaseViewModel() {
    private val _messageState = MutableStateFlow(Message())
    val messageState: StateFlow<Message> = _messageState.asStateFlow()
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var loadJob: Job? = null
    private var referralJob: Job? = null

    fun loadMessage(messageId: String){
        loadJob?.cancel()
        _isLoading.value = true
        loadJob = launchCatching {
            val message = getMessageById(messageId)
            if(message != null){
                _messageState.value = message
                loadReferral()
                _isLoading.value = false
            }
        }
    }

    fun loadReferral(){
        referralJob?.cancel()
        referralJob = launchCatching {
            val referralId = _messageState.value.referralId
            val referral = getReferralById(referralId)
            if(referral != null){
                val client = async { getUser(referral.clientId) }
                val provider = async { getUser(referral.providerId) }
                _referralState.value = referral
                clientWhoReferred = client.await()
                providerThatReceived = provider.await()
            }
        }
    }

    fun replyMessage(openScreen: (String) -> Unit){
        openScreen(NavRoutes.NEW_MESSAGE.replace("{${NavRoutes.ReferralArgs.ID}}", referralState.value.id))
    }

}