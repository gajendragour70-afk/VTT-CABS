package com.vttcabs.customer.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
    contacts: List<EmergencyContact> = listOf(
        EmergencyContact("1", "John Doe", "9876543210", "Father"),
        EmergencyContact("2", "Jane Doe", "9876543211", "Mother")
    ),
    onAddContact: () -> Unit = {},
    onEditContact: (EmergencyContact) -> Unit = {},
    onDeleteContact: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf<EmergencyContact?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Emergency contacts will be notified in case of any safety concern during your trip.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Emergency Numbers
            item {
                Text(
                    text = "Emergency Helplines",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                EmergencyHelplineCard(
                    name = "Police",
                    phone = "100",
                    icon = Icons.Default.LocalPolice
                )
            }

            item {
                EmergencyHelplineCard(
                    name = "Ambulance",
                    phone = "108",
                    icon = Icons.Default.LocalHospital
                )
            }

            item {
                EmergencyHelplineCard(
                    name = "VTT CABS Support",
                    phone = "1800-XXX-XXXX",
                    icon = Icons.Default.Support
                )
            }

            // My Contacts Header
            item {
                Text(
                    text = "My Emergency Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (contacts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No emergency contacts added",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onAddContact) {
                                Text("Add Contact")
                            }
                        }
                    }
                }
            }

            items(contacts) { contact ->
                ContactCard(
                    contact = contact,
                    onEdit = { onEditContact(contact) },
                    onDelete = { showDeleteDialog = contact }
                )
            }

            // Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { contact ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to remove ${contact.name} from your emergency contacts?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteContact(contact.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmergencyHelplineCard(
    name: String,
    phone: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = phone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FilledTonalButton(onClick = { }) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call")
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = contact.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = contact.phone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = contact.relation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
