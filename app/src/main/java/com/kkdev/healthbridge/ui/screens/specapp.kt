package com.kkdev.healthbridge.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
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
fun SpecialistAppointmentsScreen(specialistName: String) { // Pass specialist name
    var appointments by remember { mutableStateOf(listOf<AppointmentRequestWithId>()) }
    var searchText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formattedDate by remember { derivedStateOf { dateFormatter.format(selectedDate) } }

    // Date Picker Dialog State
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time),
            )
        }
    }

    // Data Loading
    LaunchedEffect(searchText, formattedDate, specialistName) {
        var query = FirebaseFirestore.getInstance().collection("appointment_requests")
            .whereEqualTo("doctor", specialistName) // Filter by specialist
            .whereEqualTo("date", formattedDate)
            .orderBy("time") // Assuming you store time as a string that can be ordered

        if (searchText.isNotEmpty()) {
            query = query.whereGreaterThanOrEqualTo("patientId", searchText)
                .whereLessThan("patientId", searchText + '\uf8ff') // Basic ID search
            // For more robust search, consider integrating with a search service or denormalizing patient data
        }

        query.get().addOnSuccessListener { result ->
            appointments = result.mapNotNull { document ->
                try {
                    val appointment = document.toObject<AppointmentRequest>()
                    if (appointment != null) {
                        AppointmentRequestWithId(document.id, appointment)
                    } else {
                        null // Handle potential null appointment
                    }
                } catch (e: Exception) {
                    println("Error deserializing appointment: ${e.message}")
                    null // Skip appointments with errors
                }
            }
        }.addOnFailureListener { e ->
            println("Error fetching appointments: ${e.message}")
            // Handle the error appropriately, e.g., show a snackbar or error message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Appointments") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).padding(16.dp)) {
            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search Patient Name or ID") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Date") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Date",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appointment List
            if (appointments.isEmpty()) {
                Text("No more appointments today.")
            } else {
                LazyColumn {
                    items(appointments) { appointmentWithId ->
                        AppointmentItem(appointmentWithId)
                        // Add onClick actions or navigation as needed, e.g.:
                        /*{
                           navController.navigate("appointmentDetails/${appointmentWithId.id}")
                        }*/
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointmentWithId: AppointmentRequestWithId) {
    val appointment = appointmentWithId.appointment
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${appointment.time} –  (Patient ID: ${appointment.patientId})", // Assuming a "time" field
                fontWeight = FontWeight.Bold
            )
            // If you have patient names in the appointment document, display them:
            // Text("${appointment.time} – ${appointment.patientName} (Patient ID: ${appointment.patientId})", fontWeight = FontWeight.Bold)
            Text("Reason: ${appointment.reason ?: "Not specified"}")  // Assuming a "reason" field.  Handle nulls.

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /* TODO: View Profile Action */ }) {
                    Icon(Icons.Filled.Person, contentDescription = "View Profile")
                    Text("View Profile")
                }
                Button(onClick = { /* TODO: Start Consultation Action */ }) {
                    Text("Start Consultation")
                }
            }
        }
    }
}

