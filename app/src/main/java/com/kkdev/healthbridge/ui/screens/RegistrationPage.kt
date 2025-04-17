package com.kkdev.healthbridge.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class Role {
    Admin,
    Specialist,
    Patient
}

@Composable
fun RegistrationPage(onRegistrationSuccess: () -> Unit) { // UID is no longer a parameter
    var role by remember { mutableStateOf(Role.Patient) }
    val fullName = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }  // Email is now just displayed
    val mobileNumber = remember { mutableStateOf("") }

    // Role-specific fields (as before)
    val adminBranchAccess = remember { mutableStateOf("") }
    val adminDesignation = remember { mutableStateOf("") }
    val specialistDepartment = remember { mutableStateOf("") }
    val specialistSpecialization = remember { mutableStateOf("") }
    val specialistBranchLocation = remember { mutableStateOf("") }
    val specialistAvailability = remember { mutableStateOf("") }
    val specialistMedicalId = remember { mutableStateOf("") }
    val patientDateOfBirth = remember { mutableStateOf("") }
    val patientGender = remember { mutableStateOf("") }
    val patientPreferredBranch = remember { mutableStateOf("") }
    val patientMedicalNotes = remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Fetch user's email and UID from FirebaseAuth
    val currentUser = auth.currentUser
    LaunchedEffect(key1 = currentUser) {
        email.value = currentUser?.email ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Complete Registration", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        // Role selection (same as before)
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Role.values().forEach { currentRole ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == currentRole, onClick = { role = currentRole })
                    Text(currentRole.name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Common fields (email is now read-only)
        OutlinedTextField(
            value = fullName.value,
            onValueChange = { fullName.value = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = email.value,
            onValueChange = {}, // Make it read-only
            label = { Text("Email (Read-only)") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = mobileNumber.value,
            onValueChange = { mobileNumber.value = it },
            label = { Text("Mobile Number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Role-specific fields (same as before)
        when (role) {
            Role.Admin -> {
                OutlinedTextField(value = adminBranchAccess.value, onValueChange = { adminBranchAccess.value = it }, label = { Text("Branch Access") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = adminDesignation.value, onValueChange = { adminDesignation.value = it }, label = { Text("Designation/Staff ID (Optional)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
            }
            Role.Specialist -> {
                OutlinedTextField(value = specialistDepartment.value, onValueChange = { specialistDepartment.value = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = specialistSpecialization.value, onValueChange = { specialistSpecialization.value = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = specialistBranchLocation.value, onValueChange = { specialistBranchLocation.value = it }, label = { Text("Branch Location") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = specialistAvailability.value, onValueChange = { specialistAvailability.value = it }, label = { Text("Availability (Hours/Days)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = specialistMedicalId.value, onValueChange = { specialistMedicalId.value = it }, label = { Text("Medical Registration ID (Optional)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
            }
            Role.Patient -> {
                OutlinedTextField(value = patientDateOfBirth.value, onValueChange = { patientDateOfBirth.value = it }, label = { Text("Date of Birth") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = patientGender.value, onValueChange = { patientGender.value = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = patientPreferredBranch.value, onValueChange = { patientPreferredBranch.value = it }, label = { Text("Preferred Branch") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = patientMedicalNotes.value, onValueChange = { patientMedicalNotes.value = it }, label = { Text("Medical History/Notes (Optional)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
            }
        }

        Button(onClick = {
            val uid = currentUser?.uid // Get UID here
            if (uid != null) {
                val userData = mutableMapOf(
                    "role" to role.name,
                    "fullName" to fullName.value,
                    "email" to email.value,
                    "mobileNumber" to mobileNumber.value
                )

                when (role) {
                    Role.Admin -> {
                        userData["branchAccess"] = adminBranchAccess.value
                        if (adminDesignation.value.isNotEmpty()) userData["designation"] = adminDesignation.value
                    }
                    Role.Specialist -> {
                        userData["department"] = specialistDepartment.value
                        userData["specialization"] = specialistSpecialization.value
                        userData["branchLocation"] = specialistBranchLocation.value
                        userData["availability"] = specialistAvailability.value
                        if (specialistMedicalId.value.isNotEmpty()) userData["medicalId"] = specialistMedicalId.value
                    }
                    Role.Patient -> {
                        userData["dateOfBirth"] = patientDateOfBirth.value
                        userData["gender"] = patientGender.value
                        userData["preferredBranch"] = patientPreferredBranch.value
                        if (patientMedicalNotes.value.isNotEmpty()) userData["medicalNotes"] = patientMedicalNotes.value
                    }
                }

                db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        Log.d("Registration", "User data added successfully for UID: $uid")
                        onRegistrationSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Registration", "Error adding user data for UID: $uid", e)
                        // Handle Firestore error (e.g., display error message)
                    }
            } else {
                // Handle case where UID is null (shouldn't happen if user is logged in)
                Log.e("Registration", "Error: UID is null. User not logged in?")
                // Maybe navigate back to login or display an error
            }
        }) {
            Text("Complete Registration as ${role.name}")
        }
    }
}
