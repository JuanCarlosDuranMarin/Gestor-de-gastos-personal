package com.example.gastosapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekDetailScreen(
    week: WeekRecord,
    onBack: () -> Unit,
    onSelectDay: (String) -> Unit,
    onUpdateWeekBudget: (Double) -> Unit
) {

    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var inputAmount by remember { mutableStateOf("") }

    Scaffold(

        containerColor =
            Color(0xFF160B21),

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        "Semana ${week.weekNumber}",
                        fontWeight =
                            FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription =
                                "Volver"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            inputAmount = String.format("%.0f", week.allocatedBudget)
                            showEditBudgetDialog = true
                        }
                    ) {

                        Icon(
                            Icons.Default.Edit,
                            contentDescription =
                                "Editar presupuesto",
                            tint = Color.White
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            Color(0xFF241033),
                        titleContentColor =
                            Color.White,
                        navigationIconContentColor =
                            Color.White
                    )
            )
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(

                modifier =
                    Modifier
                        .widthIn(max = 750.dp)
                        .fillMaxWidth()
                        .padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(0xFF6C3FA4)
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        "RESUMEN SEMANAL",
                        color =
                            Color.White.copy(
                                alpha = .75f
                            ),
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(
                        "Presupuesto: $${money(week.allocatedBudget)}",
                        color =
                            Color.White,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        "Total gastado: $${money(week.totalSpent)}",
                        color =
                            Color.White,
                        fontSize = 21.sp,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        "Productos registrados: ${week.totalProducts}",
                        color =
                            Color.White
                    )

                    Spacer(
                        Modifier.height(5.dp)
                    )

                    Text(
                        "Disponible: $${money(week.remainingBudget)}",
                        color =
                            Color.White,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Text(
                "Días de la semana",
                color =
                    Color.White,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            LazyColumn(

                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    week.days,
                    key = {
                        it.dayName
                    }
                ) { day ->

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        onClick = {
                            onSelectDay(
                                day.dayName
                            )
                        },

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFF241033)
                            )
                    ) {

                        Row(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(modifier = Modifier.weight(1f)) {

                                Text(
                                    day.dayName,
                                    color =
                                        Color.White,
                                    fontWeight =
                                        FontWeight.Bold,
                                    fontSize = 17.sp
                                )

                                Text(
                                    "${day.totalProducts} productos registrados",
                                    color =
                                        Color.White.copy(
                                            alpha = .6f
                                        ),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                "$${money(day.totalSpent)}",
                                color =
                                    Color(0xFFB88AE5),
                                fontWeight =
                                    FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            containerColor = Color(0xFF241033),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Editar presupuesto semanal") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Ingresa el nuevo presupuesto asignado para esta semana.")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Presupuesto") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF9B6DD0),
                            unfocusedLabelColor = Color.White.copy(alpha = .7f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onUpdateWeekBudget(amount)
                            showEditBudgetDialog = false
                            inputAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBudgetDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
    }

}
