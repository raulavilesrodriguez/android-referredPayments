package com.avilesrodriguez.presentation.fakeData

import com.avilesrodriguez.domain.model.message.Message

val message1 = Message(
    id = "m1",
    referralId = "2r",
    senderId = "1u",
    receiverId = "2u",
    subject = "In Process to contact Referred",
    content = "In Process to contact Referred Juana Liceo, with mobile number: 0999654321, email: uana.liceo@petstore.com" +
            "referred by: Brayan Muelas",
    attachmentsUrl = emptyList(),
    createdAt = System.currentTimeMillis(),
    isRead = false,
    isDeletedBySender = false,
    isDeletedByReceiver = false
)