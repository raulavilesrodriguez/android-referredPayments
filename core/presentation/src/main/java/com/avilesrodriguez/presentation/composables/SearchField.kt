package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SearchFieldBasic(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholder: Int,
    @DrawableRes trailingIcon: Int,
    modifier: Modifier = Modifier
){
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        placeholder = { Text(text = stringResource(id = placeholder)) },
        trailingIcon = {
            Icon(
                painter = painterResource(id = trailingIcon),
                contentDescription = null
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSearch(
    options: List<Int>,
    selectedOption: Int,
    onClick: (Int) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholder: Int,
    @DrawableRes trailingIcon: Int,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        var expanded by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        // 1. Contenedor del Dropdown (Se adapta al texto seleccionado)
        Surface(
            modifier = Modifier
                .width(IntrinsicSize.Min) 
                .widthIn(min = 80.dp, max = 160.dp),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it } // Dejamos que el componente gestione el estado
            ) {
                // El gatillo manual compacto
                Row(
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .padding(horizontal = 12.dp, vertical = 16.dp) // Padding para igualar altura del buscador
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(selectedOption),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(4.dp))
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.widthIn(min = 200.dp) // Menú más ancho que el botón
                ) {
                    options.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(text = stringResource(selectionOption)) },
                            onClick = {
                                onClick(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // 2. Contenedor del campo Search
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text(
                        text = stringResource(id = placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                trailingIcon = {
                    Icon(painterResource(id = trailingIcon), contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
        
        LaunchedEffect(Unit) {
            focusManager.clearFocus()
        }
    }
}

@Composable
fun SearchToolBar(
    @StringRes title: Int,
    @DrawableRes iconBack: Int,
    iconBackClick: () -> Unit,
    @DrawableRes iconSearch: Int,
    iconSearchClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize()
            .padding(top = 16.dp, start = 0.dp, end = 0.dp, bottom = 16.dp)
    ){
        IconButton(
            onClick = {iconBackClick()},
            modifier = Modifier
                .padding(start = 0.dp, end = 12.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(id = iconBack),
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.80f)
        ){
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        IconButton(
            onClick = iconSearchClick,
            modifier = Modifier
                .padding(end = 4.dp)
                .align(Alignment.CenterEnd)
        ){
            Icon(
                painter = painterResource(id = iconSearch),
                contentDescription = null
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholder: Int,
    @DrawableRes leadingIcon: Int,
    onLeadingIconClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            placeholder = { Text(text = stringResource(id = placeholder)) },
            leadingIcon = {
                IconButton(onClick = onLeadingIconClick) {
                    Icon(
                        painter = painterResource(id = leadingIcon),
                        contentDescription = null
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }
}

@Composable
fun SearchToolBarNoBack(
    @StringRes title: Int,
    @DrawableRes iconSearch: Int,
    iconSearchClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize()
            .padding(top = 16.dp, start = 0.dp, end = 0.dp, bottom = 16.dp)
    ){
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.80f)
        ){
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        IconButton(
            onClick = iconSearchClick,
            modifier = Modifier
                .padding(end = 4.dp)
                .align(Alignment.CenterEnd)
        ){
            Icon(
                painter = painterResource(id = iconSearch),
                contentDescription = null
            )
        }
    }
}
