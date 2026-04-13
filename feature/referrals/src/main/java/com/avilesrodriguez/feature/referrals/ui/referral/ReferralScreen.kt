package com.avilesrodriguez.feature.referrals.ui.referral

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicButton
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.RatingBar
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.basicButton
import com.avilesrodriguez.presentation.ext.toColor
import com.avilesrodriguez.presentation.ext.toDisplayIcon
import com.avilesrodriguez.presentation.ext.toDisplayName
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.profile.ItemEditProfile
import com.avilesrodriguez.presentation.profile.ItemProfile
import com.avilesrodriguez.presentation.rating.RatingReason
import com.avilesrodriguez.presentation.rating.getReasonsByRating
import java.util.Locale

@Composable
fun ReferralScreen(
    referralId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    showTopBar: Boolean = true,
    viewModel: ReferralViewModel = hiltViewModel()
){
    LaunchedEffect(referralId) {
        viewModel.loadReferralInformation(referralId.orEmpty())
    }
    val referral by viewModel.referralState.collectAsState()
    val referralRating by viewModel.referralRating.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingRating by viewModel.isLoadingRating.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived
    val unReadMessages by viewModel.unReadMessages.collectAsState()

    val subjectAccept = stringResource(R.string.subject_referral_accepted)
    val contentAccept = stringResource(R.string.content_referral_accepted)


    ReferralScreenContent(
        onBackClick = onBackClick,
        referral = referral,
        user = user,
        isLoading = isLoading,
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived,
        onNameClick = { viewModel.onNameReferral(openScreen)},
        onEmailClick = { viewModel.onEmailReferral(openScreen)},
        onPhoneClick = { viewModel.onPhoneReferral(openScreen)},
        onAcceptReferral = { viewModel.onAcceptReferral(subjectAccept, contentAccept, openScreen)},
        onProcessClick = { viewModel.onProcessReferral(openScreen)},
        unReadMessages = unReadMessages.toString(),
        showTopBar = showTopBar,
        onRatingChanged = {viewModel.onRatingChanged(it)},
        referralRating = referralRating,
        onFeedbackReasonChanged = {viewModel.onFeedbackReasonChanged(it)},
        saveRatings = {viewModel.saveRatings()},
        isLoadingRating = isLoadingRating
    )
}

