package com.kkdev.healthbridge.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kkdev.healthbridge.AuthState
import com.kkdev.healthbridge.AuthViewModel


@Composable
fun LoginPage(modifier: Modifier = Modifier,navController: NavController,authViewModel: AuthViewModel) {


    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    FirebaseFirestore.getInstance().collection("users").document(it.uid).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val role = document.getString("role")
                                if (role != null) {
                                    navController.navigate("home/$role") // Navigate with role
                                } else {
                                    // Handle case where role is not found for the user.
                                    Log.w("LoginPage", "User role not found in Firestore")
                                    Toast.makeText(context, "User role not defined. Contact support.", Toast.LENGTH_SHORT).show()
                                    // Optionally, navigate to a default screen or back to login.
                                }
                            } else {
                                Log.w("LoginPage", "No such document")
                                Toast.makeText(context, "User data not found. Please complete registration.", Toast.LENGTH_SHORT).show()
                                // You might want to navigate to a registration completion screen here.
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("LoginPage", "Error getting document: ", exception)
                            Toast.makeText(context, "Error retrieving user data. Please try again.", Toast.LENGTH_SHORT).show()
                            // Handle the error appropriately, e.g., display a user-friendly message.
                        }
                }
            }

            is AuthState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            else -> Unit // Do nothing for other states (e.g., loading)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Health Bridge", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(16.dp))

        Text(text = "Login Page", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.login(email,password)
        },
            enabled = authState.value != AuthState.Loading
        ) {
            Text(text = "Login")
        }


        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("signup")
        }) {
            Text(text = "Don't have an account, Signup")
        }

    }

}


















