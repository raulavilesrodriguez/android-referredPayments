package com.avilesrodriguez.feature.messages.ui.messages

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun MessagesList(
    searchText: String,
    onValueChange: (String) -> Unit,
    onMessageClick: (Message) -> Unit,
    messages: List<Message>,
    currentUserId: String,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    loadMoreMessages: () -> Unit,
    isLoading: Boolean,
    listState: LazyListState
){
    // Detectamos si el usuario tiene el dedo en la pantalla moviendo la lista
    val isDragged by listState.interactionSource.collectIsDraggedAsState()

    val focusManager = LocalFocusManager.current

    LaunchedEffect(isDragged) {
        if (isDragged) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val atBottom = !listState.canScrollForward  //ya no hay más contenido abajo → estoy en el final
            isDragged && atBottom
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if(shouldLoad){
                    loadMoreMessages()
                }
            }
    }

    Box(modifier = Modifier.fillMaxWidth()){
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            item {
                SearchFieldBasic(
                    value = searchText,
                    onValueChange = onValueChange,
                    placeholder = R.string.search_in_mail,
                    trailingIcon = R.drawable.search,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
            if(messages.isNotEmpty()){
                items(
                    messages,
                    key = { message -> message.id }
                ){ message ->
                    MessageItem(
                        message = message,
                        currentUserId = currentUserId,
                        onClick = { onMessageClick(message) },
                        clientWhoReferred = clientWhoReferred,
                        providerThatReceived = providerThatReceived
                    )
                }
            } else if(!isLoading){
                item {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text= stringResource(R.string.no_have_emails))
                    }
                }
            }

            if(isLoading && messages.isNotEmpty()){
                item {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}