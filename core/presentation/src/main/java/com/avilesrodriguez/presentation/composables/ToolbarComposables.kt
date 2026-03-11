package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val TOOLBAR_HEIGHT = 64.dp

@Composable
fun ToolBarWithIcon(
    @DrawableRes iconBack: Int,
    title: String,
    backClick: () -> Unit,
    modifier: Modifier = Modifier,
    tintIcon: Color? = null,
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TOOLBAR_HEIGHT)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Left
    ){
        IconButton(
            onClick = { backClick()}
        ) {
            Icon(
                painter = painterResource(id = iconBack),
                contentDescription = null,
                tint = tintIcon ?: LocalContentColor.current
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxWidth(0.80f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ToolbarPlaceholder(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.height(TOOLBAR_HEIGHT))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicToolbar(@StringRes title: Int, modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier.height(TOOLBAR_HEIGHT),
        title = {
            Text(
                text= stringResource(title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .fillMaxWidth(0.80f),
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarMain(title: String, options: List<Int>, onActionClick: (Int) -> Unit) {
    TopAppBar(
        modifier = Modifier.height(TOOLBAR_HEIGHT),
        title = {
            Text(
                text= title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
                )
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        actions = {
            DropdownContextMenu(options = options) { action -> onActionClick(action) }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    )
}
