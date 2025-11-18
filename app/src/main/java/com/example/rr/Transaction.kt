package com.example.rr

data class Transaction(
    val id: String = "",
    val name: String = "",
    val amount: Long = 0,
    val date: String = "",
    val roi: String = "0.00",
    val remarks: String = "",
    val type: String = "credit",  // "credit" or "debit"
    val timestamp: Long = System.currentTimeMillis()
)