@Composable
fun ReferralScreenContent(
    onBackClick: () -> Unit,
    referral: Referral?,
    user: UserData?,
    isLoading: Boolean,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onNameClick: () -> Unit,
    onEmailClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onAcceptReferral: () -> Unit,
    onProcessClick: () -> Unit,
    unReadMessages: String,
    showTopBar: Boolean = true,
    onRatingChanged: (Double) -> Unit,
    referralRating: Double,
    onFeedbackReasonChanged: (String) -> Unit,
    saveRatings: () -> Unit,
    isLoadingRating: Boolean,
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if(showTopBar){
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.information_referral),
                    backClick = { onBackClick() }
                )
            }else{
                BasicToolbar(stringResource(R.string.information_referral))
            }
        },
        content = { paddingValues ->
            if(!isLoading){
                ProfileReferral(
                    referral = referral,
                    user = user,
                    clientWhoReferred = clientWhoReferred,
                    providerThatReceived = providerThatReceived,
                    onNameClick = onNameClick,
                    onEmailClick = onEmailClick,
                    onPhoneClick = onPhoneClick,
                    onAcceptReferral = onAcceptReferral,
                    onProcessClick = onProcessClick,
                    unReadMessages = unReadMessages,
                    onRatingChanged = onRatingChanged,
                    referralRating = referralRating,
                    onFeedbackReasonChanged = onFeedbackReasonChanged,
                    saveRatings = saveRatings,
                    isLoadingRating = isLoadingRating,
                    modifier = Modifier.padding(paddingValues)
                )
            } else{
                Box(Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}

@Composable
fun ProfileReferral(
    referral: Referral?,
    user: UserData?,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onNameClick: () -> Unit,
    onEmailClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onAcceptReferral: () -> Unit,
    onProcessClick: () -> Unit,
    unReadMessages: String,
    onRatingChanged: (Double) -> Unit,
    referralRating: Double,
    onFeedbackReasonChanged: (String) -> Unit,
    saveRatings: () -> Unit,
    isLoadingRating: Boolean,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isPending = referral?.status == ReferralStatus.PENDING

        when(user){
            is UserData.Client -> {
                Text(
                    text = stringResource(R.string.review_payment_process),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                if(isPending){
                    Text(
                        text = stringResource(R.string.pending_acceptance_referred),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else{
                    InBox(onProcessClick = onProcessClick, unReadMessages = unReadMessages)
                }
                if(referral != null) ReferralStatus(status = referral.status)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                ItemEditProfile(R.drawable.name, title = R.string.name_referred, data = referral?.name?:"", iconEdit = R.drawable.edit_gray){onNameClick()}
                ItemEditProfile(R.drawable.mail, title = R.string.email_referred, data = referral?.email?:"", iconEdit = R.drawable.edit_gray){onEmailClick()}
                ItemEditProfile(R.drawable.phone, title = R.string.phone_number_referred, data = referral?.numberPhone?:"", iconEdit = R.drawable.edit_gray){onPhoneClick()}
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                val nameProvider = providerThatReceived?.name?.truncate(30)?:""
                ItemProfile(R.drawable.step, title = R.string.referring, data = nameProvider)
                if(referral?.status == ReferralStatus.PAID || referral?.status == ReferralStatus.REJECTED){
                    var showFeedbackOptions by rememberSaveable { mutableStateOf(false) }
                    var selectedReason by rememberSaveable { mutableStateOf<RatingReason?>(null) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if(referralRating == 0.0){
                            Text(
                                text = stringResource(R.string.rate_experience),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingBar(
                                    rating = referral.rating,
                                    starSize = 32.dp,
                                    onRatingChanged = {
                                        onRatingChanged(it)
                                        showFeedbackOptions = true
                                        selectedReason = null
                                        onFeedbackReasonChanged("")
                                    }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f", referral.rating),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            AnimatedVisibility(visible = showFeedbackOptions) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally){
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val reasons = getReasonsByRating(referral.rating.toInt())
                                        reasons.forEach { reason ->
                                            val isSelectedReason = selectedReason == reason
                                            val newSelection = if (isSelectedReason) null else reason
                                            val reasonText = stringResource(reason.resId)
                                            SuggestionChip(
                                                onClick = {
                                                    selectedReason = newSelection
                                                    onFeedbackReasonChanged(if(newSelection != null) reasonText else "")
                                                          },
                                                label = { Text(text = stringResource(reason.resId)) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = if (isSelectedReason) MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.surfaceVariant,
                                                    labelColor = if (isSelectedReason) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                shape = RoundedCornerShape(16.dp),
                                                border = null
                                            )
                                        }
                                    }
                                }
                            }
                            Button(
                                onClick = { saveRatings() },
                                modifier = Modifier.padding(8.dp),
                                enabled = referral.rating != 0.0 && !referral.feedbackReason.isNullOrBlank()
                            ) {
                                if(isLoadingRating){
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(text = stringResource(R.string.save_rating))
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }else{
                            Text(
                                text = stringResource(R.string.rating_given),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingBar(
                                    rating = referral.rating,
                                    starSize = 32.dp,
                                    isEditable = false
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f", referral.rating),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                    }
                }
            }
            is UserData.Provider -> {
                Text(
                    text=stringResource(R.string.process_referral),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                if(isPending){
                    BasicButton(
                        text = R.string.accept_and_view_contact,
                        modifier = Modifier.basicButton()
                    ) { onAcceptReferral() }
                } else {
                    InBox(onProcessClick = onProcessClick, unReadMessages = unReadMessages)
                }
                if(referral != null) ReferralStatus(status = referral.status)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                ItemProfile(icon = R.drawable.name, title = R.string.name_referred, data = referral?.name?:"")
                ItemProfile(
                    icon = R.drawable.mail,
                    title = R.string.email_referred,
                    data = if(isPending) "••••@••••.com" else referral?.email?:""
                )
                ItemProfile(
                    icon = R.drawable.phone,
                    title = R.string.phone_number_referred,
                    data = if(isPending) "09••••••••" else referral?.numberPhone?:""
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                val nameClient = clientWhoReferred?.name?.truncate(30)?:""
                ItemProfile(R.drawable.step, title = R.string.referral, data = nameClient)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if((referral?.rating ?: 0.0) > 0.0){
                        Text(
                            text = stringResource(R.string.rating_performed, clientWhoReferred?.name?:""),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RatingBar(
                                rating = referral?.rating?:0.0,
                                starSize = 32.dp,
                                isEditable = false
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", referral?.rating?:0.0),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ReferralStatus(status: ReferralStatus){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp),
            tint = status.toColor()
        )
        Box(
            modifier = Modifier
                .background(status.toColor(), shape = RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(status.toDisplayName()),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun InBox(
    onProcessClick: () -> Unit,
    unReadMessages: String,
){
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onProcessClick() }
    ){
        Icon(
            painter = painterResource(R.drawable.email_fill),
            contentDescription = "emails_inbox",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        if(unReadMessages.toInt()>0){
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .background(
                        Color.Red,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = if(unReadMessages.toInt() > 99) "99+" else unReadMessages,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileReferralPreview(){
    MaterialTheme {
        ReferralScreenContent(
            onBackClick = {},
            referral = referral,
            user = userProvider,
            isLoading = false,
            clientWhoReferred = userClient,
            providerThatReceived = userProvider,
            onNameClick = {},
            onEmailClick = {},
            onPhoneClick = {},
            onAcceptReferral = {},
            onProcessClick = {},
            unReadMessages = "100",
            showTopBar = true,
            onRatingChanged = {},
            referralRating = 0.0,
            onFeedbackReasonChanged = {},
            saveRatings = {},
            isLoadingRating = false
        )
    }
}