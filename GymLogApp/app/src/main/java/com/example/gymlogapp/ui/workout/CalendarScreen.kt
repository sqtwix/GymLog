package com.example.gymlogapp.ui.workout

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymlogapp.data.network.BodyPart
import com.example.gymlogapp.data.network.WorkoutLocation
import com.example.gymlogapp.data.network.WorkoutResponse
import com.example.gymlogapp.data.network.WorkoutType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workouts by viewModel.workouts.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val types by viewModel.types.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val bodyParts by viewModel.bodyParts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingWorkout by remember { mutableStateOf<WorkoutResponse?>(null) }

    // Dark-themed background gradient
    val darkGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1E2E), Color(0xFF11111B))
    )
    val accentColor = Color(0xFF89B4FA)
    val containerBg = Color(0xFF181825)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GymLog Tracker",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadWorkouts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = containerBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = accentColor,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.background(darkGradient)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Calendar strip selector
            CalendarStrip(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.selectedDate.value = it }
            )

            // Header for Selected Date Workouts
            val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
            Text(
                text = formattedDate,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Filtering workouts for selected date
            val filteredWorkouts = workouts.filter { w ->
                try {
                    val parsed = LocalDateTime.parse(w.date)
                    parsed.toLocalDate() == selectedDate
                } catch (e: Exception) {
                    false
                }
            }

            if (uiState is WorkoutUiState.Loading && workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (filteredWorkouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, size = 48.dp, tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No workouts scheduled for today", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredWorkouts) { workout ->
                        WorkoutCard(
                            workout = workout,
                            onEdit = { editingWorkout = workout },
                            onDelete = { viewModel.deleteWorkout(workout.id) }
                        )
                    }
                }
            }

            if (uiState is WorkoutUiState.Error) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.loadWorkouts() }) {
                            Text("Retry", color = accentColor)
                        }
                    }
                ) {
                    Text((uiState as WorkoutUiState.Error).message)
                }
            }
        }
    }

    // Add Workout Dialog
    if (showAddDialog) {
        WorkoutFormDialog(
            title = "Add New Workout",
            types = types,
            locations = locations,
            bodyParts = bodyParts,
            initialDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onSubmit = { typeId, locationId, desc, dateTime, duration, bodyPartIds ->
                viewModel.createWorkout(typeId, locationId, desc, dateTime, duration, bodyPartIds)
                showAddDialog = false
            }
        )
    }

    // Edit Workout Dialog
    editingWorkout?.let { workout ->
        val parsedDateTime = try {
            LocalDateTime.parse(workout.date)
        } catch (e: Exception) {
            LocalDateTime.of(selectedDate, LocalTime.NOON)
        }

        WorkoutFormDialog(
            title = "Edit Workout",
            types = types,
            locations = locations,
            bodyParts = bodyParts,
            initialDate = selectedDate,
            initialType = workout.type,
            initialLocation = workout.location,
            initialDescription = workout.description ?: "",
            initialTime = parsedDateTime.toLocalTime(),
            initialDuration = workout.durationInMinutes.toString(),
            initialBodyParts = workout.bodyParts,
            onDismiss = { editingWorkout = null },
            onSubmit = { typeId, locationId, desc, dateTime, duration, bodyPartIds ->
                viewModel.updateWorkout(workout.id, typeId, locationId, desc, dateTime, duration, bodyPartIds)
                editingWorkout = null
            }
        )
    }
}

