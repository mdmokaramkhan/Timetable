package com.mukrram.timetable.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TimetableTopAppBar(
                titleText = "Create account",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to sign in",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpacing.md),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(84.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAddAlt1,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(AppSpacing.lg),
                )
            }
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Set up your access in under a minute. You can update details later from profile settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "The first account becomes admin. Later sign-ups become faculty unless linked to a faculty record.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(AppSpacing.md),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            AppOutlinedTextField(
                value = uiState.registerUsername,
                onValueChange = viewModel::setRegisterUsername,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Username") },
                enabled = !uiState.registerLoading,
                supportingText = {
                    Text("Use lowercase letters/numbers (minimum 3 characters).")
                },
            )
            AppOutlinedTextField(
                value = uiState.registerPassword,
                onValueChange = viewModel::setRegisterPassword,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Password") },
                enabled = !uiState.registerLoading,
                visualTransformation = if (uiState.registerPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = viewModel::toggleRegisterPasswordVisible) {
                        Icon(
                            if (uiState.registerPasswordVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = "Toggle password visibility",
                        )
                    }
                },
            )
            AppOutlinedTextField(
                value = uiState.registerConfirmPassword,
                onValueChange = viewModel::setRegisterConfirmPassword,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Confirm password") },
                enabled = !uiState.registerLoading,
                visualTransformation = if (uiState.registerPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            AppOutlinedTextField(
                value = uiState.registerFacultyId,
                onValueChange = viewModel::setRegisterFacultyId,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Faculty ID (optional)") },
                enabled = !uiState.registerLoading,
                supportingText = {
                    Text("Paste the faculty MongoDB ID from Manage > Faculty if needed.")
                },
            )

            uiState.registerError?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            AppButton(
                onClick = viewModel::register,
                enabled = !uiState.registerLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (uiState.registerLoading) "Creating your account..." else "Create account",
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextButton(
                onClick = { navController.popBackStack() },
                enabled = !uiState.registerLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(AppSpacing.sm))
                Text("Already have an account? Sign in")
            }
        }
    }
}
