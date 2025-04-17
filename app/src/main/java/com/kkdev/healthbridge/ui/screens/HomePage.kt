package com.kkdev.healthbridge.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.kkdev.healthbridge.AuthViewModel

// --- Data classes (replace with your actual data) ---
data class Patient(val name: String, val branch: String)
data class Appointment(val doctor: String, val time: String)
data class Specialist(val name: String, val appointmentsToday: Int)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
// Add data classes for Admin if needed

// --- Composable functions ---

@Composable
fun PatientHomePage(authViewModel: AuthViewModel,navController: NavController,patient: Patient, upcomingAppointment: Appointment?) {
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("👤 Hello, ${patient.name}!", style = MaterialTheme.typography.titleLarge)
                Text("📍 Branch: ${patient.branch}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("bookapp")}, modifier = Modifier.fillMaxWidth()) {
            Text("📅 Book Appointment")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("📂 My Records")
        }
        Button(onClick = {navController.navigate("message")}, modifier = Modifier.fillMaxWidth()) {
            Text("💬 Chat with Doctor")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("🔔 Notifications")
        }
        Spacer(modifier = Modifier.height(16.dp))

        upcomingAppointment?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Upcoming Appointment:", style = MaterialTheme.typography.titleMedium)
                    Text("🩺 ${it.doctor} | 🕒 ${it.time}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } ?: Text("No upcoming appointments", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = {
            authViewModel.signout()
            navController.navigate("login")
        }) {
            Text("Sign out")
        }
    }
}


@Composable
fun AdminHomePage(navController: NavController,    authViewModel: AuthViewModel
) {
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚙️ Admin Panel - KIOT Health", style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("👥 Patients")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("🧠 Doctors")
        }
        Button(onClick = { navController.navigate("admin-app")}, modifier = Modifier.fillMaxWidth()) {
            Text("🗓 Appointments")
        }
        Button(onClick = { navController.navigate("attendance")}, modifier = Modifier.fillMaxWidth()) {
            Text("📊 Attendance")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("📁 Bulk Upload")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("📊 View Reports")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("🔔 Notifications")
        }
        Button(onClick = {
            authViewModel.signout()
            navController.navigate("login")
        }) {
            Text("Sign out")
        }
    }
}


@Composable
fun SpecialistHomePage(authViewModel: AuthViewModel,navController: NavController,specialist: Specialist, appointments: List<Pair<String, String>>) {
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🩺 Welcome Dr. ${specialist.name}!", style = MaterialTheme.typography.titleLarge)
                Text("📍 Today: ${specialist.appointmentsToday} Appointments", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        appointments.forEach { (time, patient) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏰ $time - $patient", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = { /*TODO*/ }) {
                        Text("View 📂")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("spec-app")}, modifier = Modifier.fillMaxWidth()) {
            Text("👥 View Patients")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("📥 Upload Prescription")
        }
        Button(onClick = { navController.navigate("message")}, modifier = Modifier.fillMaxWidth()) {
            Text("💬 Messages")
        }
        Button(onClick = {
            authViewModel.signout()
            navController.navigate("login")
        }) {
            Text("Sign out")
        }
    }
}

