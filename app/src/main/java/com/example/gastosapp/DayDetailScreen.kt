package com.example.gastosapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DayDetailScreen(
    day: DayRecord,
    week: WeekRecord,
    onBack: () -> Unit,
    onAddExpenses: (List<Pair<TimeCategory, ExpenseItem>>) -> Unit,
    onUpdateFullExpense: (TimeCategory, String, TimeCategory, String, Double, Int) -> Unit,
    onDeleteExpense: (TimeCategory, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showEditExpenseDialog by remember { mutableStateOf(false) }

    // Estados para el flujo de borrado del producto
    var showDeleteMenuDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    var selectedExpenseItem by remember { mutableStateOf<ExpenseItem?>(null) }
    var selectedExpenseCategory by remember { mutableStateOf<TimeCategory>(TimeCategory.MORNING) }

    // Estados para el diálogo de edición completa
    var editCategory by remember { mutableStateOf(TimeCategory.MORNING) }
    var editNameInput by remember { mutableStateOf("") }
    var editPriceInput by remember { mutableStateOf("") }
    var editQuantityInput by remember { mutableStateOf("") }

    // Estados para adición múltiple de productos en el diálogo
    data class SessionExpense(
        val id: String = UUID.randomUUID().toString(),
        val category: TimeCategory,
        val name: String,
        val price: Double,
        val quantity: Int
    )

    val pendingExpenses = remember { mutableStateListOf<SessionExpense>() }
    var selectedCategory by remember { mutableStateOf(TimeCategory.MORNING) }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("1") }

    Scaffold(
        containerColor = Color(0xFF160B21),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        day.dayName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF241033),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pendingExpenses.clear()
                    selectedCategory = TimeCategory.MORNING
                    productName = ""
                    productPrice = ""
                    productQuantity = "1"
                    showDialog = true
                },
                containerColor = Color(0xFF9B6DD0),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar gasto")
            }
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C3FA4))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("RESUMEN DEL DÍA", color = Color.White.copy(alpha = .75f), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "$${money(day.totalSpent)}",
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(Modifier.height(5.dp))
                            Text("Productos registrados: ${day.totalProducts}", color = Color.White, maxLines = 1, softWrap = false)
                            Spacer(Modifier.height(5.dp))
                            Text(
                                "Restante semanal: $${money(week.remainingBudget)}",
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                items(TimeCategory.entries) { category ->
                    val itemsList = day.expenses[category] ?: emptyList()
                    val categoryTotal = itemsList.sumOf { it.price }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${category.icon} ${category.title}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                Text(
                                    "$${money(categoryTotal)}",
                                    color = Color(0xFFB88AE5),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            if (itemsList.isEmpty()) {
                                Text("Sin gastos registrados", color = Color.White.copy(alpha = .5f), maxLines = 1, softWrap = false)
                            } else {
                                itemsList.forEach { item ->
                                    HorizontalDivider(color = Color.White.copy(alpha = .1f))
                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    selectedExpenseItem = item
                                                    selectedExpenseCategory = category
                                                    editCategory = category
                                                    editNameInput = item.name
                                                    editPriceInput = String.format("%.0f", item.price)
                                                    editQuantityInput = item.quantity.toString()
                                                    showEditExpenseDialog = true
                                                },
                                                onLongClick = {
                                                    selectedExpenseItem = item
                                                    selectedExpenseCategory = category
                                                    showDeleteMenuDialog = true
                                                }
                                            ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                            Text("Precio unitario: $${money(item.price)} | Cant: ${item.quantity}", color = Color.White.copy(alpha = .65f), fontSize = 12.sp, maxLines = 1, softWrap = false)
                                        }

                                        Text(
                                            "$${money(item.total)}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Diálogo al mantener presionado: Botón "Borrar producto"
        if (showDeleteMenuDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteMenuDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Opciones de producto", maxLines = 1, softWrap = false) },
                text = { Text("¿Qué deseas hacer con '${selectedExpenseItem?.name}'?", maxLines = 1, softWrap = false) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteMenuDialog = false
                            showConfirmDeleteDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7070))
                    ) {
                        Text("Borrar producto")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteMenuDialog = false }) {
                        Text("Cancelar", color = Color.White)
                    }
                }
            )
        }

        // 2. Diálogo de confirmación de borrado
        if (showConfirmDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDeleteDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Confirmar eliminación", maxLines = 1, softWrap = false) },
                text = { Text("¿Seguro que quieres eliminar este producto?", maxLines = 1, softWrap = false) },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedExpenseItem?.let {
                                onDeleteExpense(selectedExpenseCategory, it.id)
                            }
                            showConfirmDeleteDialog = false
                            selectedExpenseItem = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showConfirmDeleteDialog = false
                            selectedExpenseItem = null
                        }
                    ) {
                        Text("No", color = Color.White)
                    }
                }
            )
        }

        // Diálogo para añadir producto con adición múltiple minimizada
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Registrar Productos", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Formulario Activo (Actual) en la parte superior
                        Text("Franja horaria:", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, softWrap = false)
                        TimeCategory.entries.forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                                Text("${cat.icon} ${cat.title}", fontSize = 13.sp, maxLines = 1, softWrap = false)
                            }
                        }

                        OutlinedTextField(
                            value = productName,
                            onValueChange = { input ->
                                productName = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre del producto", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            placeholder = { Text("Ej. Pan", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )

                        OutlinedTextField(
                            value = productPrice,
                            onValueChange = { productPrice = it },
                            label = { Text("Precio unitario", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            placeholder = { Text("Ej. 1500", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = productQuantity,
                            onValueChange = { productQuantity = it },
                            label = { Text("Cantidad", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            placeholder = { Text("Ej. 1", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Botón "Agregar otro producto"
                        Button(
                            onClick = {
                                val price = productPrice.toDoubleOrNull() ?: 0.0
                                val qty = productQuantity.toIntOrNull() ?: 1
                                if (productName.isNotBlank() && price > 0 && qty > 0) {
                                    val newSessionExp = SessionExpense(
                                        category = selectedCategory,
                                        name = productName.trim(),
                                        price = price,
                                        quantity = qty
                                    )
                                    pendingExpenses.add(newSessionExp)
                                    productName = ""
                                    productPrice = ""
                                    productQuantity = "1"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C3FA4))
                        ) {
                            Text("Agregar otro producto", maxLines = 1, softWrap = false)
                        }

                        // Abajo de eso: listado de productos ya registrados en esta sesión (minimizados)
                        if (pendingExpenses.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Text("Registrados en esta sesión:", color = Color(0xFFB88AE5), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)

                            pendingExpenses.forEachIndexed { index, sessionExp ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F0F2E))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${sessionExp.category.icon} ${sessionExp.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, softWrap = false)
                                            Text("Cant: ${sessionExp.quantity} x $${money(sessionExp.price)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                        }
                                        IconButton(onClick = { pendingExpenses.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = Color(0xFFFF7070))
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
                            val price = productPrice.toDoubleOrNull() ?: 0.0
                            val qty = productQuantity.toIntOrNull() ?: 1
                            if (productName.isNotBlank() && price > 0 && qty > 0) {
                                val finalExp = SessionExpense(
                                    category = selectedCategory,
                                    name = productName.trim(),
                                    price = price,
                                    quantity = qty
                                )
                                pendingExpenses.add(finalExp)
                            }

                            // Registrar todos en una lista acumulada
                            val listToAdd = mutableListOf<Pair<TimeCategory, ExpenseItem>>()
                            pendingExpenses.forEach { exp ->
                                listToAdd.add(
                                    exp.category to ExpenseItem(
                                        name = exp.name,
                                        price = exp.price,
                                        quantity = exp.quantity
                                    )
                                )
                            }

                            if (listToAdd.isNotEmpty()) {
                                onAddExpenses(listToAdd)
                            }

                            showDialog = false
                            pendingExpenses.clear()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar todos", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        pendingExpenses.clear()
                    }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo para editar completo un producto existente (Franja horaria, Nombre, Precio, Cantidad)
        if (showEditExpenseDialog && selectedExpenseItem != null) {
            AlertDialog(
                onDismissRequest = { showEditExpenseDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Editar producto", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Franja horaria:", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, softWrap = false)
                        TimeCategory.entries.forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = editCategory == cat,
                                    onClick = { editCategory = cat }
                                )
                                Text("${cat.icon} ${cat.title}", fontSize = 13.sp, maxLines = 1, softWrap = false)
                            }
                        }

                        OutlinedTextField(
                            value = editNameInput,
                            onValueChange = { input ->
                                editNameInput = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre del producto", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )

                        OutlinedTextField(
                            value = editPriceInput,
                            onValueChange = { editPriceInput = it },
                            label = { Text("Precio unitario", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = editQuantityInput,
                            onValueChange = { editQuantityInput = it },
                            label = { Text("Cantidad", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = editPriceInput.toDoubleOrNull() ?: 0.0
                            val qty = editQuantityInput.toIntOrNull() ?: 1
                            if (editNameInput.isNotBlank() && price > 0 && qty > 0) {
                                selectedExpenseItem?.let { item ->
                                    onUpdateFullExpense(
                                        selectedExpenseCategory,
                                        item.id,
                                        editCategory,
                                        editNameInput.trim(),
                                        price,
                                        qty
                                    )
                                }
                                showEditExpenseDialog = false
                                selectedExpenseItem = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditExpenseDialog = false }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }
    }
}
