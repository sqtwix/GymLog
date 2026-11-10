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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlogapp.R
import com.example.gymlogapp.ui.components.GymLogLogo
import com.example.gymlogapp.repository.AuthRepository
import com.example.gymlogapp.model.RegisterRequest
import com.example.gymlogapp.utils.getErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController, authRepository: AuthRepository) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") } // формат: "1995-05-15"
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
            text = stringResource(R.string.registration_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name_label)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email_label)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(stringResource(R.string.password_label)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text(stringResource(R.string.gender_label)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = birthDay, onValueChange = { birthDay = it }, label = { Text(stringResource(R.string.birth_date_label)) }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val normalizedGender = gender.trim().uppercase()
                    val validBirthDate = runCatching {
                        java.time.LocalDate.parse(birthDay.trim())
                    }.isSuccess
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, R.string.fill_required_fields, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (normalizedGender !in setOf("F", "M")) {
                        Toast.makeText(context, R.string.invalid_gender, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!validBirthDate) {
                        Toast.makeText(context, R.string.invalid_birth_date, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            authRepository.register(
                                RegisterRequest(
                                    username = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    gender = normalizedGender,
                                    birthDate = "${birthDay.trim()}T00:00:00"
                                )
                            )
                            Toast.makeText(context, R.string.registration_success, Toast.LENGTH_LONG).show()
                            navController.navigate("main") {
                                popUpTo("register") { inclusive = true }
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_register))
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text(stringResource(R.string.login_prompt))
        }
    }
}
