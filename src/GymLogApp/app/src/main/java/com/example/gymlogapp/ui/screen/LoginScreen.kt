package com.example.gymlogapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlogapp.R
import com.example.gymlogapp.ui.components.GymLogLogo
import com.example.gymlogapp.repository.AuthRepository
import com.example.gymlogapp.utils.getErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, authRepository: AuthRepository) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GymLogLogo()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            authRepository.login(email, password)
                            Toast.makeText(context, R.string.login_success, Toast.LENGTH_SHORT).show()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            errorMessage = getErrorMessage(context, e)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.action_login))
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text(stringResource(R.string.register_prompt))
        }
    }
}
