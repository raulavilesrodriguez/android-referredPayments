package com.avilesrodriguez.feature.messages.ui.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.user.UserData

@Composable
fun MessagesList(
    onMessageClick: (Message) -> Unit,
    messages: List<Message>,
    currentUserId: String,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(messages){ message ->
            MessageItem(
                message = message,
                currentUserId = currentUserId,
                onClick = { onMessageClick(message) },
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived
            )
        }

    }
}