package com.example.gastosapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    weeks: List<WeekRecord>,
    directCategories: List<DirectPurchaseCategory>,
    compensations: List<CompensationItem>,
    monthlyBudget: Double,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    var expandedWeekIds by remember { mutableStateOf(setOf<String>()) }
    var expandedCategoryIds by remember { mutableStateOf(setOf<String>()) }

    val totalSpentWeeks = weeks.sumOf { it.totalSpent }
    val totalSpentProducts = directCategories.sumOf { it.totalSpent }
    val totalCompensations = compensations.sumOf { it.amount }
    val globalTotalSpent = totalSpentWeeks + totalSpentProducts
    val globalRemaining = monthlyBudget - globalTotalSpent + totalCompensations

    Scaffold(
        containerColor = Color(0xFF160B21),
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas y Registros", fontWeight = FontWeight.Bold, color = Color.White) },
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
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 750.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de Gastos Totales y Compensaciones (Diseño 2x2 adaptado a pantallas delgadas)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C3FA4))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📊 RESUMEN GLOBAL", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Semanas", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    Text("$${money(totalSpentWeeks)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                                Spacer(Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Productos", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    Text("$${money(totalSpentProducts)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                                Spacer(Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Compensación", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    Text("+$${money(totalCompensations)}", color = Color(0xFFB8FFB8), fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }

                            Spacer(Modifier.height(6.dp))
                            Divider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Total Gastado", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    Text("$${money(globalTotalSpent)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("Restante Global", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    Text(
                                        "$${money(globalRemaining)}",
                                        color = if (globalRemaining >= 0) Color(0xFFB8FFB8) else Color(0xFFFF8080),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                // Pestañas Semanas, Productos, Compensación
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF241033), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val activeBg = Color(0xFF9B6DD0)
                        val inactiveBg = Color.Transparent

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selectedTab == 0) activeBg else inactiveBg, RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Semanas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selectedTab == 1) activeBg else inactiveBg, RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Productos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selectedTab == 2) activeBg else inactiveBg, RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 2 }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Compensac.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }

                // Contenido de Pestañas
                if (selectedTab == 0) {
                    if (weeks.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                                Text("No hay semanas registradas.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(weeks, key = { it.id }) { week ->
                            val isExpanded = expandedWeekIds.contains(week.id)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedWeekIds = if (isExpanded) {
                                                    expandedWeekIds - week.id
                                                } else {
                                                    expandedWeekIds + week.id
                                                }
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Semana ${week.weekNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                            Text("Presupuesto: $${money(week.allocatedBudget)} | Gastado: $${money(week.totalSpent)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "$${money(week.remainingBudget)}",
                                                color = if (week.remainingBudget >= 0) Color(0xFFB8FFB8) else Color(0xFFFF8080),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Expandir",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    if (isExpanded) {
                                        Spacer(Modifier.height(12.dp))
                                        Divider(color = Color.White.copy(alpha = 0.15f))
                                        Spacer(Modifier.height(8.dp))

                                        val hasAnyExpense = week.days.any { day -> day.expenses.values.any { it.isNotEmpty() } }

                                        if (hasAnyExpense) {
                                            week.days.forEach { day ->
                                                if (day.expenses.values.any { it.isNotEmpty() }) {
                                                    Text(day.dayName, color = Color(0xFF9B6DD0), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                                                    day.expenses.forEach { (category, items) ->
                                                        items.forEach { item ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text("• ${item.name} (${category.title})", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, softWrap = false)
                                                                Spacer(Modifier.width(8.dp))
                                                                Text("$${money(item.price)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Text("No hay gastos registrados en esta semana.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    if (directCategories.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                                Text("No hay bloques de productos registrados.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(directCategories, key = { it.id }) { cat ->
                            val isExpanded = expandedCategoryIds.contains(cat.id)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedCategoryIds = if (isExpanded) {
                                                    expandedCategoryIds - cat.id
                                                } else {
                                                    expandedCategoryIds + cat.id
                                                }
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(cat.categoryName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                            Text("Tope: $${money(cat.allocatedBudget)} | Gastado: $${money(cat.totalSpent)}", color = Color.White.copy(alpha =.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "$${money(cat.remainingBudget)}",
                                                color = if (cat.remainingBudget >= 0) Color(0xFFB8FFB8) else Color(0xFFFF8080),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Expandir",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    if (isExpanded) {
                                        Spacer(Modifier.height(12.dp))
                                        Divider(color = Color.White.copy(alpha = 0.15f))
                                        Spacer(Modifier.height(8.dp))

                                        if (cat.items.isNotEmpty()) {
                                            cat.items.forEach { item ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("• ${item.name} (x${item.quantity})", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, softWrap = false)
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("$${money(item.price)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                                }
                                            }
                                        } else {
                                            Text("No hay productos añadidos en este bloque.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Pestaña Compensaciones en Estadísticas
                    if (compensations.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                                Text("No hay compensaciones registradas.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(compensations, key = { it.id }) { comp ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(comp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                        Text("Dinero extra en presupuesto", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "+$${money(comp.amount)}",
                                        color = Color(0xFFB8FFB8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
