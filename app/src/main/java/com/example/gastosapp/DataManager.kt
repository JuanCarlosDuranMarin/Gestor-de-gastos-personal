package com.example.gastosapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object DataManager {

    private const val PREFS_NAME = "gastos_app_data"
    private const val KEY_MONTHLY_BUDGET = "monthly_budget"
    private const val KEY_MONTHLY_SET = "monthly_set"
    private const val KEY_WEEKS = "weeks"
    private const val KEY_DIRECT_PURCHASE_CATEGORIES = "direct_purchase_categories"
    private const val KEY_CALENDAR_DATA = "calendar_user_data"
    private const val KEY_COMPENSATIONS = "compensations"
    private const val KEY_SHOPPING_LISTS = "shopping_lists"

    fun saveData(
        context: Context,
        monthlyBudget: Double,
        isMonthlySet: Boolean,
        weeks: List<WeekRecord>,
        directCategories: List<DirectPurchaseCategory>,
        calendarData: Map<String, CalendarDayStatus>,
        compensations: List<CompensationItem>,
        shoppingLists: List<ShoppingList>
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val weeksArray = JSONArray()
        weeks.forEach { week ->
            val weekObject = JSONObject().apply {
                put("id", week.id)
                put("weekNumber", week.weekNumber)
                put("allocatedBudget", week.allocatedBudget)
                val daysArray = JSONArray()
                week.days.forEach { day ->
                    val dayObject = JSONObject().apply {
                        put("dayName", day.dayName)
                        val expensesObject = JSONObject()
                        TimeCategory.entries.forEach { category ->
                            val expensesArray = JSONArray()
                            day.expenses[category]?.forEach { item ->
                                expensesArray.put(JSONObject().apply {
                                    put("id", item.id)
                                    put("name", item.name)
                                    put("price", item.price)
                                    put("quantity", item.quantity)
                                    put("date", item.date)
                                })
                            }
                            expensesObject.put(category.name, expensesArray)
                        }
                        put("expenses", expensesObject)
                    }
                    daysArray.put(dayObject)
                }
                put("days", daysArray)
            }
            weeksArray.put(weekObject)
        }

        val categoriesArray = JSONArray()
        directCategories.forEach { cat ->
            val catObj = JSONObject().apply {
                put("id", cat.id)
                put("categoryName", cat.categoryName)
                put("allocatedBudget", cat.allocatedBudget)
                val itemsArray = JSONArray()
                cat.items.forEach { item ->
                    itemsArray.put(JSONObject().apply {
                        put("id", item.id)
                        put("name", item.name)
                        put("price", item.price)
                        put("quantity", item.quantity)
                        put("date", item.date)
                    })
                }
                put("items", itemsArray)
            }
            categoriesArray.put(catObj)
        }

        val calendarObj = JSONObject()
        calendarData.forEach { (dateKey, status) ->
            calendarObj.put(dateKey, status.name)
        }

        val compsArray = JSONArray()
        compensations.forEach { comp ->
            compsArray.put(JSONObject().apply {
                put("id", comp.id)
                put("name", comp.name)
                put("amount", comp.amount)
                put("createdAt", comp.createdAt)
            })
        }

        val listsArray = JSONArray()
        shoppingLists.forEach { list ->
            listsArray.put(JSONObject().apply {
                put("id", list.id)
                put("title", list.title)
                put("optionalBudget", list.optionalBudget)
                put("createdAt", list.createdAt)
                val itemsArr = JSONArray()
                list.items.forEach { item ->
                    itemsArr.put(JSONObject().apply {
                        put("id", item.id)
                        put("name", item.name)
                        put("price", item.price)
                        put("isChecked", item.isChecked)
                    })
                }
                put("items", itemsArr)
            })
        }

        prefs.edit()
            .putFloat(KEY_MONTHLY_BUDGET, monthlyBudget.toFloat())
            .putBoolean(KEY_MONTHLY_SET, isMonthlySet)
            .putString(KEY_WEEKS, weeksArray.toString())
            .putString(KEY_DIRECT_PURCHASE_CATEGORIES, categoriesArray.toString())
            .putString(KEY_CALENDAR_DATA, calendarObj.toString())
            .putString(KEY_COMPENSATIONS, compsArray.toString())
            .putString(KEY_SHOPPING_LISTS, listsArray.toString())
            .apply()
    }

    data class SavedData(
        val monthlyBudget: Double,
        val isMonthlySet: Boolean,
        val weeks: List<WeekRecord>,
        val directCategories: List<DirectPurchaseCategory>,
        val calendarData: Map<String, CalendarDayStatus>,
        val compensations: List<CompensationItem>,
        val shoppingLists: List<ShoppingList>
    )

    fun loadData(context: Context): SavedData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val monthlyBudget = prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        val isMonthlySet = prefs.getBoolean(KEY_MONTHLY_SET, false)

        val weeksJson = prefs.getString(KEY_WEEKS, null)
        val categoriesJson = prefs.getString(KEY_DIRECT_PURCHASE_CATEGORIES, null)
        val calendarJson = prefs.getString(KEY_CALENDAR_DATA, null)
        val compensationsJson = prefs.getString(KEY_COMPENSATIONS, null)
        val listsJson = prefs.getString(KEY_SHOPPING_LISTS, null)

        val weeks = mutableListOf<WeekRecord>()
        if (!weeksJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(weeksJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id")
                    val weekNumber = obj.optInt("weekNumber", 1)
                    val allocatedBudget = obj.optDouble("allocatedBudget", 0.0)

                    val daysArr = obj.optJSONArray("days")
                    val days = mutableListOf<DayRecord>()

                    if (daysArr != null) {
                        for (j in 0 until daysArr.length()) {
                            val dayObj = daysArr.getJSONObject(j)
                            val dayName = dayObj.optString("dayName", "")
                            val expensesObj = dayObj.optJSONObject("expenses")
                            val expensesMap = mutableMapOf<TimeCategory, MutableList<ExpenseItem>>()

                            TimeCategory.entries.forEach { category ->
                                val list = mutableListOf<ExpenseItem>()
                                val itemsArr = expensesObj?.optJSONArray(category.name)
                                if (itemsArr != null) {
                                    for (k in 0 until itemsArr.length()) {
                                        val itemObj = itemsArr.getJSONObject(k)
                                        list.add(
                                            ExpenseItem(
                                                id = itemObj.optString("id"),
                                                name = itemObj.optString("name"),
                                                price = itemObj.optDouble("price", 0.0),
                                                quantity = itemObj.optInt("quantity", 1),
                                                date = itemObj.optLong("date", System.currentTimeMillis())
                                            )
                                        )
                                    }
                                }
                                expensesMap[category] = list
                            }
                            days.add(DayRecord(dayName = dayName, expenses = expensesMap))
                        }
                    }
                    if (days.isNotEmpty()) {
                        weeks.add(WeekRecord(id = id, weekNumber = weekNumber, allocatedBudget = allocatedBudget, days = days))
                    } else {
                        weeks.add(WeekRecord(id = id, weekNumber = weekNumber, allocatedBudget = allocatedBudget))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val categories = mutableListOf<DirectPurchaseCategory>()
        if (!categoriesJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(categoriesJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id")
                    val name = obj.optString("categoryName", "Productos")
                    val budget = obj.optDouble("allocatedBudget", 0.0)
                    val itemsArr = obj.optJSONArray("items")
                    val items = mutableListOf<DirectPurchaseItem>()

                    if (itemsArr != null) {
                        for (j in 0 until itemsArr.length()) {
                            val itemObj = itemsArr.getJSONObject(j)
                            items.add(
                                DirectPurchaseItem(
                                    id = itemObj.optString("id"),
                                    name = itemObj.optString("name"),
                                    price = itemObj.optDouble("price", 0.0),
                                    quantity = itemObj.optInt("quantity", 1),
                                    date = itemObj.optLong("date", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                    categories.add(DirectPurchaseCategory(id = id, categoryName = name, allocatedBudget = budget, items = items))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val calendarData = mutableMapOf<String, CalendarDayStatus>()
        if (!calendarJson.isNullOrBlank()) {
            try {
                val obj = JSONObject(calendarJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val statusName = obj.getString(key)
                    calendarData[key] = CalendarDayStatus.valueOf(statusName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val compensations = mutableListOf<CompensationItem>()
        if (!compensationsJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(compensationsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    compensations.add(
                        CompensationItem(
                            id = obj.optString("id"),
                            name = obj.optString("name"),
                            amount = obj.optDouble("amount", 0.0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val shoppingLists = mutableListOf<ShoppingList>()
        if (!listsJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(listsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id")
                    val title = obj.optString("title", "")
                    val budget = obj.optDouble("optionalBudget", 0.0)
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    val itemsArr = obj.optJSONArray("items")
                    val items = mutableListOf<ShoppingListItem>()
                    if (itemsArr != null) {
                        val length = itemsArr.length()
                        for (j in 0 until length) {
                            val itemObj = itemsArr.getJSONObject(j)
                            items.add(
                                ShoppingListItem(
                                    id = itemObj.optString("id"),
                                    name = itemObj.optString("name"),
                                    price = itemObj.optDouble("price", 0.0),
                                    isChecked = itemObj.optBoolean("isChecked", false)
                                )
                            )
                        }
                    }
                    shoppingLists.add(ShoppingList(id = id, title = title, optionalBudget = budget, items = items, createdAt = createdAt))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return SavedData(monthlyBudget, isMonthlySet, weeks, categories, calendarData, compensations, shoppingLists)
    }
}
