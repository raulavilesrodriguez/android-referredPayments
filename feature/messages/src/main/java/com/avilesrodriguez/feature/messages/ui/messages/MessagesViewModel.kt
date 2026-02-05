package com.avilesrodriguez.feature.messages.ui.messages

import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetMessagesByReferral
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.MarkAsDeleteBySenderMessage
import com.avilesrodriguez.domain.usecases.MarkAsDeletedByReceiverMessage
import com.avilesrodriguez.domain.usecases.MarkAsReadMessage
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralById: GetReferralById,
    private val getMessagesByReferral: GetMessagesByReferral,
    private val markAsRead: MarkAsReadMessage,
    private val markAsDeletedBySender: MarkAsDeleteBySenderMessage,
    private val markAsDeletedByReceiver: MarkAsDeletedByReceiverMessage
): BaseViewModel() {

}