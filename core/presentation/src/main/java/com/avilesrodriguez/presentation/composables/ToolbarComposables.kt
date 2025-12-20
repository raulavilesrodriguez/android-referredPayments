package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicToolbar(@StringRes title: Int) {
    TopAppBar(
        title = {
            Text(stringResource(title))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun ProfileToolBar(
    @DrawableRes iconBack: Int,
    @StringRes title: Int,
    backClick: () -> Unit,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Left
    ){
        IconButton(
            onClick = { backClick()}
        ) {
            Icon(
                painter = painterResource(id = iconBack),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxWidth(0.80f)
        )
    }
}