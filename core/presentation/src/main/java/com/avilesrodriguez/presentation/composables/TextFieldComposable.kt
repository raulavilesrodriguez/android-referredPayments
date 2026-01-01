package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME

@Composable
fun NameField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if(it.length <= MAX_LENGTH_NAME)
                onNewValue(it)
                            },
            shape = RoundedCornerShape(16.dp),
            placeholder = {Text(text = stringResource(R.string.placeholder_name))},
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Email") }
        )
        Text(
            text = "${value.length}/$MAX_LENGTH_NAME",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun TextFieldProfile(
    value: String,
    onNewValue: (String) -> Unit,
    maxLength: Int,
    @DrawableRes icon: Int,
    @StringRes title: Int,
    modifier: Modifier = Modifier){
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if(it.length <= maxLength)
                    onNewValue(it)
            },
            label = {Text(text = stringResource(title))},
            shape = RoundedCornerShape(16.dp),
            placeholder = {Text(text = stringResource(title))},
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(painter = painterResource(icon), contentDescription = "Email") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
        Text(
            text = "${value.length}/$maxLength",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun NameTextFieldCursor(
    value: String,
    onNewValue: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length) // selection cursor
            )
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (newValue.text.length <= MAX_LENGTH_NAME) {
                    textFieldValue = newValue
                    onNewValue(newValue.text)
                }
            },
            label = {Text(text = stringResource(R.string.label_name))},
            shape = RoundedCornerShape(16.dp),
            placeholder = {Text(text = stringResource(R.string.placeholder_name))},
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        Text(
            text = "${textFieldValue.text.length}/$MAX_LENGTH_NAME",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun EmailField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(R.string.email)) },
        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PasswordField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier) {
    PasswordField(value, R.string.password, onNewValue, modifier)
}

@Composable
fun RepeatPasswordField(
    value: String,
    onNewValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PasswordField(value, R.string.repeat_password, onNewValue, modifier)
}

@Composable
private fun PasswordField(
    value: String,
    @StringRes placeholder: Int,
    onNewValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    val icon =
        if (isVisible) painterResource(R.drawable.visibility)
        else painterResource(R.drawable.visibility_off)

    val visualTransformation =
        if (isVisible) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text(text = stringResource(placeholder)) },
        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock") },
        trailingIcon = {
            IconButton(
                onClick = { isVisible = !isVisible }
            ) {
                Icon(painter = icon, contentDescription = "Visibility")
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation
    )
}