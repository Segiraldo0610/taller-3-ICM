package com.example.taller3firebase.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.example.taller3firebase.R
import java.io.File

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    state: AuthFormState,
    onModeChange: (AuthMode) -> Unit,
    onNameChange: (String) -> Unit,
    onIdentificationChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPhotoCaptured: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(
                    if (state.mode == AuthMode.LOGIN) R.string.login_subtitle else R.string.register_subtitle,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))

            if (state.mode == AuthMode.REGISTER) {
                ProfilePhotoCapture(
                    modifier = Modifier,
                    photoUri = state.photoUri,
                    onPhotoCaptured = onPhotoCaptured,
                )
                Spacer(Modifier.height(16.dp))
                AuthTextField(value = state.name, onValueChange = onNameChange, labelRes = R.string.name_label)
                AuthTextField(value = state.identification, onValueChange = onIdentificationChange, labelRes = R.string.identification_label)
                AuthTextField(value = state.phone, onValueChange = onPhoneChange, labelRes = R.string.phone_label, keyboardType = KeyboardType.Phone)
            }

            AuthTextField(value = state.email, onValueChange = onEmailChange, labelRes = R.string.email_label, keyboardType = KeyboardType.Email)
            PasswordField(value = state.password, onValueChange = onPasswordChange, labelRes = R.string.password_label)
            if (state.mode == AuthMode.REGISTER) {
                PasswordField(value = state.confirmPassword, onValueChange = onConfirmPasswordChange, labelRes = R.string.confirm_password_label)
            }

            state.error?.let { message ->
                Text(
                    text = stringResource(message.stringRes()),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(if (state.mode == AuthMode.LOGIN) R.string.login_action else R.string.register_action))
                }
            }
            TextButton(
                onClick = {
                    onModeChange(if (state.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN)
                },
            ) {
                Text(stringResource(if (state.mode == AuthMode.LOGIN) R.string.go_to_register else R.string.go_to_login))
            }
        }
    }
}

@Composable
private fun AuthTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    )
}

@Composable
private fun PasswordField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(if (visible) R.string.hide_password else R.string.show_password),
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    )
}

@Composable
private fun ProfilePhotoCapture(
    modifier: Modifier = Modifier,
    photoUri: String?,
    onPhotoCaptured: (String) -> Unit,
) {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingUri?.let { onPhotoCaptured(it.toString()) }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            createCameraUri(context).also {
                pendingUri = it
                cameraLauncher.launch(it)
            }
        }
    }
    val openCamera: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createCameraUri(context).also {
                pendingUri = it
                cameraLauncher.launch(it)
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(R.string.profile_photo_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape),
            )
        }
        OutlinedButton(onClick = openCamera) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Text(
                text = stringResource(if (photoUri == null) R.string.take_profile_photo else R.string.retake_profile_photo),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

private fun createCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "profile_photos").apply { mkdirs() }
    val file = File.createTempFile("profile_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun AuthMessage.stringRes(): Int = when (this) {
    AuthMessage.NAME_REQUIRED -> R.string.error_name_required
    AuthMessage.IDENTIFICATION_REQUIRED -> R.string.error_identification_required
    AuthMessage.EMAIL_INVALID -> R.string.error_email_invalid
    AuthMessage.PASSWORD_REQUIRED -> R.string.error_password_required
    AuthMessage.PASSWORD_TOO_SHORT -> R.string.error_password_too_short
    AuthMessage.PASSWORDS_DO_NOT_MATCH -> R.string.error_passwords_do_not_match
    AuthMessage.PHONE_REQUIRED -> R.string.error_phone_required
    AuthMessage.PHOTO_REQUIRED -> R.string.error_photo_required
    AuthMessage.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
    AuthMessage.AUTHENTICATION_FAILED -> R.string.error_authentication
    AuthMessage.PROFILE_SAVE_FAILED -> R.string.error_profile_save
}

