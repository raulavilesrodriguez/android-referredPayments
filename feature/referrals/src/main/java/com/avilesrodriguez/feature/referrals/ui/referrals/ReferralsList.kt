package com.avilesrodriguez.feature.referrals.ui.referrals

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.MenuDropdownBox
import com.avilesrodriguez.presentation.ext.options
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ReferralsList(
    selectedStatus: Int?,
    filterReferralsByStatus: (Int) -> Unit,
    onReferralClick: (Referral) -> Unit,
    referrals: List<ReferralWithNames>,
    user: UserData?,
    isLoading: Boolean,
    onLoadMoreReferrals: () -> Unit,
    modifier: Modifier = Modifier
){
    val statusOptions = ReferralStatus.options(true)
    val listState = rememberLazyListState()

    // Detectamos si el usuario tiene el dedo en la pantalla moviendo la lista
    val isDragged by listState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val atBottom = !listState.canScrollForward  //ya no hay más contenido abajo → estoy en el final
            isDragged && atBottom
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if(shouldLoad && !isLoading && referrals.isNotEmpty()){
                    onLoadMoreReferrals()
                }
            }
    }

    // Scroll automático al inicio solo ante nuevos referidos principales (no por paginación)
    LaunchedEffect(referrals.firstOrNull()?.referral?.id) {
        if (referrals.isNotEmpty() && listState.firstVisibleItemIndex <= 2) {
            listState.animateScrollToItem(index = 0)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        state = listState
    ) {
        item{
            //To suppress focus
            Box(
                Modifier
                    .size(0.dp)
                    .focusable()
            )
        }
        item{
            MenuDropdownBox(
                options = statusOptions,
                selectedOption = selectedStatus?:R.string.all_status,
                onClick = filterReferralsByStatus,
                title = R.string.filter_by_status,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 4.dp)
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        if(referrals.isNotEmpty()){
            items(
                referrals,
                key = { it.referral.id }
            ){ item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onReferralClick(item.referral) }
                ){
                    ReferralItem(
                        referral = item.referral,
                        otherPartyName = item.otherPartyName,
                        user = user
                    )
                }
            }
        }else if(!isLoading){
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

        if (isLoading && referrals.isNotEmpty()) {
            item {
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