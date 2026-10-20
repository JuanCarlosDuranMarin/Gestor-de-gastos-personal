package com.example.gastosapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
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
fun ShoppingListsScreen(
    shoppingLists: List<ShoppingList>,
    onBack: () -> Unit,
    onAddShoppingList: (String, Double) -> Unit,
    onUpdateShoppingList: (String, String, Double) -> Unit,
    onSelectList: (ShoppingList) -> Unit,
    onDeleteShoppingList: (String) -> Unit
) {
    var showNewListDialog by remember { mutableStateOf(false) }
    var newListTitleInput by remember { mutableStateOf("") }
    var newListBudgetInput by remember { mutableStateOf("") }

    // Estados para gestión (editar / eliminar)
    var listToManage by remember { mutableStateOf<ShoppingList?>(null) }
    var showEditListDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var editTitleInput by remember { mutableStateOf("") }
    var editBudgetInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFF160B21),
        topBar = {
            TopAppBar(
                title = { Text("Listas de Productos", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
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
                    newListTitleInput = ""
                    newListBudgetInput = ""
                    showNewListDialog = true
                },
                containerColor = Color(0xFF9B6DD0),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir lista")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 750.dp).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                newListTitleInput = ""
                                newListBudgetInput = ""
                                showNewListDialog = true
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir una nueva lista", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }

                if (shoppingLists.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                            Text("No hay listas creadas. Presiona el botón de arriba para crear una.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                        }
                    }
                } else {
                    items(shoppingLists, key = { it.id }) { sList ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onSelectList(sList) },
                                    onLongClick = { listToManage = sList }
                                ),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sList.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                    if (sList.optionalBudget > 0) {
                                        Text("Presupuesto: $${money(sList.optionalBudget)} | Gastado: $${money(sList.totalSpent)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    } else {
                                        Text("Gastado: $${money(sList.totalSpent)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "$${money(sList.totalSpent)}",
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

        // Diálogo para añadir una nueva lista
        if (showNewListDialog) {
            AlertDialog(
                onDismissRequest = { showNewListDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Añadir nueva lista", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newListTitleInput,
                            onValueChange = { input ->
                                newListTitleInput = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre de la lista (ej. Super)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )
                        OutlinedTextField(
                            value = newListBudgetInput,
                            onValueChange = { newListBudgetInput = it },
                            label = { Text("Precio/Monto opcional (ej. 15000)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val budget = newListBudgetInput.toDoubleOrNull() ?: 0.0
                            val title = if (newListTitleInput.isNotBlank()) newListTitleInput.trim() else "Lista de Compras"
                            onAddShoppingList(title, budget)
                            showNewListDialog = false
                            newListTitleInput = ""
                            newListBudgetInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Crear", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewListDialog = false }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo de opciones al mantener presionado una lista (Editar / Eliminar)
        if (listToManage != null && !showEditListDialog && !showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { listToManage = null },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Opciones de lista", maxLines = 1, softWrap = false) },
                text = { Text("¿Qué deseas hacer con '${listToManage?.title}'?", maxLines = 1, softWrap = false) },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = listToManage
                            if (target != null) {
                                editTitleInput = target.title
                                editBudgetInput = if (target.optionalBudget > 0) target.optionalBudget.toString() else ""
                                showEditListDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Editar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmDialog = true
                        }
                    ) {
                        Text("Eliminar", color = Color(0xFFFF7070))
                    }
                }
            )
        }

        // Diálogo para editar la lista
        if (showEditListDialog && listToManage != null) {
            AlertDialog(
                onDismissRequest = {
                    showEditListDialog = false
                    listToManage = null
                },
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
                            value = editTitleInput,
                            onValueChange = { input ->
                                editTitleInput = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre de la lista", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )
                        OutlinedTextField(
                            value = editBudgetInput,
                            onValueChange = { editBudgetInput = it },
                            label = { Text("Precio/Monto opcional", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val budget = editBudgetInput.toDoubleOrNull() ?: 0.0
                            val title = if (editTitleInput.isNotBlank()) editTitleInput.trim() else "Lista"
                            listToManage?.let { target ->
                                onUpdateShoppingList(target.id, title, budget)
                            }
                            showEditListDialog = false
                            listToManage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditListDialog = false
                        listToManage = null
                    }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogo de confirmación para eliminar Lista
        if (showDeleteConfirmDialog && listToManage != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    listToManage = null
                },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Eliminar Lista", maxLines = 1, softWrap = false) },
                text = { Text("¿Seguro que quieres eliminar '${listToManage?.title}'?", maxLines = 1, softWrap = false) },
                confirmButton = {
                    Button(
                        onClick = {
                            listToManage?.let { target ->
                                onDeleteShoppingList(target.id)
                            }
                            showDeleteConfirmDialog = false
                            listToManage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text("Sí, eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirmDialog = false
                        listToManage = null
                    }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }
    }
}
