package com.kkdev.healthbridge.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAttendanceDashboard() {
    var attendanceRecords by remember { mutableStateOf(listOf<AttendanceRecordWithId>()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var selectedDepartment by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var searchText by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formattedDate by remember { derivedStateOf { dateFormatter.format(selectedDate) } }

    val departments = remember { mutableStateListOf("All", "General Medicine", "Neurology", "Dermatology", "Cardiology") } // Fetch from DB if needed
    val statuses = remember { listOf("All", "Present", "Absent", "Not Marked") }

    // Date Picker Dialog
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


    // Data Loading and Filtering
    LaunchedEffect(formattedDate, selectedDepartment, selectedStatus, searchText) {
        var query = FirebaseFirestore.getInstance().collection("attendance")  // Assuming "attendance" collection
            .whereEqualTo("date", formattedDate)  //  Ensure you have a "date" field


        if (selectedDepartment != "All") {
            query = query.whereEqualTo("department", selectedDepartment) // Ensure "department" field exists
        }

        if (selectedStatus != "All") {
            query = query.whereEqualTo("status", selectedStatus)  // Ensure a "status" field ("Present", "Absent", etc.)
        }

        if (searchText.isNotEmpty()) {
            // Basic search by patient ID or name (adjust based on your data)
            query = query.whereGreaterThanOrEqualTo("patientId", searchText)  // Assuming "patientId"
                .whereLessThan("patientId", searchText + '\uf8ff')
            //  For name search, you might need a more advanced approach or denormalized data.
        }


        query.get().addOnSuccessListener { result ->
            attendanceRecords = result.mapNotNull { document ->
                try {
                    val record = document.toObject<AttendanceRecord>()
                    if (record != null) {
                        AttendanceRecordWithId(document.id, record)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    println("Error deserializing attendance record: ${e.message}")
                    null
                }
            }
        }.addOnFailureListener { e ->
            println("Error fetching attendance records: ${e.message}")
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Attendance Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).padding(16.dp)) {
            // Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Date",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                var expandedDepartment by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedDepartment,
                    onExpandedChange = { expandedDepartment = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = if (selectedDepartment == "All") "All Departments" else selectedDepartment,
                        onValueChange = {},
                        label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDepartment) },
                        modifier = Modifier.menuAnchor().weight(1f)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDepartment,
                        onDismissRequest = { expandedDepartment = false }
                    ) {
                        departments.forEach { department ->
                            DropdownMenuItem(
                                text = { Text(department) },
                                onClick = {
                                    selectedDepartment = department
                                    expandedDepartment = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expandedStatus by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedStatus,
                        onValueChange = {},
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier.menuAnchor().weight(1f)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    selectedStatus = status
                                    expandedStatus = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Enter Patient Name / ID") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Attendance List
            LazyColumn {
                if (attendanceRecords.isEmpty()) {
                    item { Text("No attendance records found.") }
                } else {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Time", fontWeight = FontWeight.Bold)
                            Text("Patient Name", fontWeight = FontWeight.Bold)
                            Text("Patient ID", fontWeight = FontWeight.Bold)
                            Text("Department", fontWeight = FontWeight.Bold)
                            Text("Doctor", fontWeight = FontWeight.Bold)
                            Text("Status", fontWeight = FontWeight.Bold)
                            Text("Actions", fontWeight = FontWeight.Bold)
                        }
                        Divider()
                    }

                    items(attendanceRecords) { recordWithId ->
                        AttendanceRecordItem(recordWithId) { action, newStatus ->
                            handleAttendanceAction(action, recordWithId.id, newStatus)
                        }
                    }

                    // Summary
                    item {
                        val markedCount = attendanceRecords.count { it.record.status != "Not Marked" }
                        Text(
                            "✔️ Attendance marked for $markedCount out of ${attendanceRecords.size} patients today.",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceRecordItem(
    recordWithId: AttendanceRecordWithId,
    onAction: (String, String?) -> Unit  // e.g., ("Mark Present", "Present")
) {
    val record = recordWithId.record
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(record.time) // Assuming you have a "time" field
        Text(record.patientName) // Assuming you have a "patientName" field
        Text(record.patientId)
        Text(record.department)
        Text(record.doctor)
        Text(record.status)

        Row {
            if (record.status == "Not Marked") {
                TextButton(onClick = { onAction("Mark Present", "Present") }) { Text("Mark Present") }
                TextButton(onClick = { onAction("Mark Absent", "Absent") }) { Text("Mark Absent") }
            } else if (record.status == "Present") {
                TextButton(onClick = { onAction("Mark Absent", "Absent") }) { Text("Mark Absent") }
            } else if (record.status == "Absent") {
                TextButton(onClick = { onAction("Mark Present", "Present") }) { Text("Mark Present") }
            }
            TextButton(onClick = { /* TODO: View Details */ }) { Text("View Details") }
        }
    }
    Divider()
}

fun handleAttendanceAction(action: String, recordId: String, newStatus: String?) {
    val db = FirebaseFirestore.getInstance()
    val recordRef = db.collection("attendance").document(recordId)

    when (action) {
        "Mark Present", "Mark Absent" -> {
            if (newStatus != null) {
                recordRef.update("status", newStatus)
                    .addOnSuccessListener { println("Attendance record $recordId updated to $newStatus") }
                    .addOnFailureListener { e -> println("Error updating attendance: ${e.message}") }
            }
        }
        // For "View Details", you'd likely navigate to another screen.
    }
}

// Data Classes (Match your Firestore structure)
data class AttendanceRecordWithId(
    val id: String,
    val record: AttendanceRecord
)

data class AttendanceRecord(
    val time: String = "",
    val patientName: String = "",
    val patientId: String = "",
    val department: String = "",
    val doctor: String = "",
    val status: String = "Not Marked", // "Present", "Absent", "Not Marked" (or adjust to your needs)
    val date: String = "" // Add date field here
    //  Add other relevant fields, e.g. appointmentId, notes, etc.
)