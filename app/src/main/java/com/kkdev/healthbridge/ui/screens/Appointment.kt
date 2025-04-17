package com.kkdev.healthbridge.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentControlScreen() {
    var appointments by remember { mutableStateOf(listOf<AppointmentRequestWithId>()) }
    var filterDate by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("All") }
    val statuses = remember { listOf("All", "Pending", "Accepted", "Completed", "Cancelled") }

    // Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val initialDate = calendar.time
    val selectedDate = remember { mutableStateOf(initialDate) }
    val formattedDate = remember { derivedStateOf { dateFormatter.format(selectedDate.value) } }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    filterDate = formattedDate.value
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(initialSelectedDateMillis = selectedDate.value.time),
            )
        }
    }

    LaunchedEffect(filterDate, searchText, filterStatus) {
        var query = FirebaseFirestore.getInstance().collection("appointment_requests")
            .orderBy("date") // Assuming you have date for sorting

        if (!searchText.isNullOrEmpty()) {
            // Simple search by patientId for now (expand as needed)
            query = query.whereGreaterThanOrEqualTo("patientId", searchText)
                .whereLessThan("patientId", searchText + '\uf8ff')
        }

        if (filterDate != null) {
            query = query.whereEqualTo("date", filterDate)
        }

        if (filterStatus != "All") {
            query = query.whereEqualTo("status", filterStatus)
        }

        query.get().addOnSuccessListener { result ->
            appointments = result.mapNotNull { document ->
                try {
                    val appointment = document.toObject<AppointmentRequest>()
                    if (appointment != null) {
                        AppointmentRequestWithId(document.id, appointment)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    // Handle potential deserialization errors, e.g., log the error
                    println("Error deserializing appointment: ${e.message}")
                    null
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Control") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filterDate ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date Filter") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Select Date",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search by Name/Specialist") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            var expandedStatus by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = filterStatus,
                    onValueChange = {},
                    label = { Text("Status") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statuses.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                filterStatus = status
                                expandedStatus = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                if (appointments.isEmpty()) {
                    item { Text("No appointments found.") }
                } else {
                    items(appointments) { appointmentWithId ->
                        AppointmentItem(appointmentWithId) { action, specialist ->
                            handleAppointmentAction(action, appointmentWithId.id, specialist)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentItem(
    appointmentWithId: AppointmentRequestWithId,
    onAction: (String, String?) -> Unit
) {
    val appointment = appointmentWithId.appointment
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${appointment.time} - ${appointment.patientId} ", // Add patient name if available
                fontWeight = FontWeight.Bold
            )
            Text("Specialist: ${appointment.doctor}")
            Text("Status: ${appointment.status}")

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction("Reschedule", null) }) { Text("Reschedule") }
                Button(onClick = { onAction("Cancel", null) }) { Text("Cancel") }
                if (appointment.status == "Pending") {  // Conditionally show Accept
                    Button(onClick = { onAction("Accept", null) }) { Text("Accept") }
                }


                // Simple specialist selection for demo
                var selectedSpecialist by remember { mutableStateOf("Dr. Any") }
                var expandedSpecialist by remember { mutableStateOf(false) }
                if (appointment.status == "Pending" || appointment.status == "Accepted") { //  Show assign for Pending/Accepted
                    ExposedDropdownMenuBox(
                        expanded = expandedSpecialist,
                        onExpandedChange = { expandedSpecialist = it }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedSpecialist,
                            onValueChange = {},
                            label = { Text("Assign Specialist") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpecialist)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .width(150.dp) // Adjust width as needed
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSpecialist,
                            onDismissRequest = { expandedSpecialist = false }
                        ) {
                            listOf("Dr. Any", "Dr. A", "Dr. B").forEach { specialist -> // Replace with actual list
                                DropdownMenuItem(
                                    text = { Text(specialist) },
                                    onClick = {
                                        selectedSpecialist = specialist
                                        onAction("Assign", specialist)
                                        expandedSpecialist = false
                                    },contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun handleAppointmentAction(action: String, appointmentId: String, specialist: String?) {
    val db = FirebaseFirestore.getInstance()
    val appointmentRef = db.collection("appointment_requests").document(appointmentId)

    when (action) {
        "Reschedule" -> {
            // You'd likely open a dialog or navigate to a reschedule screen here
            println("Reschedule appointment $appointmentId")
        }
        "Cancel" -> {
            appointmentRef.update("status", "Cancelled")
                .addOnSuccessListener { println("Appointment $appointmentId cancelled.") }
                .addOnFailureListener { e -> println("Error cancelling appointment: $e") }
        }
        "Assign" -> {
            if (specialist != null) {
                appointmentRef.update("doctor", specialist, "status", "Accepted")
                    .addOnSuccessListener { println("Appointment $appointmentId assigned to $specialist") }
                    .addOnFailureListener { e -> println("Error assigning specialist: $e") }
            }
        }
        "Accept" -> {
            appointmentRef.update("status", "Accepted")
                .addOnSuccessListener { println("Appointment $appointmentId accepted.") }
                .addOnFailureListener { e -> println("Error accepting appointment: $e") }
        }
    }
}

data class AppointmentRequestWithId(
    val id: String,
    val appointment: AppointmentRequest
)


