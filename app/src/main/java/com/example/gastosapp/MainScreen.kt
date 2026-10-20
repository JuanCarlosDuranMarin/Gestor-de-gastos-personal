package com.example.gastosapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    isMonthlySet: Boolean,
    monthlyBudget: Double,
    monthlyRemaining: Double,
    totalSpentAllWeeks: Double,
    totalDirectPurchases: Double,
    totalShoppingListsSpent: Double,
    totalCompensations: Double,
    weeks: List<WeekRecord>,
    directCategories: List<DirectPurchaseCategory>,
    compensations: List<CompensationItem>,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onSetMonthlyBudget: (Double) -> Unit,
    onAddWeek: (Double) -> Unit,
    onAddDirectCategory: (String, Double) -> Unit,
    onAddDirectProducts: (List<DirectPurchaseItem>) -> Unit,
    onAddCompensation: (String, Double) -> Unit,
    onSelectWeek: (WeekRecord) -> Unit,
    onSelectDirectCategory: (DirectPurchaseCategory) -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenShoppingLists: () -> Unit,
    onDeleteWeek: (String) -> Unit,
    onDeleteDirectCategory: (String) -> Unit,
    onDeleteCompensation: (String) -> Unit,
    onDeleteMonthlyBudget: () -> Unit
) {
    var showMonthlyDialog by remember(isMonthlySet) { mutableStateOf(!isMonthlySet) }
    var showWeekDialog by remember { mutableStateOf(false) }
    var showDirectBudgetDialog by remember { mutableStateOf(false) }
    var showDirectProductDialog by remember { mutableStateOf(false) }
    var showCompensationDialog by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }

    var directNameInput by remember { mutableStateOf("") }
    var compensationNameInput by remember { mutableStateOf("") }
    var compensationAmountInput by remember { mutableStateOf("") }

    // Estados para adición múltiple de productos sueltos
    data class SessionDirectProduct(
        val name: String,
        val price: Double,
        val quantity: Int
    )
    val pendingDirectProducts = remember { mutableStateListOf<SessionDirectProduct>() }
    var prodName by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodQuantity by remember { mutableStateOf("1") }

    var showBudgetOptions by remember { mutableStateOf(false) }
    var showDeleteBudgetConfirm by remember { mutableStateOf(false) }

    var budgetInput by remember { mutableStateOf("") }
    var weeklyBudgetInput by remember { mutableStateOf("") }
    var directBudgetInput by remember { mutableStateOf("") }

    // Estados para confirmación de borrado
    var weekToDelete by remember { mutableStateOf<WeekRecord?>(null) }
    var categoryToDelete by remember { mutableStateOf<DirectPurchaseCategory?>(null) }
    var compensationToDelete by remember { mutableStateOf<CompensationItem?>(null) }

    val pagerState = rememberPagerState(
        initialPage = selectedTab,
        pageCount = { 3 }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            onTabChange(pagerState.currentPage)
        }
    }
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    Scaffold(
        containerColor = Color(0xFF160B21),
        topBar = {
            TopAppBar(
                title = { Text("Control de Gastos", fontWeight = FontWeight.Bold, color = Color.White) },
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
                                text = { Text("📊 Análisis", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    onOpenStatistics()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📅 Calendario", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    onOpenCalendar()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📝 Listas de Productos", color = Color.White) },
                                onClick = {
                                    showTopMenu = false
                                    onOpenShoppingLists()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF241033))
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showAddMenu = true },
                    containerColor = Color(0xFF9B6DD0),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                    modifier = Modifier.background(Color(0xFF241033))
                ) {
                    DropdownMenuItem(
                        text = { Text("Añadir Semana", color = Color.White) },
                        onClick = {
                            showAddMenu = false
                            showWeekDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Añadir Bloque de Producto", color = Color.White) },
                        onClick = {
                            showAddMenu = false
                            showDirectBudgetDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Añadir un Producto", color = Color.White) },
                        onClick = {
                            showAddMenu = false
                            pendingDirectProducts.clear()
                            prodName = ""
                            prodPrice = ""
                            prodQuantity = "1"
                            showDirectProductDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Agregar Compensación", color = Color.White) },
                        onClick = {
                            showAddMenu = false
                            showCompensationDialog = true
                        }
                    )
                }
            }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta Resumen Mensual (Fija arriba)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { },
                            onLongClick = { showBudgetOptions = true }
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF6C3FA4))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Parte superior: Sueldo mensual a la izquierda, Compensación arriba a la derecha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SUELDO MENSUAL", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "$${money(monthlyBudget)}",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Compensación", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                Text(
                                    "+$${money(totalCompensations)}",
                                    color = Color(0xFFB8FFB8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(Modifier.height(4.dp))

                        // Parte inferior: Gastado Seman., Gastado Prod. y Gastado Lista juntos a la izquierda, Restante a la derecha a su misma altura
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(2f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column {
                                    Text("Gastado Seman.", color = Color.White.copy(alpha = 0.75f), fontSize = 9.sp, maxLines = 1, softWrap = false)
                                    Text(
                                        "$${money(totalSpentAllWeeks)}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                Column {
                                    Text("Gastado Prod.", color = Color.White.copy(alpha = 0.75f), fontSize = 9.sp, maxLines = 1, softWrap = false)
                                    Text(
                                        "$${money(totalDirectPurchases)}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                Column {
                                    Text("Gastado Lista", color = Color.White.copy(alpha = 0.75f), fontSize = 9.sp, maxLines = 1, softWrap = false)
                                    Text(
                                        "$${money(totalShoppingListsSpent)}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(Modifier.width(4.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Restante", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp, maxLines = 1, softWrap = false)
                                Text(
                                    "$${money(monthlyRemaining)}",
                                    color = if (monthlyRemaining >= 0) Color(0xFFB8FFB8) else Color(0xFFFF8080),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Pestañas superiores (3 pestañas: Semanas, Productos, Compensación)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF241033), RoundedCornerShape(16.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val activeBg = Color(0xFF9B6DD0)
                    val inactiveBg = Color.White.copy(alpha = 0.05f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selectedTab == 0) activeBg else inactiveBg, RoundedCornerShape(10.dp))
                            .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📅 Semanas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selectedTab == 1) activeBg else inactiveBg, RoundedCornerShape(10.dp))
                            .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛍️ Productos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selectedTab == 2) activeBg else inactiveBg, RoundedCornerShape(10.dp))
                            .clickable { coroutineScope.launch { pagerState.animateScrollToPage(2) } }
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💵 Compensac.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1, softWrap = false)
                    }
                }

                // HorizontalPager para deslizar entre 3 páginas
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (page) {
                            0 -> {
                                item {
                                    Column {
                                        Text("Listado de Semanas", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                        Spacer(Modifier.height(2.dp))
                                        Text("Mantén apretado para eliminar", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1, softWrap = false)
                                    }
                                }

                                if (weeks.isEmpty()) {
                                    item {
                                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                                            Text("No hay semanas creadas. Presiona '+' para añadir una.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    items(weeks, key = { it.id }) { week ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = { onSelectWeek(week) },
                                                    onLongClick = { weekToDelete = week }
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
                                                    Text("Semana ${week.weekNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                                    Text("Presupuesto: $${money(week.allocatedBudget)} | Gastado: $${money(week.totalSpent)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    "$${money(week.remainingBudget)}",
                                                    color = if (week.remainingBudget >= 0) Color(0xFFB8FFB8) else Color(0xFFFF8080),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                item {
                                    Column {
                                        Text("Listado de Productos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                        Spacer(Modifier.height(2.dp))
                                        Text("Mantén apretado para eliminar", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1, softWrap = false)
                                    }
                                }

                                if (directCategories.isEmpty()) {
                                    item {
                                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                                            Text("No hay bloques de productos creados. Presiona '+' para añadir uno.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    items(directCategories, key = { it.id }) { cat ->
                                        val isGeneral = cat.categoryName.equals("Productos Generales", ignoreCase = true)
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = { onSelectDirectCategory(cat) },
                                                    onLongClick = { categoryToDelete = cat }
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
                                                    Text(cat.categoryName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                                    if (isGeneral) {
                                                        Text("Gastado: $${money(cat.totalSpent)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                                    } else {
                                                        Text("Tope: $${money(cat.allocatedBudget)} | Gastado: $${money(cat.totalSpent)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                                    }
                                                }
                                                if (!isGeneral) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        "$${money(cat.remainingBudget)}",
                                                        color = if (cat.remainingBudget >= 0) Color(0xFFB8FFB8) else Color(0xFFFF8080),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                item {
                                    Column {
                                        Text("Listado de Compensaciones", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                        Spacer(Modifier.height(2.dp))
                                        Text("Mantén apretado para eliminar", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1, softWrap = false)
                                    }
                                }

                                if (compensations.isEmpty()) {
                                    item {
                                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241033))) {
                                            Text("No hay compensaciones registradas. Presiona '+' para añadir dinero extra.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    items(compensations, key = { it.id }) { comp ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = { },
                                                    onLongClick = { compensationToDelete = comp }
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
                                                    Text(comp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, softWrap = false)
                                                    Text("Dinero extra añadido al restante", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
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
        }

        // Diálogo de confirmación para eliminar Semana
        if (weekToDelete != null) {
            AlertDialog(
                onDismissRequest = { weekToDelete = null },
                containerColor = Color(0xFF241033),
                title = { Text("Eliminar Semana", color = Color.White) },
                text = { Text("¿Seguro que quieres eliminar esta semana y todos sus registros?", color = Color.White) },
                confirmButton = {
                    Button(onClick = {
                        weekToDelete?.let { onDeleteWeek(it.id) }
                        weekToDelete = null
                    }) { Text("Sí") }
                },
                dismissButton = {
                    TextButton(onClick = { weekToDelete = null }) { Text("No", color = Color.White) }
                }
            )
        }

        // Diálogo de confirmación para eliminar Bloque de Producto
        if (categoryToDelete != null) {
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                containerColor = Color(0xFF241033),
                title = { Text("Eliminar Bloque", color = Color.White) },
                text = { Text("¿Seguro que quieres eliminar este bloque de productos?", color = Color.White) },
                confirmButton = {
                    Button(onClick = {
                        categoryToDelete?.let { onDeleteDirectCategory(it.id) }
                        categoryToDelete = null
                    }) { Text("Sí") }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) { Text("No", color = Color.White) }
                }
            )
        }

        // Diálogo de confirmación para eliminar Compensación
        if (compensationToDelete != null) {
            AlertDialog(
                onDismissRequest = { compensationToDelete = null },
                containerColor = Color(0xFF241033),
                title = { Text("Eliminar Compensación", color = Color.White) },
                text = { Text("¿Seguro que quieres eliminar esta compensación?", color = Color.White) },
                confirmButton = {
                    Button(onClick = {
                        compensationToDelete?.let { onDeleteCompensation(it.id) }
                        compensationToDelete = null
                    }) { Text("Sí") }
                },
                dismissButton = {
                    TextButton(onClick = { compensationToDelete = null }) { Text("No", color = Color.White) }
                }
            )
        }

        // Diálogo para añadir un producto suelto (Añadir un Producto)
        if (showDirectProductDialog) {
            AlertDialog(
                onDismissRequest = { showDirectProductDialog = false },
                containerColor = Color(0xFF241033),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Añadir un Producto", maxLines = 1, softWrap = false) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Productos ya agregados en esta sesión (minimizados)
                        pendingDirectProducts.forEachIndexed { index, sessionProd ->
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
                                        Text(sessionProd.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, softWrap = false)
                                        Text("Cant: ${sessionProd.quantity} x $${money(sessionProd.price)}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    }
                                    IconButton(onClick = { pendingDirectProducts.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = Color(0xFFFF7070))
                                    }
                                }
                            }
                        }

                        if (pendingDirectProducts.isNotEmpty()) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Text("Nuevo producto (se creará arriba):", color = Color(0xFFB88AE5), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        OutlinedTextField(
                            value = prodName,
                            onValueChange = { input ->
                                prodName = if (input.isNotEmpty()) input.replaceFirstChar { it.uppercase() } else input
                            },
                            label = { Text("Nombre del producto", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            )
                        )

                        OutlinedTextField(
                            value = prodPrice,
                            onValueChange = { prodPrice = it },
                            label = { Text("Precio unitario", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = prodQuantity,
                            onValueChange = { prodQuantity = it },
                            label = { Text("Cantidad", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Botón "Agregar otro producto"
                        Button(
                            onClick = {
                                val price = prodPrice.toDoubleOrNull() ?: 0.0
                                val qty = prodQuantity.toIntOrNull() ?: 1
                                if (prodName.isNotBlank() && price > 0) {
                                    pendingDirectProducts.add(SessionDirectProduct(prodName.trim(), price, qty))
                                    prodName = ""
                                    prodPrice = ""
                                    prodQuantity = "1"
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
                            val price = prodPrice.toDoubleOrNull() ?: 0.0
                            val qty = prodQuantity.toIntOrNull() ?: 1
                            if (prodName.isNotBlank() && price > 0) {
                                pendingDirectProducts.add(SessionDirectProduct(prodName.trim(), price, qty))
                            }

                            val itemsToAdd = mutableListOf<DirectPurchaseItem>()
                            pendingDirectProducts.forEach { item ->
                                itemsToAdd.add(
                                    DirectPurchaseItem(
                                        name = item.name,
                                        price = item.price,
                                        quantity = item.quantity
                                    )
                                )
                            }

                            if (itemsToAdd.isNotEmpty()) {
                                onAddDirectProducts(itemsToAdd)
                            }

                            showDirectProductDialog = false
                            pendingDirectProducts.clear()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) {
                        Text("Guardar todos", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDirectProductDialog = false
                        pendingDirectProducts.clear()
                    }) {
                        Text("Cancelar", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        // Diálogos generales de creación...
        if (showMonthlyDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (isMonthlySet) {
                        showMonthlyDialog = false
                    }
                },
                containerColor = Color(0xFF241033),
                title = { Text(if (isMonthlySet) "Editar Sueldo Mensual" else "Configurar Sueldo Mensual", color = Color.White, maxLines = 1, softWrap = false) },
                text = {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Monto mensual", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF9B6DD0)
                        )
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val amount = budgetInput.toDoubleOrNull()
                        if (amount != null && amount > 0.0) {
                            onSetMonthlyBudget(amount)
                            showMonthlyDialog = false
                            budgetInput = ""
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    if (isMonthlySet) {
                        TextButton(onClick = { showMonthlyDialog = false }) {
                            Text("Cancelar", color = Color.White)
                        }
                    }
                }
            )
        }

        // Diálogo de opciones al mantener presionado el sueldo
        if (showBudgetOptions) {
            AlertDialog(
                onDismissRequest = { showBudgetOptions = false },
                containerColor = Color(0xFF241033),
                title = { Text("Opciones de Sueldo", color = Color.White) },
                text = { Text("¿Qué deseas hacer con el sueldo mensual?", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = {
                            showBudgetOptions = false
                            budgetInput = String.format("%.0f", monthlyBudget)
                            showMonthlyDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6DD0))
                    ) { Text("Editar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showBudgetOptions = false
                        showDeleteBudgetConfirm = true
                    }) { Text("Eliminar", color = Color(0xFFFF8080)) }
                }
            )
        }

        // Diálogo de confirmación de eliminación de sueldo
        if (showDeleteBudgetConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteBudgetConfirm = false },
                containerColor = Color(0xFF241033),
                title = { Text("¿Eliminar Sueldo?", color = Color.White) },
                text = { Text("¿Estás seguro de eliminar el monto mensual? No se borrarán tus semanas ni productos.", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteMonthlyBudget()
                            showDeleteBudgetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) { Text("Sí, Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteBudgetConfirm = false }) {
                        Text("No, Cancelar", color = Color.White)
                    }
                }
            )
        }

        if (showWeekDialog) {
            AlertDialog(
                onDismissRequest = { showWeekDialog = false },
                containerColor = Color(0xFF241033),
                title = { Text("Añadir Nueva Semana", color = Color.White, maxLines = 1, softWrap = false) },
                text = {
                    OutlinedTextField(
                        value = weeklyBudgetInput,
                        onValueChange = { weeklyBudgetInput = it },
                        label = { Text("Presupuesto de la semana", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val amount = weeklyBudgetInput.toDoubleOrNull() ?: 0.0
                        if (amount >= 0.0) {
                            onAddWeek(amount)
                            showWeekDialog = false
                            weeklyBudgetInput = ""
                        }
                    }) { Text("Crear") }
                }
            )
        }

        if (showDirectBudgetDialog) {
            AlertDialog(
                onDismissRequest = { showDirectBudgetDialog = false },
                containerColor = Color(0xFF241033),
                title = { Text("Añadir Bloque de Producto", color = Color.White, maxLines = 1, softWrap = false) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = directNameInput,
                            onValueChange = { directNameInput = it },
                            label = { Text("Nombre (ej. Supermercado)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF9B6DD0)
                            )
                        )
                        OutlinedTextField(
                            value = directBudgetInput,
                            onValueChange = { directBudgetInput = it },
                            label = { Text("Total de gasto (ej. 20000)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF9B6DD0)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amount = directBudgetInput.toDoubleOrNull() ?: 0.0
                        val name = if (directNameInput.isNotBlank()) directNameInput.trim() else "Producto ${directCategories.size + 1}"
                        if (amount >= 0.0) {
                            onAddDirectCategory(name, amount)
                            showDirectBudgetDialog = false
                            directBudgetInput = ""
                            directNameInput = ""
                        }
                    }) { Text("Crear") }
                }
            )
        }

        if (showCompensationDialog) {
            AlertDialog(
                onDismissRequest = { showCompensationDialog = false },
                containerColor = Color(0xFF241033),
                title = { Text("Agregar Compensación", color = Color.White, maxLines = 1, softWrap = false) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = compensationNameInput,
                            onValueChange = { compensationNameInput = it },
                            label = { Text("Concepto (ej. Devolución)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF9B6DD0)
                            )
                        )
                        OutlinedTextField(
                            value = compensationAmountInput,
                            onValueChange = { compensationAmountInput = it },
                            label = { Text("Cantidad de dinero (ej. 5000)", maxLines = 1, softWrap = false, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF9B6DD0)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amount = compensationAmountInput.toDoubleOrNull() ?: 0.0
                        val name = if (compensationNameInput.isNotBlank()) compensationNameInput.trim() else "Compensación"
                        if (amount > 0.0) {
                            onAddCompensation(name, amount)
                            showCompensationDialog = false
                            compensationAmountInput = ""
                            compensationNameInput = ""
                        }
                    }) { Text("Agregar") }
                }
            )
        }
    }
}

fun money(value: Double): String = String.format("%,.0f", value)
