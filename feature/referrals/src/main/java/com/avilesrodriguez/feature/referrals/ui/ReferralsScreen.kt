package com.avilesrodriguez.feature.referrals.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType

@Composable
fun ReferralsScreen(
    openScreen: (String) -> Unit,
    user: UserData?,
    viewModel: ReferralViewModel = hiltViewModel()
){
    Box(modifier = Modifier.fillMaxSize()){
        if(user != null){
            user.let {
                when(it.type){
                    UserType.CLIENT -> ReferralsScreenClient()
                    UserType.PROVIDER -> ReferralsScreenProvider()
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}