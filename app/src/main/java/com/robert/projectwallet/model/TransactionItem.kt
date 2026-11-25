package com.robert.projectwallet.model

data class TransactionItem(
    val id: String,
    val title: String,
    val amount: Double,
    val categoryId: String,
    val categoryName: String,
    val date: String, // format yyyy-MM-dd
    val type: String // "INCOME" or "EXPENSE"
)
