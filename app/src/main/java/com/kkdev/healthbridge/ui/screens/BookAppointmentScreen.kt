package com.kkdev.healthbridge.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    var selectedDepartment by remember { mutableStateOf<String?>(null) }
    var selectedDoctor by remember { mutableStateOf<String?>(null) }
    var reasonForVisit by remember { mutableStateOf<String?>(null) }
    var preferredDate by remember { mutableStateOf<String?>(null) }
    var preferredTime by remember { mutableStateOf<String?>(null) }
    var appointmentMode by remember { mutableStateOf<String?>(null) }
    var attachedReport by remember { mutableStateOf<Uri?>(null) }
    var appointmentRequestStatus by remember { mutableStateOf("") }

    // Placeholder Data
    val departments = remember { listOf("General Medicine", "Cardiology", "Dermatology") }
    val doctors = remember { listOf("Dr. Smith", "Dr. Jones", "Dr. Williams") }
    val reasons = remember { listOf("Fever", "Headache", "Follow-up") }
    val modes = remember { listOf("In-person", "Video Consultation", "Phone Call") }

    // Dropdown state
    var expandedDepartment by remember { mutableStateOf(false) }
    var expandedDoctor by remember { mutableStateOf(false) }
    var expandedReason by remember { mutableStateOf(false) }
    var expandedMode by remember { mutableStateOf(false) }

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
                    preferredDate = formattedDate.value
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

    // File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        attachedReport = uri
    }

    fun bookAppointment() {
        if (selectedDepartment != null && selectedDoctor != null && reasonForVisit != null && preferredDate != null && preferredTime != null && appointmentMode != null) {
            val appointmentRequest = AppointmentRequest(
                patientId = uid.toString(),
                department = selectedDepartment!!,
                doctor = selectedDoctor!!,
                reason = reasonForVisit!!,
                date = preferredDate!!,
                time = preferredTime!!,
                mode = appointmentMode!!,
                reportUri = attachedReport?.toString()
            )

            FirebaseFirestore.getInstance().collection("appointment_requests").add(appointmentRequest)
                .addOnSuccessListener {
                    appointmentRequestStatus = "Appointment request submitted successfully."
                    navController.popBackStack()
                }
                .addOnFailureListener { e ->
                    appointmentRequestStatus = "Error: ${e.message}"
                }
        } else {
            appointmentRequestStatus = "Please fill all required fields."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Book Appointment", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Department Dropdown
        DropdownField(
            label = "Department/Specialization",
            value = selectedDepartment,
            items = departments,
            expanded = expandedDepartment,
            onExpandedChange = { expandedDepartment = it },
            onItemSelected = { selectedDepartment = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Doctor Dropdown
        DropdownField(
            label = "Preferred Doctor/Specialist",
            value = selectedDoctor,
            items = doctors,
            expanded = expandedDoctor,
            onExpandedChange = { expandedDoctor = it },
            onItemSelected = { selectedDoctor = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Reason Dropdown
        DropdownField(
            label = "Reason for Visit/Symptoms",
            value = reasonForVisit,
            items = reasons,
            expanded = expandedReason,
            onExpandedChange = { expandedReason = it },
            onItemSelected = { reasonForVisit = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Date
        OutlinedTextField(
            value = preferredDate ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Preferred Date") },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "Select Date",
                    modifier = Modifier.clickable { showDatePicker = true }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Time
        OutlinedTextField(
            value = preferredTime ?: "",
            onValueChange = { preferredTime = it },
            label = { Text("Preferred Time Slot (e.g., 2:00 PM)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Mode Dropdown
        DropdownField(
            label = "Mode of Appointment",
            value = appointmentMode,
            items = modes,
            expanded = expandedMode,
            onExpandedChange = { expandedMode = it },
            onItemSelected = { appointmentMode = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Attach Report
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { filePickerLauncher.launch("*/*") }) { Text("Attach Report") }
            Spacer(Modifier.width(8.dp))
            Text(attachedReport?.lastPathSegment ?: "No file selected")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { bookAppointment() }) { Text("Book Appointment") }
        if (appointmentRequestStatus.isNotEmpty()) {
            Text(
                appointmentRequestStatus,
                color = if (appointmentRequestStatus.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
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


// AppointmentRequest Data Class
data class AppointmentRequest(
    val patientId: String = "",
    val department: String = "",
    val doctor: String = "",
    val reason: String = "",
    val date: String = "",
    val time: String = "",
    val mode: String = "",
    val status: String = "Pending",
    val reportUri: String? = null
)