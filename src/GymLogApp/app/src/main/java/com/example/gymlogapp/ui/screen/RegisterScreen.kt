package com.example.gymlogapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var showValidationErrors by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val formattedBirthDate = formatBirthDate(birthDateDigits)
    val isBirthDateValid = birthDateDigits.length == 8 && isValidBirthDate(formattedBirthDate)
    val hasRequiredTextFields = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()

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

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && name.isBlank()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && email.isBlank()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && password.isBlank()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.gender_label),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            color = if (showValidationErrors && gender == null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                Triple("M", R.string.gender_male, R.drawable.male_sign),
                Triple("F", R.string.gender_female, R.drawable.female_sign)
            ).forEach { (code, labelRes, imageRes) ->
                val isSelected = gender == code
                val cardShape = RoundedCornerShape(20.dp)

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(124.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = { gender = code },
                            role = Role.RadioButton
                        ),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else if (showValidationErrors && gender == null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 3.dp else 0.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(cardShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(imageRes),
                            contentDescription = stringResource(labelRes),
                            modifier = Modifier.size(88.dp),
                            contentScale = ContentScale.Fit
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(4.dp)
                            )
                        }
                    }
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
            visualTransformation = BirthDateVisualTransformation,
            isError = showValidationErrors && !isBirthDateValid
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val birthDate = formattedBirthDate
                    val selectedGender = gender
                    showValidationErrors = true
                    if (!hasRequiredTextFields) {
                        Toast.makeText(context, R.string.fill_required_fields, Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (selectedGender == null) {
                        Toast.makeText(context, R.string.invalid_gender, Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (!isBirthDateValid) {
                        Toast.makeText(context, R.string.invalid_birth_date, Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    showValidationErrors = false
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
