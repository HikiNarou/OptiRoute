package com.optiroute.com.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType
import com.optiroute.com.presentation.viewmodel.AuthViewModel
import com.optiroute.com.presentation.viewmodel.ProfileEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileEditViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()

    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswordSection by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            username = user.username
            email = user.email
            fullName = user.fullName
        }
    }

    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileEditViewModel.ProfileState.Success -> {
                onNavigateBack()
                profileViewModel.clearState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Edit Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        currentUser?.let { user ->
                            Text(
                                text = "${user.userType.name} Account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            currentUser?.let { user ->
                                val updatedUser = user.copy(
                                    username = username,
                                    email = email,
                                    fullName = fullName
                                )

                                if (showPasswordSection && newPassword.isNotEmpty()) {
                                    profileViewModel.updateProfileWithPassword(
                                        user = updatedUser,
                                        currentPassword = currentPassword,
                                        newPassword = newPassword
                                    )
                                } else {
                                    profileViewModel.updateProfile(updatedUser)
                                }
                            }
                        },
                        enabled = profileState !is ProfileEditViewModel.ProfileState.Loading
                    ) {
                        if (profileState is ProfileEditViewModel.ProfileState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Information Section
            ProfileSection(
                title = "Profile Information",
                icon = Icons.Default.Person
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.lowercase().replace(" ", "") },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Account Type Section (Read-only)
            ProfileSection(
                title = "Account Information",
                icon = Icons.Default.Info
            ) {
                currentUser?.let { user ->
                    OutlinedTextField(
                        value = user.userType.name,
                        onValueChange = { },
                        label = { Text("Account Type") },
                        leadingIcon = {
                            Icon(
                                when (user.userType) {
                                    UserType.UMKM -> Icons.Default.Store
                                    UserType.ADMIN -> Icons.Default.AdminPanelSettings
                                    UserType.KURIR -> Icons.Default.DeliveryDining
                                },
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = if (user.isActive) "Active" else "Inactive",
                        onValueChange = { },
                        label = { Text("Account Status") },
                        leadingIcon = {
                            Icon(
                                if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = if (user.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = if (user.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Password Change Section
            ProfileSection(
                title = "Security",
                icon = Icons.Default.Security
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Switch(
                        checked = showPasswordSection,
                        onCheckedChange = {
                            showPasswordSection = it
                            if (!it) {
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        }
                    )
                }

                if (showPasswordSection) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                    )

                    if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                        Text(
                            text = "Passwords do not match",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (newPassword.isNotEmpty() && newPassword.length < 6) {
                        Text(
                            text = "Password must be at least 6 characters",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Error Display
            if (profileState is ProfileEditViewModel.ProfileState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (profileState as ProfileEditViewModel.ProfileState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    currentUser?.let { user ->
                        val updatedUser = user.copy(
                            username = username,
                            email = email,
                            fullName = fullName
                        )

                        if (showPasswordSection && newPassword.isNotEmpty()) {
                            if (newPassword == confirmPassword && newPassword.length >= 6) {
                                profileViewModel.updateProfileWithPassword(
                                    user = updatedUser,
                                    currentPassword = currentPassword,
                                    newPassword = newPassword
                                )
                            }
                        } else {
                            profileViewModel.updateProfile(updatedUser)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = profileState !is ProfileEditViewModel.ProfileState.Loading &&
                        username.isNotBlank() &&
                        email.isNotBlank() &&
                        fullName.isNotBlank() &&
                        (!showPasswordSection ||
                                (currentPassword.isNotBlank() &&
                                        newPassword.isNotBlank() &&
                                        confirmPassword.isNotBlank() &&
                                        newPassword == confirmPassword &&
                                        newPassword.length >= 6))
            ) {
                if (profileState is ProfileEditViewModel.ProfileState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            content()
        }
    }
}