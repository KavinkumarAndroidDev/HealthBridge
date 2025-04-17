package com.kkdev.healthbridge

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambdaInstance
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kkdev.healthbridge.screens.AdminHomePage
import com.kkdev.healthbridge.screens.Appointment
import com.kkdev.healthbridge.screens.LoginPage
import com.kkdev.healthbridge.screens.Patient
import com.kkdev.healthbridge.screens.PatientHomePage
import com.kkdev.healthbridge.screens.SignupPage
import com.kkdev.healthbridge.screens.Specialist
import com.kkdev.healthbridge.screens.SpecialistHomePage
import com.kkdev.healthbridge.ui.screens.AppointmentControlScreen
import com.kkdev.healthbridge.ui.screens.BookAppointmentScreen
import com.kkdev.healthbridge.ui.screens.MessagingScreen
import com.kkdev.healthbridge.ui.screens.PatientAttendanceDashboard
import com.kkdev.healthbridge.ui.screens.RegistrationPage
import com.kkdev.healthbridge.ui.screens.SpecialistAppointmentsScreen


@Composable
fun MyAppNavigation(modifier: Modifier = Modifier,authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier,navController,authViewModel)
        }
        composable("signup"){
            SignupPage(modifier,navController,authViewModel)
        }
        composable("home/{role}") { backStackEntry ->  // Route now includes role
            val role = backStackEntry.arguments?.getString("role") ?: "unknown"
            RoleBasedHomeScreen(authViewModel,navController,role = role)
        }
        composable("registration"){
            RegistrationPage(onRegistrationSuccess = {
                navController.navigate("login")
            })
        }
        composable("bookapp"){

            BookAppointmentScreen(navController)
        }
        composable("message"){
            MessagingScreen(navController)
        }
        composable("admin-app") {
            AppointmentControlScreen()
        }
        composable("spec-app") {
            SpecialistAppointmentsScreen("Dr. John")
        }
        composable("attendance") {
            PatientAttendanceDashboard()
        }
    })
}


@Composable
fun RoleBasedHomeScreen(authViewModel: AuthViewModel,navController: NavController, role: String) {
    // Replace with actual data fetching/logic
    val patient = Patient("Sowndariya", "KIOT Health")
    val upcomingAppointment = Appointment("Dr. Priya", "Sat, 4 PM")
    val specialist = Specialist("Meena", 4)
    val specialistAppointments = listOf(
        "10:00" to "Ramesh",
        "11:00" to "Sowmya",
        // ... more appointments
    )

    when (role) {
        "Patient" -> PatientHomePage(authViewModel,navController, patient, upcomingAppointment)
        "Admin" -> AdminHomePage(navController,authViewModel)
        "Specialist" -> SpecialistHomePage(authViewModel,navController,specialist, specialistAppointments)
        else -> Text("Unknown Role") // Or navigate to an error screen
    }
}
