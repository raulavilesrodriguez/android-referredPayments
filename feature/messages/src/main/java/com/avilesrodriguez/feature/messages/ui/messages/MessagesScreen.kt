package com.avilesrodriguez.feature.messages.ui.messages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.feature.messages.ui.message.MessageScreen
import com.avilesrodriguez.feature.messages.ui.newMessage.NewMessage
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import kotlinx.coroutines.launch

sealed class MessagesContent{
    data object MessagesSplash: MessagesContent()
    data class NewMessage(val referralId: String): MessagesContent()
    data class MessageDetail(val messageId: String): MessagesContent()
    companion object{
        val Saver: Saver<MessagesContent?, Any> = Saver(
            save = { content ->
                when(content){
                    is MessagesSplash -> "message_splash"
                    is NewMessage -> "new_message:${content.referralId}"
                    is MessageDetail -> "message_detail:${content.messageId}"
                    null -> null
                }
            },
            restore = { value ->
                val str = value as? String ?: return@Saver null
                when {
                    str == "message_splash" -> MessagesSplash
                    str.startsWith("new_message:") -> NewMessage(str.removePrefix("new_message:"))
                    str.startsWith("message_detail:") -> MessageDetail(str.removePrefix("message_detail:"))
                    else -> null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MessagesScreen(
    referralId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: MessagesViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.loadReferralInformation(referralId.orEmpty())
    }
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val referralState by viewModel.referralState.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val coroutineScope = rememberCoroutineScope()

    val isTabletLandscape = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840) &&
            adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(480)

    val customDirective = calculatePaneScaffoldDirective(adaptiveInfo).copy(
        maxHorizontalPartitions = if (isTabletLandscape) 2 else 1,
        horizontalPartitionSpacerSize = 20.dp
    )

    val navigator = rememberListDetailPaneScaffoldNavigator<MessagesContent>(
        scaffoldDirective = customDirective
    )

    var detailContent by rememberSaveable(stateSaver = MessagesContent.Saver) {
        mutableStateOf(if(isTabletLandscape) MessagesContent.MessagesSplash else null)
    }

    val paneExpansionState = rememberPaneExpansionState()
    val isShowingBothPanels = isTabletLandscape && navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    LaunchedEffect(isShowingBothPanels) {
        if (isShowingBothPanels) {
            paneExpansionState.setFirstPaneProportion(0.5f)
        }
    }

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch { navigator.navigateBack() }
    }

    LaunchedEffect(isTabletLandscape) {
        if (isTabletLandscape) {
            // Si pasamos a tablet y no hay nada, ponemos el Splash
            if (detailContent == null) {
                detailContent = MessagesContent.MessagesSplash
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        } else {
            // Si pasamos a móvil y lo que había era el Splash, lo quitamos para ver la lista

            if (detailContent is MessagesContent.MessagesSplash) {
                detailContent = null
                if (navigator.canNavigateBack()) navigator.navigateBack()
            }
        }
    }

    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)){
        ListDetailPaneScaffold(
            modifier = Modifier.weight(1f),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            paneExpansionState = paneExpansionState,
            listPane = {
                AnimatedPane {
                    MessagesScreenContent(
                        onBackClick = onBackClick,
                        searchText = searchText,
                        onValueChange = viewModel::updateSearchText,
                        onMessageClick = { message ->
                            detailContent = MessagesContent.MessageDetail(message.id)
                            coroutineScope.launch {
                                viewModel.onMessageClick(message)
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                            }
                        },
                        messages = uiState,
                        user = user,
                        isLoading = isLoading,
                        clientWhoReferred = clientWhoReferred,
                        providerThatReceived = providerThatReceived,
                        referral = referralState,
                        onNewMessageClick = { detailContent = MessagesContent.NewMessage(referralState.id)
                            coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) } }
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    when(val content = detailContent){
                        is MessagesContent.MessagesSplash -> MessagesSplash()
                        is MessagesContent.NewMessage -> {
                            NewMessage(
                                referralId = content.referralId,
                                onBackClick = {coroutineScope.launch { navigator.navigateBack() }},
                                openScreen = openScreen,
                                showTopBar = !isShowingBothPanels
                            )
                        }
                        is MessagesContent.MessageDetail -> {
                            MessageScreen(
                                messageId = content.messageId,
                                onBackClick = {coroutineScope.launch { navigator.navigateBack() }},
                                onReplyClick = {
                                    detailContent = MessagesContent.NewMessage(referralState.id)
                                    coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                },
                                openScreen = openScreen,
                                showTopBar = !isShowingBothPanels
                            )
                        }
                        null -> {}
                    }
                }
            }
        )
    }
}

@Composable
private fun MessagesScreenContent(
    onBackClick: () -> Unit,
    searchText: String,
    onValueChange: (String) -> Unit,
    onMessageClick: (Message) -> Unit,
    messages: List<Message>,
    user: UserData?,
    isLoading: Boolean,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    referral: Referral,
    onNewMessageClick: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = stringResource(R.string.emails_referral, referral.name),
                backClick = { onBackClick() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = onNewMessageClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        content = { innerPadding ->
            InBox(
                searchText = searchText,
                onValueChange = onValueChange,
                onMessageClick = onMessageClick,
                messages = messages,
                user = user,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                isLoading = isLoading,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
private fun InBox(
    searchText: String,
    onValueChange: (String) -> Unit,
    onMessageClick: (Message) -> Unit,
    messages: List<Message>,
    user: UserData?,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier.fillMaxSize()){
        SearchFieldBasic(
            value = searchText,
            onValueChange = onValueChange,
            placeholder = R.string.search_in_mail,
            trailingIcon = R.drawable.search,
            modifier = Modifier
                .padding(16.dp)
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()){
            if(!isLoading){
                if(messages.isNotEmpty()){
                    MessagesList(
                        onMessageClick = onMessageClick,
                        messages = messages,
                        currentUserId = user?.uid?:"",
                        clientWhoReferred = clientWhoReferred,
                        providerThatReceived = providerThatReceived)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text= stringResource(R.string.no_have_emails))
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
