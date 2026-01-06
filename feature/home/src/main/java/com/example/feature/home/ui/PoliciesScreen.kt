package com.example.feature.home.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.ProfileToolBar

@Composable
fun PoliciesScreen(
    popUp: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.policies,
                backClick = popUp
            )
        },
        content = { innerPadding ->
            PoliciesScreenContent(
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun PoliciesScreenContent(
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.policies_title),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.policies_intro),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        PolicySection(
            title = R.string.policies_section_1_title,
            body = R.string.policies_section_1_body
        )

        Text(
            text = stringResource(R.string.policies_section_1_example),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )

        PolicySection(
            title = R.string.policies_section_2_title,
            body = R.string.policies_section_2_body
        )

        PolicySection(
            title = R.string.policies_section_3_title,
            body = R.string.policies_section_3_body
        )

        PolicySection(
            title = R.string.policies_section_4_title,
            body = R.string.policies_section_4_body
        )

        PolicySection(
            title = R.string.policies_section_5_title,
            body = R.string.policies_section_5_body
        )

    }
}

@Composable
private fun PolicySection(
    @StringRes title: Int,
    @StringRes body: Int
) {
    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = stringResource(body),
        style = MaterialTheme.typography.bodyMedium
    )
}