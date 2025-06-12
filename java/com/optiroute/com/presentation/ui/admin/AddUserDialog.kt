package com.optiroute.com.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.optiroute.com.domain.models.UserType

data class UserFormData(
    val username: String,
    val email: String,
    val fullName: String,
    val password: String,
    val userType: UserType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (UserFormData) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf(UserType.KURIR) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add New User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // User Type Selection
                    Text(
                        text = "User Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    UserType.values().forEach { userType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedUserType == userType,
                                    onClick = { selectedUserType = userType },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedUserType == userType,
                                onClick = { selectedUserType = userType }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                when (userType) {
                                    UserType.UMKM -> Icons.Default.Store
                                    UserType.ADMIN -> Icons.Default.AdminPanelSettings
                                    UserType.KURIR -> Icons.Default.DeliveryDining
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (userType) {
                                        UserType.UMKM -> "UMKM Owner"
                                        UserType.ADMIN -> "Admin Kurir"
                                        UserType.KURIR -> "Kurir"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (userType) {
                                        UserType.UMKM -> "Manage customers and track deliveries"
                                        UserType.ADMIN -> "Manage depots, vehicles, and couriers"
                                        UserType.KURIR -> "Deliver packages and optimize routes"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Divider()

                    // User Information
                    Text(
                        text = "User Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

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
                        singleLine = true,
                        placeholder = { Text("No spaces or special characters") }
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

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
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
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
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
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword
                    )

                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = "Passwords do not match",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
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
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (password == confirmPassword) {
                                val userData = UserFormData(
                                    username = username,
                                    email = email,
                                    fullName = fullName,
                                    password = password,
                                    userType = selectedUserType
                                )
                                onConfirm(userData)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading &&
                                username.isNotBlank() &&
                                email.isNotBlank() &&
                                fullName.isNotBlank() &&
                                password.isNotBlank() &&
                                confirmPassword.isNotBlank() &&
                                password == confirmPassword
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Create User")
                        }
                    }
                }
            }
        }
    }
}