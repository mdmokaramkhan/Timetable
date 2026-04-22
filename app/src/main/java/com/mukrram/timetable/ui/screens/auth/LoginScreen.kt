package com.mukrram.timetable.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.navigation.AuthRoutes
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                modifier = Modifier.size(100.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppSpacing.xl),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .size(36.dp)
                    .padding(top = AppSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(AppSpacing.sm),
                )
            }
        }
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Sign in to manage schedules or quickly check your upcoming classes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = AppSpacing.sm),
        )
        Text(
            text = "Admins and faculty use the same login. We will route you to the right home screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = AppSpacing.md),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

        AppOutlinedTextField(
            value = uiState.loginUsername,
            onValueChange = viewModel::setLoginUsername,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Username") },
            enabled = !uiState.loginLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        Spacer(Modifier.height(AppSpacing.md))
        AppOutlinedTextField(
            value = uiState.loginPassword,
            onValueChange = viewModel::setLoginPassword,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Password") },
            enabled = !uiState.loginLoading,
            visualTransformation = if (uiState.loginPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.login()
                },
            ),
            trailingIcon = {
                val desc = if (uiState.loginPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = viewModel::toggleLoginPasswordVisible) {
                    Icon(
                        imageVector = if (uiState.loginPasswordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = desc,
                    )
                }
            },
        )
        uiState.loginError?.let { err ->
            Spacer(Modifier.height(AppSpacing.md))
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(AppSpacing.xl))
        AppButton(
            onClick = viewModel::login,
            enabled = !uiState.loginLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (uiState.loginLoading) "Signing you in..." else "Sign in",
                fontWeight = FontWeight.SemiBold,
            )
        }
        TextButton(
            onClick = { navController.navigate(AuthRoutes.Register) },
            enabled = !uiState.loginLoading,
            modifier = Modifier.padding(top = AppSpacing.sm),
        ) {
            Text("New here? Create an account")
        }
    }
}
