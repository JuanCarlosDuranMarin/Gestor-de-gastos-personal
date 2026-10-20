package com.example.gastosapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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

// Función auxiliar local para formatear el dinero
private fun formatMoney(amount: Double): String {
    return String.format("%,.0f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectPurchasesScreen(
    category: DirectPurchaseCategory,
    onBack: () -> Unit,
    onUpdateBudget: (Double) -> Unit,
    onAddItems: (List<DirectPurchaseItem>) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val isGeneral = category.categoryName.equals("Productos Generales", ignoreCase = true)

    // Estados para adición múltiple minimizada
    data class SessionDirectItem(
        val name: String,
        val price: Double,
        val quantity: Int
    )

    val pendingItems = remember { mutableStateListOf<SessionDirectItem>() }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("1") }

    var budgetInput by remember { mutableStateOf(category.allocatedBudget.toString()) }

    Scaffold(
        containerColor = Color(0xFF160B21),
        topBar = {
            TopAppBar(
                title = { Text(category.categoryName, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF241033), titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pendingItems.clear()
                    productName = ""
                    productPrice = ""
                    productQuantity = "1"
                    showAddDialog = true
                },
                containerColor = Color(0xFF9B6DD0),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 750.dp).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tarjeta Resumen Editable o Total General
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C3FA4))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (isGeneral) "TOTAL GASTADO EN PRODUCTOS" else "PRESUPUESTO CATEGORÍA",
                                    color = Color.White.copy(alpha = .75f),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (!isGeneral) {
                                    IconButton(onClick = { showBudgetDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar presupuesto", tint = Color.White)
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$${formatMoney(category.totalSpent)}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Total de productos: ${category.totalProducts}", color = Color.White, maxLines = 1, softWrap = false)
                            if (!isGeneral) {
                                Spacer(Modifier.height(4.dp))
                                Text("Restante: $${formatMoney(category.remainingBudget)}", color = Color.White, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                // Lista de productos
                items(category.items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                Spacer(Modifier.height(4.dp))
                                Text("Cant: ${item.quantity} x $${formatMoney(item.price)}", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, maxLines = 1, softWrap = false)
                            }
                            Spacer(Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "$${formatMoney(item.total)}",
                                    color = Color(0xFFB88AE5),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { onDeleteItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF7070))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para agregar producto (con adición múltiple minimizada)
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Agregar Productos", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Productos ya agregados en esta sesión (minimizados)
                        pendingItems.forEachIndexed { index, sessionItem ->
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
                                        Text(sessionItem.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, softWrap = false)
                                        Text("Cant: ${sessionItem.quantity} x $${formatMoney(sessionItem.price)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                    IconButton(onClick = { pendingItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = Color(0xFFFF7070))
                                    }
                                }
                            }
                        }

                        if (pendingItems.isNotEmpty()) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Text("Nuevo producto (se creará arriba):", color = Color(0xFFB88AE5), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        // Formulario Activo en la parte superior
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { input ->
                                productName = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre del producto", maxLines = 1, softWrap = false, fontSize = 12.sp) },
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
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = productQuantity,
                            onValueChange = { productQuantity = it },
                            label = { Text("Cantidad", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Botón "Agregar otro producto"
                        Button(
                            onClick = {
                                val price = productPrice.toDoubleOrNull() ?: 0.0
                                val qty = productQuantity.toIntOrNull() ?: 1
                                if (productName.isNotBlank() && price > 0) {
                                    pendingItems.add(SessionDirectItem(productName.trim(), price, qty))
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
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = productPrice.toDoubleOrNull() ?: 0.0
                            val qty = productQuantity.toIntOrNull() ?: 1
                            if (productName.isNotBlank() && price > 0) {
                                pendingItems.add(SessionDirectItem(productName.trim(), price, qty))
                            }

                            val itemsToAdd = mutableListOf<DirectPurchaseItem>()
                            pendingItems.forEach { item ->
                                itemsToAdd.add(
                                    DirectPurchaseItem(
                                        name = item.name,
                                        price = item.price,
                                        quantity = item.quantity
                                    )
                                )
                            }

                            if (itemsToAdd.isNotEmpty()) {
                                onAddItems(itemsToAdd)
                            }

                            showAddDialog = false
                            pendingItems.clear()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar todos", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        pendingItems.clear()
                    }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo para editar presupuesto de la categoría (si no es general)
        if (showBudgetDialog && !isGeneral) {
            AlertDialog(
                onDismissRequest = { showBudgetDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Editar presupuesto", maxLines = 1, softWrap = false) },
                text = {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Nuevo presupuesto", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newBudget = budgetInput.toDoubleOrNull() ?: category.allocatedBudget
                            onUpdateBudget(newBudget)
                            showBudgetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBudgetDialog = false }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }
    }
}
