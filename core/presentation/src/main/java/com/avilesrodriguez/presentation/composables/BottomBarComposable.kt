package com.avilesrodriguez.presentation.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BasicBottomBar(@StringRes value: Int){
    val bottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Text(
        text = stringResource(value),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceBright)
            .padding(
                top = 4.dp,
                bottom = 4.dp  // respect controls + tu padding
            ),
        textAlign = TextAlign.Center,
        color = Color.LightGray,
        fontSize = 12.sp
    )
}