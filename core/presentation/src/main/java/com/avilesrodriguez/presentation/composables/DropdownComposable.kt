package com.avilesrodriguez.presentation.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DropdownContextMenu(
    options: List<Int>,
    modifier: Modifier = Modifier,
    onActionClick: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier){
        IconButton(onClick = {isExpanded = true}) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Menu"
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.width(180.dp)
        ){
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {Text(text = stringResource(selectionOption))},
                    onClick = {
                        isExpanded = false
                        onActionClick(selectionOption)
                    }
                )
            }
        }
    }
}