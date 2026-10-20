package com.example.gastosapp

import java.util.UUID

enum class CalendarDayStatus {
    ATTENDED,    // Vino a buscarme (Punto Rosado)
    ABSENT,      // No vino a buscarme (Punto Morado Oscuro)
    UNAVAILABLE  // No disponible / Feriado (Punto Gris)
}

data class DirectPurchaseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val quantity: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val date: Long = System.currentTimeMillis()
) {
    val total: Double
        get() = price
}

data class DirectPurchaseCategory(
    val id: String = UUID.randomUUID().toString(),
    val categoryName: String = "Productos Generales",
    val allocatedBudget: Double = 0.0,
    val items: List<DirectPurchaseItem> = emptyList()
) {
    val totalSpent: Double
        get() = items.sumOf { it.price }

    val totalProducts: Int
        get() = items.sumOf { it.quantity }

    val remainingBudget: Double
        get() = if (categoryName.equals("Productos Generales", ignoreCase = true)) 0.0 else allocatedBudget - totalSpent

    fun addItem(
        name: String,
        price: Double,
        quantity: Int,
        date: Long = System.currentTimeMillis()
    ): DirectPurchaseCategory {
        val newItem = DirectPurchaseItem(
            name = name,
            price = price,
            quantity = quantity,
            date = date
        )
        return copy(items = items + newItem)
    }

    fun addItems(newItems: List<DirectPurchaseItem>): DirectPurchaseCategory {
        return copy(items = items + newItems)
    }

    fun removeItem(itemId: String): DirectPurchaseCategory {
        val updatedItems = items.filter { it.id != itemId }
        return copy(items = updatedItems)
    }

    fun updateItemPrice(itemId: String, newPrice: Double): DirectPurchaseCategory {
        val updatedItems = items.map { item ->
            if (item.id == itemId) item.copy(price = newPrice) else item
        }
        return copy(items = updatedItems)
    }
}

data class ExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val quantity: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val date: Long = System.currentTimeMillis()
) {
    val total: Double
        get() = price
}

enum class TimeCategory(
    val title: String,
    val icon: String
) {
    MORNING("Mañana", "🌅"),
    NOON("Mediodía", "☀️"),
    AFTERNOON("Tarde", "🌆"),
    NIGHT("Noche", "🌙")
}

fun getDayNameFromDate(date: Long): String {
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = date }
    return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "Lunes"
        java.util.Calendar.TUESDAY -> "Martes"
        java.util.Calendar.WEDNESDAY -> "Miércoles"
        java.util.Calendar.THURSDAY -> "Jueves"
        java.util.Calendar.FRIDAY -> "Viernes"
        java.util.Calendar.SATURDAY -> "Sábado"
        java.util.Calendar.SUNDAY -> "Domingo"
        else -> "Lunes"
    }
}

data class DayRecord(
    val dayName: String,
    val expenses: Map<TimeCategory, List<ExpenseItem>> = mapOf(
        TimeCategory.MORNING to emptyList(),
        TimeCategory.NOON to emptyList(),
        TimeCategory.AFTERNOON to emptyList(),
        TimeCategory.NIGHT to emptyList()
    )
) {
    val totalSpent: Double
        get() = expenses.values
            .flatten()
            .sumOf { it.price }

    val totalProducts: Int
        get() = expenses.values
            .flatten()
            .sumOf { it.quantity }

    fun addExpense(
        category: TimeCategory,
        item: ExpenseItem
    ): DayRecord {
        val currentList = expenses[category] ?: emptyList()
        val updatedMap = expenses.toMutableMap()
        updatedMap[category] = currentList + item
        return copy(expenses = updatedMap)
    }

    fun updateExpense(
        category: TimeCategory,
        itemId: String,
        newPrice: Double
    ): DayRecord {
        val currentList = expenses[category] ?: emptyList()
        val updatedList = currentList.map { item ->
            if (item.id == itemId) item.copy(price = newPrice) else item
        }
        val updatedMap = expenses.toMutableMap()
        updatedMap[category] = updatedList
        return copy(expenses = updatedMap)
    }

    fun updateFullExpense(
        oldCategory: TimeCategory,
        itemId: String,
        newCategory: TimeCategory,
        newName: String,
        newPrice: Double,
        newQuantity: Int
    ): DayRecord {
        val oldList = expenses[oldCategory] ?: emptyList()
        val itemToUpdate = oldList.find { it.id == itemId } ?: return this
        val updatedItem = itemToUpdate.copy(
            name = newName,
            price = newPrice,
            quantity = newQuantity
        )

        val updatedMap = expenses.toMutableMap()
        if (oldCategory == newCategory) {
            updatedMap[oldCategory] = oldList.map { if (it.id == itemId) updatedItem else it }
        } else {
            updatedMap[oldCategory] = oldList.filter { it.id != itemId }
            val newList = (updatedMap[newCategory] ?: emptyList()) + updatedItem
            updatedMap[newCategory] = newList
        }
        return copy(expenses = updatedMap)
    }

    // Nuevo método para eliminar un gasto específico
    fun deleteExpense(
        category: TimeCategory,
        itemId: String
    ): DayRecord {
        val currentList = expenses[category] ?: emptyList()
        val updatedList = currentList.filter { it.id != itemId }
        val updatedMap = expenses.toMutableMap()
        updatedMap[category] = updatedList
        return copy(expenses = updatedMap)
    }
}

