package com.avilesrodriguez.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.avilesrodriguez.presentation.R

data class StartListTab(
    @param:StringRes val title: Int,
    val icon: ImageVector
)

fun generateTabs(): List<StartListTab> {
    return listOf(
        StartListTab(R.string.referrals, Icons.Default.AddTask),
        StartListTab(R.string.start, Icons.Default.Home),
        StartListTab(R.string.settings, Icons.Default.Person)
    )
}