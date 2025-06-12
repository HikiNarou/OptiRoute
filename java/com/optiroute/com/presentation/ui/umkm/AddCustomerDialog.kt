package com.optiroute.com.presentation.ui.umkm

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.optiroute.com.domain.models.CustomerPriority
import com.optiroute.com.domain.models.Location

data class CustomerFormData(
    val name: String,
    val location: Location,
    val phoneNumber: String,
    val email: String,
    val itemType: String,
    val itemWeight: Double,
    val weightUnit: String,
    val notes: String,
    val priority: CustomerPriority
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (CustomerFormData) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("") }
    var itemWeight by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf("kg") }
    var notes by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(CustomerPriority.NORMAL) }

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
                    text = "Add New Customer",
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
                    // Customer Information Section
                    Text(
                        text = "Customer Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Divider()

                    // Location Section
                    Text(
                        text = "Location Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("Latitude") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("Longitude") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Divider()

                    // Package Information Section
                    Text(
                        text = "Package Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = itemType,
                        onValueChange = { itemType = it },
                        label = { Text("Item Type") },
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = itemWeight,
                            onValueChange = { itemWeight = it },
                            label = { Text("Weight") },
                            leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )

                        var expanded by remember { mutableStateOf(false) }
                        val weightUnits = listOf("kg", "g", "ton")

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = weightUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                weightUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            weightUnit = unit
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Priority Selection
                    Text(
                        text = "Priority Level",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    CustomerPriority.values().forEach { priorityOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = priority == priorityOption,
                                    onClick = { priority = priorityOption },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = priority == priorityOption,
                                onClick = { priority = priorityOption }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = priorityOption.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = when (priorityOption) {
                                        CustomerPriority.LOW -> "Standard delivery"
                                        CustomerPriority.NORMAL -> "Regular priority"
                                        CustomerPriority.HIGH -> "Priority delivery"
                                        CustomerPriority.URGENT -> "Urgent delivery"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional Notes (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                            val lat = latitude.toDoubleOrNull() ?: 0.0
                            val lng = longitude.toDoubleOrNull() ?: 0.0
                            val weight = itemWeight.toDoubleOrNull() ?: 0.0

                            val customerData = CustomerFormData(
                                name = name,
                                location = Location(lat, lng, address),
                                phoneNumber = phoneNumber,
                                email = email,
                                itemType = itemType,
                                itemWeight = weight,
                                weightUnit = weightUnit,
                                notes = notes,
                                priority = priority
                            )
                            onConfirm(customerData)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading &&
                                name.isNotBlank() &&
                                address.isNotBlank() &&
                                itemType.isNotBlank() &&
                                itemWeight.isNotBlank() &&
                                latitude.isNotBlank() &&
                                longitude.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Add Customer")
                        }
                    }
                }
            }
        }
    }
}