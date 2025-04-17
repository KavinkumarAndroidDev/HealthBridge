package com.kkdev.healthbridge.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.or
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

@Composable
fun MessagingScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    var selectedTab by remember { mutableStateOf("Inbox") }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = if (selectedTab == "Inbox") 1 else 0) {
            Tab(
                selected = selectedTab == "New Message",
                onClick = { selectedTab = "New Message" },
                text = { Text("New Message") }
            )
            Tab(
                selected = selectedTab == "Inbox",
                onClick = { selectedTab = "Inbox" },
                text = { Text("Inbox") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            "New Message" -> NewMessageForm(navController,uid.toString())
            "Inbox" -> Inbox(uid.toString(), navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageForm(navController: NavController,patientId: String) {
    var recipient by remember { mutableStateOf<String?>(null) }
    var subject by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf("") }
    var messageSentStatus by remember { mutableStateOf("") }

    // Placeholder data (replace with real data)
    val recipients = remember { listOf("Admin", "Dr. Raghav", "Dr. Other") } // Get from DB
    val subjects = remember { listOf("Reschedule", "Inquiry", "Others") }

    var expandedRecipient by remember { mutableStateOf(false) }
    var expandedSubject by remember { mutableStateOf(false) }


    fun sendMessage() {
        if (recipient != null && subject != null && messageText.isNotEmpty()) {
            val message = Message(
                senderId = patientId,
                recipient = recipient!!,
                subject = subject!!,
                message = messageText,
                timestamp = Date()
            )
            // Send to Firestore
            FirebaseFirestore.getInstance().collection("messages").add(message)
                .addOnSuccessListener {
                    messageSentStatus = "Message sent successfully!"
                    recipient = null
                    subject = null
                    messageText = ""
                }
                .addOnFailureListener { e ->
                    messageSentStatus = "Error sending message: ${e.message}"
                }
        } else {
            messageSentStatus = "Please fill all fields."
        }
    }

    Column(Modifier.padding(16.dp)) {
        // Recipient Dropdown
        DropdownField(
            label = "Send To:",
            value = recipient,
            items = recipients,
            expanded = expandedRecipient,
            onExpandedChange = { expandedRecipient = it },
            onItemSelected = { recipient = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Subject Dropdown
        DropdownField(
            label = "Subject:",
            value = subject,
            items = subjects,
            expanded = expandedSubject,
            onExpandedChange = { expandedSubject = it },
            onItemSelected = { subject = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Message Box
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Send Button
        Button(onClick = { sendMessage() }) {
            Text("Send")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = {
            navController.popBackStack()

        }){
            Text("Navigate back")
        }

        if (messageSentStatus.isNotEmpty()) {
            Text(
                messageSentStatus,
                color = if (messageSentStatus.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

    }
}

@Composable
fun Inbox(patientId: String, navController: NavController) {
    val messages = remember { mutableStateListOf<MessagePreview>() }

    // Load message previews from Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("messages")
            .where(
                Filter.or(
                    Filter.equalTo("senderId", patientId),
                    Filter.equalTo("recipient", "Admin"),
                )
            )
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                messages.clear()
                for (document in result) {
                    val message = document.toObject(Message::class.java)
                    // Create a preview (sender/recipient & snippet)
                    val preview = MessagePreview(
                        id = document.id,
                        sender = if (message.senderId == patientId) "You" else message.senderId, // Simplify for preview
                        recipient = message.recipient,
                        subject = message.subject,
                        snippet = message.message.take(50), // First 50 chars
                    )
                    messages.add(preview)
                }
            }
    }


    LazyColumn(Modifier.padding(16.dp)) {
        if (messages.isEmpty()) {
            item { Text("No messages yet.") }
        } else {
            items(messages) { preview ->
                MessagePreviewItem(preview) {
                    // On tap, navigate to full chat (not implemented here)
                    // navController.navigate("chat/${preview.id}")
                }
            }
        }
    }

    Spacer(modifier = Modifier.width(16.dp))
    Button(onClick = {
        navController.popBackStack()

    }){
        Text("Navigate back")
    }
}

@Composable
fun MessagePreviewItem(preview: MessagePreview, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Email, contentDescription = "Message", Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "${preview.sender} → ${preview.recipient}: ${preview.subject}",
                    fontWeight = FontWeight.Bold
                )
                Text(preview.snippet)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField1(
    label: String,
    value: String?,
    items: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onItemSelected: (String?) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        OutlinedTextField(
            readOnly = true,
            value = value ?: "",
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            items.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onItemSelected(selectionOption)
                        onExpandedChange(false)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}


// Data Classes
data class Message(
    val senderId: String = "",
    val recipient: String = "",
    val subject: String = "",
    val message: String = "",
    val timestamp: Date = Date()
)

data class MessagePreview(
    val id: String = "",
    val sender: String = "",
    val recipient: String = "",
    val subject: String = "",
    val snippet: String = ""
)