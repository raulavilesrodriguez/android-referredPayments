package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholder: Int,
    @DrawableRes leadingIcon: Int,
    onLeadingIconClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(modifier = modifier
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
                .padding(start= 0.dp, end = 12.dp)
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