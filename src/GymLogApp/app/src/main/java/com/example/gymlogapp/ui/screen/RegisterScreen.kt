package com.example.gymlogapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlogapp.R
import com.example.gymlogapp.ui.components.GymLogLogo
import com.example.gymlogapp.repository.AuthRepository
import com.example.gymlogapp.model.RegisterRequest
import com.example.gymlogapp.utils.getErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RegisterScreen(navController: NavController, authRepository: AuthRepository) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var birthDateDigits by remember { mutableStateOf("") }
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
        Text(
            text = stringResource(R.string.gender_label),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                "M" to R.string.gender_male,
                "F" to R.string.gender_female
            ).forEach { (code, labelRes) ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = gender == code,
                            onClick = { gender = code },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = gender == code,
                        onClick = null
                    )
                    Text(stringResource(labelRes))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = birthDateDigits,
            onValueChange = { input ->
                birthDateDigits = input.filter(Char::isDigit).take(8)
            },
            label = { Text(stringResource(R.string.birth_date_label)) },
            placeholder = { Text(stringResource(R.string.birth_date_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = BirthDateVisualTransformation
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val birthDate = formatBirthDate(birthDateDigits)
                    val selectedGender = gender
                    val validBirthDate = birthDateDigits.length == 8 && isValidBirthDate(birthDate)
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, R.string.fill_required_fields, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedGender == null) {
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
                                    gender = selectedGender,
                                    birthDate = "${birthDate}T00:00:00"
                                )
                            )
                            Toast.makeText(context, R.string.registration_success, Toast.LENGTH_LONG).show()
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

private fun formatBirthDate(digits: String): String = buildString {
    digits.take(8).forEachIndexed { index, digit ->
        append(digit)
        if (index == 3 || index == 5) append('-')
    }
}

private fun isValidBirthDate(value: String): Boolean {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
    }
    val position = ParsePosition(0)
    return formatter.parse(value, position) != null && position.index == value.length
}

private object BirthDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val formatted = formatBirthDate(digits)
        val hasYearSeparator = digits.length >= 4
        val hasMonthSeparator = digits.length >= 6

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var result = offset
                if (hasYearSeparator && offset >= 4) result++
                if (hasMonthSeparator && offset >= 6) result++
                return result.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var result = offset
                if (hasYearSeparator && offset > 4) result--
                if (hasMonthSeparator && offset > 7) result--
                return result.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
