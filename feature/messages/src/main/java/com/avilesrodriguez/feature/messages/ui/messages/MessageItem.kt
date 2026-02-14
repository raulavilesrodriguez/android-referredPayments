package com.avilesrodriguez.feature.messages.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Outbound
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.fakeData.message1
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.time.formatTimestamp

@Composable
fun MessageItem(
    message: Message,
    currentUserId: String,
    onClick: () -> Unit,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?
) {
    //  Solo es "Unread" si YO soy quien lo recibe y no lo he leído
    val isUnreadForMe = !message.isRead && message.receiverId == currentUserId
    val isSentByMe = message.senderId == currentUserId
    val nameClientProvider = if(clientWhoReferred?.uid == currentUserId)  providerThatReceived?.name?:"" else clientWhoReferred?.name?:""

    val fontWeight = if (isUnreadForMe) FontWeight.Bold else FontWeight.Normal
    val textColor = if (isUnreadForMe)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor = if (isUnreadForMe)
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = if (isSentByMe) Icons.AutoMirrored.Filled.Outbound else Icons.Default.MoveToInbox,
            contentDescription = null,
            tint = if (isSentByMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSentByMe) stringResource(R.string.you) else nameClientProvider,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSentByMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTimestamp(message.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = fontWeight
                )
            }
            Text(
                text = message.subject,
                fontWeight = fontWeight,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = message.content,
                fontWeight = if (isUnreadForMe) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageItemPreview(){
    MaterialTheme {
        MessageItem(
            message = message1,
            currentUserId = "2u",
            onClick = {},
            clientWhoReferred = userClient,
            providerThatReceived = userProvider
        )
    }
}