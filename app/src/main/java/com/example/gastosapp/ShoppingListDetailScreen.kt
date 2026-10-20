package com.example.gastosapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListDetailScreen(
    shoppingList: ShoppingList,
    onBack: () -> Unit,
    onAddItem: (String, Double) -> Unit,
    onUpdateItem: (String, String, Double) -> Unit,
    onToggleItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onUpdateList: (String, Double) -> Unit,
    onDeleteList: () -> Unit,
    onResetList: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }

    var itemNameInput by remember { mutableStateOf("") }
    var itemPriceInput by remember { mutableStateOf("") }

    // Estados para edición de producto
    var itemToEdit by remember { mutableStateOf<ShoppingListItem?>(null) }
    var editItemName by remember { mutableStateOf("") }
    var editItemPrice by remember { mutableStateOf("") }

    // Estados para edición de lista
    var showEditListDialog by remember { mutableStateOf(false) }
    var editListTitle by remember { mutableStateOf(shoppingList.title) }
    var editListBudget by remember { mutableStateOf(if (shoppingList.optionalBudget > 0) shoppingList.optionalBudget.toString() else "") }
    var showDeleteListConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF160B21),
        topBar = {
            TopAppBar(
                title = { Text(shoppingList.title, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menú", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false },
                            modifier = Modifier.background(Color(0xFF241033))
                        ) {
                            DropdownMenuItem(
                                text = { Text("✏️ Editar lista", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    editListTitle = shoppingList.title
                                    editListBudget = if (shoppingList.optionalBudget > 0) shoppingList.optionalBudget.toString() else ""
                                    showEditListDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Eliminar lista", color = Color(0xFFFF7070)) },
                                onClick = {
                                    showTopMenu = false
                                    showDeleteListConfirm = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF241033), titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemNameInput = ""
                    itemPriceInput = ""
                    showAddDialog = true
                },
                containerColor = Color(0xFF9B6DD0),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir producto")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 750.dp).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Resumen de la lista
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C3FA4))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("RESUMEN DE LA LISTA", color = Color.White.copy(alpha = .75f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            if (shoppingList.optionalBudget > 0) {
                                Text("Monto / Presupuesto: $${money(shoppingList.optionalBudget)}", color = Color.White, fontSize = 14.sp, maxLines = 1, softWrap = false)
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(
                                "Total Gastado: $${money(shoppingList.totalSpent)}",
                                color = Color(0xFFB8FFB8),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Título de productos
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Productos en la lista", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Toca un producto para editarlo", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        if (shoppingList.items.isNotEmpty()) {
                            TextButton(onClick = { showResetConfirm = true }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = Color(0xFFFF7070), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reiniciar lista", color = Color(0xFFFF7070), fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (shoppingList.items.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                            Text("No hay productos en esta lista. Presiona '+' para agregar uno.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                        }
                    }
                } else {
                    items(shoppingList.items, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        itemToEdit = item
                                        editItemName = item.name
                                        editItemPrice = if (item.price > 0) item.price.toString() else ""
                                    },
                                    onLongClick = {
                                        itemToEdit = item
                                        editItemName = item.name
                                        editItemPrice = if (item.price > 0) item.price.toString() else ""
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { onToggleItem(item.id) },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF9B6DD0))
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            item.name,
                                            color = if (item.isChecked) Color.White else Color.White.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        if (item.price > 0) {
                                            Text(
                                                "$${money(item.price)}",
                                                color = Color(0xFFB8AEB8),
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { onDeleteItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF7070))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para añadir producto a la lista
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Añadir producto", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = itemNameInput,
                            onValueChange = { input ->
                                itemNameInput = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre del producto", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )
                        OutlinedTextField(
                            value = itemPriceInput,
                            onValueChange = { itemPriceInput = it },
                            label = { Text("Precio (opcional)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = itemPriceInput.toDoubleOrNull() ?: 0.0
                            if (itemNameInput.isNotBlank()) {
                                onAddItem(itemNameInput.trim(), price)
                                showAddDialog = false
                                itemNameInput = ""
                                itemPriceInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Añadir", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo para editar producto existente
        if (itemToEdit != null) {
            AlertDialog(
                onDismissRequest = { itemToEdit = null },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Editar producto", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editItemName,
                            onValueChange = { input ->
                                editItemName = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre del producto", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )
                        OutlinedTextField(
                            value = editItemPrice,
                            onValueChange = { editItemPrice = it },
                            label = { Text("Precio (opcional)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = editItemPrice.toDoubleOrNull() ?: 0.0
                            val name = if (editItemName.isNotBlank()) editItemName.trim() else "Producto"
                            itemToEdit?.let { target ->
                                onUpdateItem(target.id, name, price)
                            }
                            itemToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToEdit = null }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo para editar la lista
        if (showEditListDialog) {
            AlertDialog(
                onDismissRequest = { showEditListDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Editar lista", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editListTitle,
                            onValueChange = { input ->
                                editListTitle = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre de la lista", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )
                        OutlinedTextField(
                            value = editListBudget,
                            onValueChange = { editListBudget = it },
                            label = { Text("Precio/Monto opcional", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val budget = editListBudget.toDoubleOrNull() ?: 0.0
                            val title = if (editListTitle.isNotBlank()) editListTitle.trim() else "Lista"
                            onUpdateList(title, budget)
                            showEditListDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditListDialog = false }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo de confirmación para eliminar Lista
        if (showDeleteListConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteListConfirm = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Eliminar Lista", maxLines = 1, softWrap = false) },
                text = { Text("¿Seguro que quieres eliminar '${shoppingList.title}'?", maxLines = 1, softWrap = false) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteList()
                            showDeleteListConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text("Sí, eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteListConfirm = false }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo de confirmación para reiniciar lista
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Reiniciar lista", maxLines = 1, softWrap = false) },
                text = { Text("¿Estás seguro de quitar todos los productos y reiniciar el gasto? El monto/precio total se mantendrá.", maxLines = 3) },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetList()
                            showResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7070))
                    ) {
                        Text("Sí, reiniciar", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }
    }
}