@Composable
fun CalendarStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    
    // Generates dates for current horizontal selector (-15 days to +15 days)
    val dates = remember {
        (-14..14).map { today.plusDays(it.toLong()) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF181825))
            .padding(vertical = 12.dp)
    ) {
        // Month / Year Label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + selectedDate.year,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Today",
                color = Color(0xFF89B4FA),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onDateSelected(today) }
                    .padding(4.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dates) { date ->
                val isSelected = date == selectedDate
                val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val dayOfMonth = date.dayOfMonth.toString()

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF89B4FA) else Color(0xFF313244)
                    ),
                    modifier = Modifier
                        .width(55.dp)
                        .clickable { onDateSelected(date) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayOfWeek,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayOfMonth,
                            fontSize = 16.sp,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutCard(
    workout: WorkoutResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = Color(0xFF313244)
    val textAccent = Color(0xFF89B4FA)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular initial
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFF1E1E2E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workout.type.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textAccent
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.type,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = workout.location ?: "Unknown Location",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.PlayArrow, // Time/Duration icon proxy
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${workout.durationInMinutes} min",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                if (!workout.description.isNullOrBlank()) {
                    Text(
                        text = workout.description,
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Render body part chips if present
                if (workout.bodyParts.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        workout.bodyParts.forEach { part ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(part, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = Color(0xFFF5C2E7),
                                    containerColor = Color(0xFF1E1E2E)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFF5C2E7))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF38BA8))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutFormDialog(
    title: String,
    types: List<WorkoutType>,
    locations: List<WorkoutLocation>,
    bodyParts: List<BodyPart>,
    initialDate: LocalDate,
    initialType: String? = null,
    initialLocation: String? = null,
    initialDescription: String = "",
    initialTime: LocalTime = LocalTime.NOON,
    initialDuration: String = "45",
    initialBodyParts: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (typeId: Long, locationId: Long?, description: String, dateTime: LocalDateTime, duration: Int, bodyPartIds: List<Long>?) -> Unit
) {
    val context = LocalContext.current

    // State setup
    var selectedType by remember {
        mutableStateOf(types.find { it.type == initialType } ?: types.firstOrNull())
    }
    var selectedLocation by remember {
        mutableStateOf(locations.find { it.location == initialLocation } ?: locations.firstOrNull())
    }
    var description by remember { mutableStateOf(initialDescription) }
    var durationStr by remember { mutableStateOf(initialDuration) }
    var selectedTime by remember { mutableStateOf(initialTime) }

    // Trace selected body parts (names or IDs)
    val selectedBodyPartIds = remember {
        mutableStateListOf<Long>().apply {
            addAll(bodyParts.filter { it.partName in initialBodyParts }.map { it.id })
        }
    }

    // Time picker dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = LocalTime.of(hourOfDay, minute)
        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    // Drops control
    var typeExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    // Conditional render check: Location name equals "Gym"
    val isGymLocation = selectedLocation?.location?.lowercase() == "gym"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = Color(0xFF1E1E2E),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedType?.type ?: "Select Type",
                        onValueChange = {},
                        label = { Text("Workout Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.background(Color(0xFF313244))
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.type, color = Color.White) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Location Dropdown
                ExposedDropdownMenuBox(
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = !locationExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedLocation?.location ?: "Select Location",
                        onValueChange = {},
                        label = { Text("Location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false },
                        modifier = Modifier.background(Color(0xFF313244))
                    ) {
                        locations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc.location, color = Color.White) },
                                onClick = {
                                    selectedLocation = loc
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }

                // Duration Input
                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it },
                    label = { Text("Duration (minutes)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time picker button
                val formattedTime = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                OutlinedTextField(
                    value = formattedTime,
                    onValueChange = {},
                    label = { Text("Start Time") },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Render body parts selector only if Location is "Gym"
                if (isGymLocation) {
                    Text(
                        "Select Targeted Muscles",
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // FlowRow replacement: row list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        bodyParts.chunked(3).forEach { rowParts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowParts.forEach { part ->
                                    val isSelected = selectedBodyPartIds.contains(part.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) {
                                                selectedBodyPartIds.remove(part.id)
                                            } else {
                                                selectedBodyPartIds.add(part.id)
                                            }
                                        },
                                        label = { Text(part.partName, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFF5C2E7),
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color.Transparent,
                                            labelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Filler chips if row is not full
                                if (rowParts.size < 3) {
                                    repeat(3 - rowParts.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val typeId = selectedType?.id ?: 1L
                    val locationId = selectedLocation?.id
                    val dur = durationStr.toIntOrNull() ?: 45
                    val dateTime = LocalDateTime.of(initialDate, selectedTime)
                    val bodyPartsList = if (isGymLocation) selectedBodyPartIds.toList() else null

                    onSubmit(typeId, locationId, description, dateTime, dur, bodyPartsList)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF89B4FA))
            ) {
                Text("SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.LightGray)
            }
        }
    )
}
