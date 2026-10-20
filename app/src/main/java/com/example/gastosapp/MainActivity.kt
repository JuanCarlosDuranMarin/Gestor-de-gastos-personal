package com.example.gastosapp

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val customDarkColorScheme = darkColorScheme(
                primary = Color(0xFF9B6DD0),
                secondary = Color(0xFF6C3FA4),
                background = Color(0xFF160B21),
                surface = Color(0xFF241033),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color.White,
                onSurface = Color.White
            )

            MaterialTheme(colorScheme = customDarkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF160B21)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

sealed class Screen {
    data object Main : Screen()
    data class WeekDetail(val weekId: String) : Screen()
    data class DayDetail(val weekId: String, val dayName: String) : Screen()
    data object Statistics : Screen()
    data class DirectPurchases(val categoryId: String) : Screen()
    data object Calendar : Screen()
    data object ShoppingLists : Screen()
    data class ShoppingListDetail(val listId: String) : Screen()
}

@Composable
fun AppNavigation() {
    val context = androidx.compose.ui.platform.LocalContext.current

    var monthlyBudget by remember { mutableDoubleStateOf(0.0) }
    var isMonthlySet by remember { mutableStateOf(false) }

    val weeks = remember { mutableStateListOf<WeekRecord>() }
    val directCategories = remember { mutableStateListOf<DirectPurchaseCategory>() }
    val calendarData = remember { mutableStateMapOf<String, CalendarDayStatus>() }
    val compensations = remember { mutableStateListOf<CompensationItem>() }
    val shoppingLists = remember { mutableStateListOf<ShoppingList>() }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var dataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val saved = DataManager.loadData(context)
        monthlyBudget = saved.monthlyBudget
        isMonthlySet = saved.isMonthlySet

        weeks.clear()
        weeks.addAll(saved.weeks)

        directCategories.clear()
        directCategories.addAll(saved.directCategories)

        calendarData.clear()
        calendarData.putAll(saved.calendarData)

        compensations.clear()
        compensations.addAll(saved.compensations)

        shoppingLists.clear()
        shoppingLists.addAll(saved.shoppingLists)

        dataLoaded = true
    }

    LaunchedEffect(
        monthlyBudget,
        isMonthlySet,
        weeks.toList(),
        directCategories.toList(),
        calendarData.toMap(),
        compensations.toList(),
        shoppingLists.toList(),
        dataLoaded
    ) {
        if (dataLoaded) {
            DataManager.saveData(
                context = context,
                monthlyBudget = monthlyBudget,
                isMonthlySet = isMonthlySet,
                weeks = weeks,
                directCategories = directCategories,
                calendarData = calendarData,
                compensations = compensations,
                shoppingLists = shoppingLists
            )
        }
    }

    val totalSpentAllWeeks = weeks.sumOf { it.totalSpent }
    val totalDirectPurchases = directCategories.sumOf { it.totalSpent }
    val totalShoppingListsSpent = shoppingLists.sumOf { it.totalSpent }
    val totalCompensations = compensations.sumOf { it.amount }
    val monthlyRemaining = monthlyBudget - totalSpentAllWeeks - totalDirectPurchases - totalShoppingListsSpent + totalCompensations

    when (val screen = currentScreen) {
        is Screen.Main -> {
            MainScreen(
                isMonthlySet = isMonthlySet,
                monthlyBudget = monthlyBudget,
                monthlyRemaining = monthlyRemaining,
                totalSpentAllWeeks = totalSpentAllWeeks,
                totalDirectPurchases = totalDirectPurchases,
                totalShoppingListsSpent = totalShoppingListsSpent,
                totalCompensations = totalCompensations,
                weeks = weeks,
                directCategories = directCategories,
                compensations = compensations,
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it },
                onSetMonthlyBudget = { budget ->
                    monthlyBudget = budget
                    isMonthlySet = true
                },
                onAddWeek = { weeklyBudget ->
                    val newWeek = WeekRecord(
                        weekNumber = weeks.size + 1,
                        allocatedBudget = weeklyBudget
                    )
                    weeks.add(newWeek)
                },
                onSelectWeek = { week ->
                    selectedTab = 0
                    currentScreen = Screen.WeekDetail(week.id)
                },
                onOpenStatistics = {
                    currentScreen = Screen.Statistics
                },
                onOpenCalendar = {
                    currentScreen = Screen.Calendar
                },
                onOpenShoppingLists = {
                    currentScreen = Screen.ShoppingLists
                },
                onAddDirectCategory = { name, budget ->
                    directCategories.add(DirectPurchaseCategory(categoryName = name, allocatedBudget = budget))
                },
                onAddDirectProducts = { list ->
                    val defaultCategoryName = "Productos Generales"
                    var generalCat = directCategories.find { it.categoryName.equals(defaultCategoryName, ignoreCase = true) }
                    if (generalCat == null) {
                        generalCat = DirectPurchaseCategory(categoryName = defaultCategoryName, allocatedBudget = 0.0)
                        directCategories.add(generalCat)
                    }
                    val catIndex = directCategories.indexOf(generalCat)
                    if (catIndex != -1) {
                        var updatedCat = directCategories[catIndex]
                        list.forEach { item ->
                            updatedCat = updatedCat.addItem(item.name, item.price, item.quantity)
                        }
                        directCategories[catIndex] = updatedCat
                    }
                },
                onAddCompensation = { name, amount ->
                    compensations.add(CompensationItem(name = name, amount = amount))
                },
                onSelectDirectCategory = { category ->
                    selectedTab = 1
                    currentScreen = Screen.DirectPurchases(category.id)
                },
                onDeleteWeek = { weekId ->
                    weeks.removeIf { it.id == weekId }
                },
                onDeleteDirectCategory = { catId ->
                    directCategories.removeIf { it.id == catId }
                },
                onDeleteCompensation = { compId ->
                    compensations.removeIf { it.id == compId }
                },
                onDeleteMonthlyBudget = {
                    monthlyBudget = 0.0
                    isMonthlySet = false
                }
            )
        }

        is Screen.WeekDetail -> {
            val weekIndex = weeks.indexOfFirst { it.id == screen.weekId }
            if (weekIndex != -1) {
                val week = weeks[weekIndex]
                BackHandler {
                    selectedTab = 0
                    currentScreen = Screen.Main
                }

                WeekDetailScreen(
                    week = week,
                    onBack = {
                        selectedTab = 0
                        currentScreen = Screen.Main
                    },
                    onSelectDay = { dayName ->
                        currentScreen = Screen.DayDetail(week.id, dayName)
                    },
                    onUpdateWeekBudget = { newBudget ->
                        weeks[weekIndex] = week.copy(allocatedBudget = newBudget)
                    }
                )
            } else {
                selectedTab = 0
                currentScreen = Screen.Main
            }
        }

        is Screen.DayDetail -> {
            val weekIndex = weeks.indexOfFirst { it.id == screen.weekId }
            if (weekIndex != -1) {
                val week = weeks[weekIndex]
                val day = week.days.find { it.dayName == screen.dayName } ?: week.days.first()
                BackHandler { currentScreen = Screen.WeekDetail(week.id) }

                DayDetailScreen(
                    day = day,
                    week = week,
                    onBack = { currentScreen = Screen.WeekDetail(week.id) },
                    onAddExpenses = { list ->
                        val updatedWeek = week.addMultipleExpenses(day.dayName, list)
                        weeks[weekIndex] = updatedWeek
                    },
                    onUpdateFullExpense = { oldCategory, itemId, newCategory, name, newPrice, newQuantity ->
                        val updatedWeek = week.updateFullExpenseInDay(day.dayName, oldCategory, itemId, newCategory, name, newPrice, newQuantity)
                        weeks[weekIndex] = updatedWeek
                    },
                    onDeleteExpense = { category, itemId ->
                        val updatedWeek = week.deleteExpenseInDay(day.dayName, category, itemId)
                        weeks[weekIndex] = updatedWeek
                    }
                )
            } else {
                currentScreen = Screen.Main
            }
        }

        is Screen.Statistics -> {
            BackHandler { currentScreen = Screen.Main }
            StatisticsScreen(
                weeks = weeks,
                directCategories = directCategories,
                compensations = compensations,
                monthlyBudget = monthlyBudget,
                onBack = { currentScreen = Screen.Main }
            )
        }

        is Screen.DirectPurchases -> {
            BackHandler {
                selectedTab = 1
                currentScreen = Screen.Main
            }
            val categoryIndex = directCategories.indexOfFirst { it.id == screen.categoryId }

            if (categoryIndex != -1) {
                val category = directCategories[categoryIndex]
                DirectPurchasesScreen(
                    category = category,
                    onBack = {
                        selectedTab = 1
                        currentScreen = Screen.Main
                    },
                    onUpdateBudget = { newBudget ->
                        directCategories[categoryIndex] = category.copy(allocatedBudget = newBudget)
                    },
                    onAddItems = { list ->
                        directCategories[categoryIndex] = category.addItems(list)
                    },
                    onDeleteItem = { id ->
                        directCategories[categoryIndex] = category.removeItem(id)
                    }
                )
            } else {
                selectedTab = 1
                currentScreen = Screen.Main
            }
        }

        is Screen.ShoppingLists -> {
            BackHandler { currentScreen = Screen.Main }
            ShoppingListsScreen(
                shoppingLists = shoppingLists,
                onBack = { currentScreen = Screen.Main },
                onAddShoppingList = { title, budget ->
                    shoppingLists.add(ShoppingList(title = title, optionalBudget = budget))
                },
                onUpdateShoppingList = { listId, newTitle, newBudget ->
                    val idx = shoppingLists.indexOfFirst { it.id == listId }
                    if (idx != -1) {
                        shoppingLists[idx] = shoppingLists[idx].updateList(newTitle, newBudget)
                    }
                },
                onSelectList = { sList ->
                    currentScreen = Screen.ShoppingListDetail(sList.id)
                },
                onDeleteShoppingList = { listId ->
                    shoppingLists.removeIf { it.id == listId }
                }
            )
        }

        is Screen.ShoppingListDetail -> {
            val listIndex = shoppingLists.indexOfFirst { it.id == screen.listId }
            if (listIndex != -1) {
                val sList = shoppingLists[listIndex]
                BackHandler {
                    currentScreen = Screen.ShoppingLists
                }
                ShoppingListDetailScreen(
                    shoppingList = sList,
                    onBack = {
                        currentScreen = Screen.ShoppingLists
                    },
                    onAddItem = { name, price ->
                        shoppingLists[listIndex] = sList.addItem(name, price)
                    },
                    onUpdateItem = { itemId, name, price ->
                        shoppingLists[listIndex] = sList.updateItem(itemId, name, price)
                    },
                    onToggleItem = { itemId ->
                        shoppingLists[listIndex] = sList.toggleItemCheck(itemId)
                    },
                    onDeleteItem = { itemId ->
                        shoppingLists[listIndex] = sList.removeItem(itemId)
                    },
                    onUpdateList = { newTitle, newBudget ->
                        shoppingLists[listIndex] = sList.updateList(newTitle, newBudget)
                    },
                    onDeleteList = {
                        shoppingLists.removeAt(listIndex)
                        currentScreen = Screen.ShoppingLists
                    },
                    onResetList = {
                        shoppingLists[listIndex] = sList.resetList()
                    }
                )
            } else {
                currentScreen = Screen.ShoppingLists
            }
        }

        is Screen.Calendar -> {
            BackHandler { currentScreen = Screen.Main }
            PersonalCalendarScreen(
                calendarData = calendarData,
                onBack = { currentScreen = Screen.Main },
                onUpdateDay = { dateKey, status ->
                    if (status != null) {
                        calendarData[dateKey] = status
                    } else {
                        calendarData.remove(dateKey)
                    }
                }
            )
        }
    }
}