data class WeekRecord(
    val id: String = UUID.randomUUID().toString(),
    val weekNumber: Int,
    val allocatedBudget: Double,
    val days: List<DayRecord> = listOf(
        DayRecord("Lunes"),
        DayRecord("Martes"),
        DayRecord("Miércoles"),
        DayRecord("Jueves"),
        DayRecord("Viernes"),
        DayRecord("Sábado"),
        DayRecord("Domingo")
    )
) {
    val totalSpent: Double
        get() = days.sumOf { it.totalSpent }

    val totalProducts: Int
        get() = days.sumOf { it.totalProducts }

    val remainingBudget: Double
        get() = allocatedBudget - totalSpent

    fun addExpenseToDay(
        dayName: String,
        category: TimeCategory,
        item: ExpenseItem
    ): WeekRecord {
        val updatedDays = days.map { day ->
            if (day.dayName == dayName) {
                day.addExpense(category, item)
            } else {
                day
            }
        }
        return copy(days = updatedDays)
    }

    fun addMultipleExpenses(
        dayName: String,
        expensesList: List<Pair<TimeCategory, ExpenseItem>>
    ): WeekRecord {
        var updatedWeek = this
        expensesList.forEach { (category, item) ->
            updatedWeek = updatedWeek.addExpenseToDay(dayName, category, item)
        }
        return updatedWeek
    }

    fun addExpenseToCorrespondingDay(
        category: TimeCategory,
        item: ExpenseItem
    ): WeekRecord {
        val dayName = getDayNameFromDate(item.date)
        return addExpenseToDay(dayName, category, item)
    }

    fun updateExpenseInDay(
        dayName: String,
        category: TimeCategory,
        itemId: String,
        newPrice: Double
    ): WeekRecord {
        val updatedDays = days.map { day ->
            if (day.dayName == dayName) {
                day.updateExpense(category, itemId, newPrice)
            } else {
                day
            }
        }
        return copy(days = updatedDays)
    }

    fun updateFullExpenseInDay(
        dayName: String,
        oldCategory: TimeCategory,
        itemId: String,
        newCategory: TimeCategory,
        newName: String,
        newPrice: Double,
        newQuantity: Int
    ): WeekRecord {
        val updatedDays = days.map { day ->
            if (day.dayName == dayName) {
                day.updateFullExpense(oldCategory, itemId, newCategory, newName, newPrice, newQuantity)
            } else {
                day
            }
        }
        return copy(days = updatedDays)
    }

    // Nuevo método para eliminar gasto en un día de la semana
    fun deleteExpenseInDay(
        dayName: String,
        category: TimeCategory,
        itemId: String
    ): WeekRecord {
        val updatedDays = days.map { day ->
            if (day.dayName == dayName) {
                day.deleteExpense(category, itemId)
            } else {
                day
            }
        }
        return copy(days = updatedDays)
    }
}

data class CompensationItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis()
)

data class ShoppingListItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val isChecked: Boolean = false
) {
    val total: Double
        get() = price
}

data class ShoppingList(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val optionalBudget: Double = 0.0,
    val items: List<ShoppingListItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalSpent: Double
        get() = items.filter { it.isChecked }.sumOf { it.price }

    fun addItem(name: String, price: Double): ShoppingList {
        val newItem = ShoppingListItem(name = name, price = price)
        return copy(items = items + newItem)
    }

    fun addItems(newItems: List<ShoppingListItem>): ShoppingList {
        return copy(items = items + newItems)
    }

    fun removeItem(itemId: String): ShoppingList {
        val updatedItems = items.filter { it.id != itemId }
        return copy(items = updatedItems)
    }

    fun toggleItemCheck(itemId: String): ShoppingList {
        val updatedItems = items.map { item ->
            if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
        }
        return copy(items = updatedItems)
    }

    fun resetList(): ShoppingList {
        return copy(items = emptyList())
    }

    fun updateList(newTitle: String, newBudget: Double): ShoppingList {
        return copy(title = newTitle, optionalBudget = newBudget)
    }

    fun updateItem(itemId: String, newName: String, newPrice: Double): ShoppingList {
        val updatedItems = items.map { item ->
            if (item.id == itemId) item.copy(name = newName, price = newPrice) else item
        }
        return copy(items = updatedItems)
    }
}
