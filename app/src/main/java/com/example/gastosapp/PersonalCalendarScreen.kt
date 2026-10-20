package com.example.gastosapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalCalendarScreen(
    calendarData: Map<String, CalendarDayStatus>,
    onBack: () -> Unit,
    onUpdateDay: (String, CalendarDayStatus?) -> Unit
) {
    var currentMonthDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayToEdit by remember { mutableStateOf<Int?>(null) }

    val daysInMonth = currentMonthDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthName = currentMonthDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "ES"))
    val year = currentMonthDate.get(Calendar.YEAR)

    // Para saber qué día de la semana empieza el mes
    val firstDayOfWeek = (currentMonthDate.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }.get(Calendar.DAY_OF_WEEK)

    // Ajuste de offset según el primer día (1 es Domingo en Calendar API)
    val dayOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

    Scaffold(
        containerColor = Color(0xFF160B21),
        topBar = {
            TopAppBar(
                title = { Text("Calendario de Asistencia", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF241033))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 750.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera de Mes con flechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newDate = currentMonthDate.clone() as Calendar
                        newDate.add(Calendar.MONTH, -1)
                        currentMonthDate = newDate
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = Color.White)
                    }

                    Text(
                        text = "${monthName.replaceFirstChar { it.uppercase() }} $year",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {
                        val newDate = currentMonthDate.clone() as Calendar
                        newDate.add(Calendar.MONTH, 1)
                        currentMonthDate = newDate
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Días de la semana
                Row(modifier = Modifier.fillMaxWidth()) {
                    val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Grilla de días
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Celdas vacías iniciales
                    items(dayOffset) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }

                    // Días del mes
                    items(daysInMonth) { dayIndex ->
                        val dayNum = dayIndex + 1
                        val dateKey = "$year-${currentMonthDate.get(Calendar.MONTH) + 1}-$dayNum"
                        val status = calendarData[dateKey]

                        val isToday = Calendar.getInstance().let {
                            it.get(Calendar.YEAR) == year &&
                            it.get(Calendar.MONTH) == currentMonthDate.get(Calendar.MONTH) &&
                            it.get(Calendar.DAY_OF_MONTH) == dayNum
                        }

                        CalendarDayItem(
                            dayNum = dayNum,
                            status = status,
                            isToday = isToday,
                            onClick = { selectedDayToEdit = dayNum }
                        )
                    }
                }
            }
        }

        // Diálogo de edición de día
        if (selectedDayToEdit != null) {
            val dayNum = selectedDayToEdit!!
            val dateKey = "$year-${currentMonthDate.get(Calendar.MONTH) + 1}-$dayNum"

            AlertDialog(
                onDismissRequest = { selectedDayToEdit = null },
                containerColor = Color(0xFF241033),
                title = { Text("Día $dayNum de $monthName", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("¿Cómo estuvo el Uber hoy?", color = Color.White.copy(alpha = 0.7f))

                        StatusOptionItem(
                            title = "Vino a buscarme",
                            icon = "✔️",
                            color = Color(0xFFFF8AE2), // Rosado
                            onClick = {
                                onUpdateDay(dateKey, CalendarDayStatus.ATTENDED)
                                selectedDayToEdit = null
                            }
                        )

                        StatusOptionItem(
                            title = "No vino a buscarme",
                            icon = "❌",
                            color = Color(0xFF4A1E63), // Morado Oscuro
                            onClick = {
                                onUpdateDay(dateKey, CalendarDayStatus.ABSENT)
                                selectedDayToEdit = null
                            }
                        )

                        StatusOptionItem(
                            title = "No disponible / Feriado",
                            icon = "🚫",
                            color = Color(0xFF636363), // Gris
                            onClick = {
                                onUpdateDay(dateKey, CalendarDayStatus.UNAVAILABLE)
                                selectedDayToEdit = null
                            }
                        )

                        TextButton(
                            onClick = {
                                onUpdateDay(dateKey, null)
                                selectedDayToEdit = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Limpiar registro", color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedDayToEdit = null }) {
                        Text("Cancelar", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun CalendarDayItem(
    dayNum: Int,
    status: CalendarDayStatus?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val indicatorColor = when (status) {
        CalendarDayStatus.ATTENDED -> Color(0xFFFF8AE2) // Rosado
        CalendarDayStatus.ABSENT -> Color(0xFF4A1E63)   // Morado Oscuro
        CalendarDayStatus.UNAVAILABLE -> Color(0xFF636363) // Gris
        null -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                if (isToday) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayNum.toString(),
            color = if (isToday) Color(0xFFFF8AE2) else Color.White,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(4.dp))

        if (status != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(indicatorColor, CircleShape)
            )
        }
    }
}

@Composable
fun StatusOptionItem(
    title: String,
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(10.dp).background(color, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "$icon $title", color = Color.White, fontSize = 14.sp)
        }
    }
}
