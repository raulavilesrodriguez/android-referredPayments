package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.FullSearch
import com.avilesrodriguez.presentation.ext.options
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun ReferralsList(
    searchText: String,
    updateSearchText: (String) -> Unit,
    selectedStatus: Int?,
    filterReferralsByStatus: (Int) -> Unit,
    onReferralClick: (String) -> Unit,
    isLoading: Boolean,
    onLoadMoreReferrals: () -> Unit,
    isPaginationActive: Boolean,
    showButton: Boolean,
    referralsRealTime: List<Referral>,
    referrals: List<Referral>,
    onViewMoreReferrals: () -> Unit,
    onViewRealReferrals: () -> Unit,
    modifier: Modifier = Modifier
){
    val statusOptions = ReferralStatus.options(true)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // boton scroll to top
    val showScrollToTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2
        }
    }

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
                    onLoadMoreReferrals()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()){
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            item{
                FullSearch(
                    options = statusOptions,
                    selectedOption = selectedStatus?:R.string.all_status,
                    onClick = filterReferralsByStatus,
                    value = searchText,
                    onValueChange = updateSearchText,
                    placeholder = R.string.search,
                    trailingIcon = R.drawable.search,
                    modifier = Modifier
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            if(!isPaginationActive){
                if(referralsRealTime.isNotEmpty()){
                    items(
                        referralsRealTime,
                        key = { it.id }
                    ){referral ->
                        ReferralItem(referral = referral, onReferralClick = onReferralClick, isRealTime = true)
                    }
                    if(showButton){
                        item {
                            TextButton(
                                onClick = {onViewMoreReferrals()}
                            ) {
                                Text(text = stringResource(R.string.view_more_referrals))
                                Icon(imageVector = Icons.Default.DoubleArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                } else if(!isLoading){
                    item{
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_have_referreds),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (isLoading && referralsRealTime.isNotEmpty()) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }else{
                if(referrals.isNotEmpty()){
                    item{
                        TextButton(
                            onClick = {onViewRealReferrals()}
                        ) {
                            Text(text= stringResource(R.string.view_recent_referrals))
                            Icon(imageVector = Icons.Default.DoubleArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    items(
                        referrals,
                        key = { it.id }
                    ){referral ->
                        ReferralItem(referral = referral, onReferralClick = onReferralClick, isRealTime = false)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                } else if(!isLoading){
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_have_referreds),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if(isLoading && referrals.isNotEmpty()){
                    item{
                        Box(Modifier
                            .fillMaxWidth()
                            .height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showScrollToTopButton,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Go Up",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}