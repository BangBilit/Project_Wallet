package com.robert.projectwallet.model

data class Category(
    val id: String,
    val name: String,
    val type: String // "INCOME" or "EXPENSE"
)
