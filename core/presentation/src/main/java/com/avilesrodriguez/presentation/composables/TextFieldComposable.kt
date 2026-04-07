package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.validationRules.ValidationRules
import com.avilesrodriguez.presentation.R

@Composable
fun NameField(
    value: String,
    onNewValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes namePlaceholder: Int,) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if(it.length <= ValidationRules.MAX_LENGTH_NAME)
                onNewValue(it)
                            },
            shape = RoundedCornerShape(16.dp),
            placeholder = {Text(text = stringResource(namePlaceholder))},
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Email",
                    tint = MaterialTheme.colorScheme.primary
                )},
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )
        Text(
            text = "${value.length}/${ValidationRules.MAX_LENGTH_NAME}",
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
    modifier: Modifier = Modifier
){
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
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
            leadingIcon = {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )},
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )
        Text(
            text = "${value.length}/$maxLength",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
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
                if (newValue.text.length <= ValidationRules.MAX_LENGTH_NAME) {
                    textFieldValue = newValue
                    onNewValue(newValue.text)
                }
            },
            label = {Text(text = stringResource(R.string.label_name))},
            shape = RoundedCornerShape(16.dp),
            placeholder = {Text(text = stringResource(R.string.placeholder_name))},
            singleLine = true,
            modifier = Modifier
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
        Text(
            text = "${textFieldValue.text.length}/${ValidationRules.MAX_LENGTH_NAME}",
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
        placeholder = {Text(text=stringResource(R.string.email))},
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = MaterialTheme.colorScheme.primary
            )},
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}

@Composable
fun EmailFieldCursor(
    value: String,
    onNewValue: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length) //selection cursor
            )
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onNewValue(newValue.text)
        },
        label = { Text(text = stringResource(R.string.email)) },
        placeholder = { Text(text = stringResource(R.string.email)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )},
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.focusRequester(focusRequester),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}

@Composable
fun PhoneField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = { if(it.length <= ValidationRules.MIN_PASS_LENGTH_PHONE_ECUADOR) onNewValue(it) },
            placeholder = { Text(stringResource(R.string.mobile_number))},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Phone",
                    tint = MaterialTheme.colorScheme.primary
                )},
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )
        Text(
            text = "${value.length}/${ValidationRules.MIN_PASS_LENGTH_PHONE_ECUADOR}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun PhoneFieldCursor(
    value: String,
    onNewValue: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
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
                val filteredText = newValue.text.filter { it.isDigit() }
                if (filteredText.length <= ValidationRules.MIN_PASS_LENGTH_PHONE_ECUADOR) {
                    textFieldValue = newValue.copy(text = filteredText)
                    onNewValue(filteredText)
                }
            },
            label = { Text(text = stringResource(R.string.mobile_number)) },
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(text = stringResource(R.string.mobile_number)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Phone",
                    tint = MaterialTheme.colorScheme.primary
                )},
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
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
        Text(
            text = "${textFieldValue.text.length}/${ValidationRules.MIN_PASS_LENGTH_PHONE_ECUADOR}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
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
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = MaterialTheme.colorScheme.primary
            )},
        trailingIcon = {
            IconButton(
                onClick = { isVisible = !isVisible }
            ) {
                Icon(painter = icon, contentDescription = "Visibility")
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}

@Composable
fun FieldForm(
    value: String,
    onNewValue: (String) -> Unit,
    namePlaceholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    modifierHeight: Modifier = Modifier,
    ruleField: Int? = null
    ) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onNewValue(it) },
            shape = RoundedCornerShape(16.dp),
            placeholder = {
                Text(
                    text = namePlaceholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )},
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .then(modifierHeight),
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )},
            textStyle = LocalTextStyle.current.copy(
                textAlign = if(ruleField != null) TextAlign.Justify else TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )
        if(ruleField != null)
        Text(
            text = "${value.length}/${ruleField}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
}
