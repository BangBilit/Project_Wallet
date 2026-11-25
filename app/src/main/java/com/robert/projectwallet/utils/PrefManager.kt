package com.robert.projectwallet.utils

import android.content.Context
import android.content.SharedPreferences
import com.robert.projectwallet.model.Category
import org.json.JSONArray
import org.json.JSONObject

class PrefManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "project_wallet_prefs"
        private const val KEY_CATEGORIES = "key_categories"
    }

    init {
        // Data coba coba
        if (getCategories().isEmpty()) {
            val defaultCategories = listOf(
                Category("1", "Gaji", "INCOME"),
                Category("2", "Bonus", "INCOME"),
                Category("3", "Makanan", "EXPENSE"),
                Category("4", "Transportasi", "EXPENSE"),
                Category("5", "Belanja", "EXPENSE"),
                Category("6", "Tagihan", "EXPENSE")
            )
            saveCategories(defaultCategories)
        }
    }

    fun getCategories(): List<Category> {
        val jsonString = prefs.getString(KEY_CATEGORIES, null) ?: return emptyList()
        val list = mutableListOf<Category>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val type = obj.getString("type")
                list.add(Category(id, name, type))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveCategories(categories: List<Category>) {
        val jsonArray = JSONArray()
        for (category in categories) {
            val obj = JSONObject().apply {
                put("id", category.id)
                put("name", category.name)
                put("type", category.type)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CATEGORIES, jsonArray.toString()).apply()
    }

    fun addCategory(category: Category) {
        val list = getCategories().toMutableList()
        list.add(category)
        saveCategories(list)
    }

    fun updateCategory(category: Category) {
        val list = getCategories().toMutableList()
        val index = list.indexOfFirst { it.id == category.id }
        if (index != -1) {
            list[index] = category
            saveCategories(list)
        }
    }

    fun deleteCategory(categoryId: String) {
        val list = getCategories().toMutableList()
        list.removeAll { it.id == categoryId }
        saveCategories(list)
    }
}
